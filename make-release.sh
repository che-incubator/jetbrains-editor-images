#!/bin/bash

#
# Copyright (c) 2021 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

base_dir=$(cd "$(dirname "$0")" || exit; pwd)

RELEASE_TAG=
PROJECTOR_CLI_LOG_LEVEL=info
BUILD_DIRECTORY="$base_dir"/build/docker
SKIP_CHECKS=false

# Logging configuration
# https://en.wikipedia.org/wiki/Syslog#Severity_level
_RD='\033[0;31m' # Red
_GR='\033[0;32m' # Green
_YL='\033[1;33m' # Yellow
_PL='\033[0;35m' # Purple
_LG='\033[0;37m' # Light Gray
_NC='\033[0m' # No Color
VERBOSE_LEVEL=6
declare LOG_LEVELS
LOG_LEVELS=([0]="${_PL}emerg${_NC}" [3]="${_RD}err${_NC}" [4]="${_YL}warning${_NC}" [6]="${_GR}info${_NC}" [7]="${_LG}debug${_NC}")
function .log () {
  local LEVEL=${1}
  shift
  if [ $VERBOSE_LEVEL -ge "$LEVEL" ]; then
    echo -e "[${LOG_LEVELS[$LEVEL]}]" "$@"
  fi
}

read -r -d '' HELP_SUMMARY <<- EOM
Usage: $0 [OPTIONS]

Performs the release of editor images.

Options:
  -h, --help              Display help information
  -v, --version           Display version information
  -t, --tag string        Release tag name (e.g. "YYYYMMDD.hashId")
  -l, --log-level string  Set the logging level ("debug"|"info"|"warn"|"error"|"fatal") (default "info")
      --skip-checks       Skip pre-release checks. WARNING! Use this option if you know what you do!

EOM

read -r -d '' GETOPT_UPDATE_NEEDED <<- EOM
getopt utility should be updated
         Need to perform:
           brew install gnu-getopt
           brew link --force gnu-getopt
EOM

printVersion() {
  read -r -d '' VERSION_INFO <<- EOM
$0 - CLI tool for release Projector-based IDE Docker images
       Revision: $(git show -s --format='%h %s')
EOM
  .log 6 "$VERSION_INFO"
}

selectWithDefault() {
  local item i=0 numItems=$#

  for item; do
    printf '%7s%s\n' "" "$((++i))) $item"
  done >&2

  while :; do
    printf %s "${PS3-#? }" >&2
    read -r index
    [ -z "$index" ] && break
    (( index >= 1 && index <= numItems )) 2>/dev/null || { .log 4 "Choose correct item" >&2; continue; }
    break
  done

  [ -n "$index" ] && printf %s "${@: index:1}"
}

# getopt necessary checks
getopt -T &>/dev/null
if [[ $? -ne 4 ]]; then
  .log 4 "Found outdated version of 'getopt'."
  if [[ "$OSTYPE" == "darwin"* ]]; then
    .log 4 "$GETOPT_UPDATE_NEEDED"
  fi
  exit 1
fi

OPTS=$(getopt -o 'hvt:l:' --longoptions 'help,version,tag:,log-level:,skip-checks' -u -n "$0" -- "$@")
# shellcheck disable=SC2181
if [[ $? -ne 0 ]] ; then .log 4 "Failed parsing options."; exit 1; fi
# shellcheck disable=SC2086
set -- $OPTS

while true; do
  case $1 in
    -h | --help )
      echo "$HELP_SUMMARY"
      exit 0
      ;;
    -v | --version )
      printVersion
      exit 0
      ;;
    -t | --tag )
      RELEASE_TAG=$2
      shift 2
      ;;
    --skip-checks )
      SKIP_CHECKS=true
      shift
      ;;
    -l | --log-level )
      PROJECTOR_CLI_LOG_LEVEL=$2
      case $2 in
        debug )
          VERBOSE_LEVEL=7
          shift 2
          ;;
        info )
          VERBOSE_LEVEL=6
          shift 2
          ;;
        warn )
          VERBOSE_LEVEL=4
          shift 2
          ;;
        error )
          VERBOSE_LEVEL=3
          shift 2
          ;;
        fatal )
          VERBOSE_LEVEL=0
          shift 2
          ;;
        * )
          .log 4 "Unable to parse logging level: $2"
          exit 1
      esac
      ;;
    * )
      break
      ;;
  esac
done

if [[ $(git diff --stat) != '' ]]; then
  .log 3 "Repository is dirty! Commit changes and re-run release '$0'"
  exit 1
else
  .log 6 "Repository is clean"
fi

if [ -z "$RELEASE_TAG" ]; then
  .log 4 "Release tag not provided by '--tag' option. Generating release tag based on template 'YYYYMMDD.hashId'."
  RELEASE_TAG=$(date '+%Y%m%d').$(git rev-parse --short HEAD)
fi

.log 6 "Release tag '$RELEASE_TAG' provided"

if [ $SKIP_CHECKS == false ]; then
  BUILD_CONFIGS=$(jq -c -r ".[] | {displayName, dockerImage, productCode} + (.productVersion[]) | @base64" < "$base_dir"/compatible-ide.json)
  for row in $BUILD_CONFIGS; do
    _jq() {
      echo "$row" | base64 --decode | jq -r "${1}"
    }

    displayName="$(_jq '.displayName')"
    dockerImage="$(_jq '.dockerImage')"
    version="$(_jq '.version')"
    downloadUrl="$(_jq '.downloadUrl')"

    .log 6 "Process '$displayName:$version' in pre-release step"

    .log 7 "Check if build directory '$BUILD_DIRECTORY' exists"
    if [ ! -e "$BUILD_DIRECTORY" ]; then
      .log 7 "Creating build directory '$BUILD_DIRECTORY'"
      mkdir -p "$BUILD_DIRECTORY"
    fi
    .log 7 "Build directory '$BUILD_DIRECTORY' exists"

    dockerImageFilePath="$BUILD_DIRECTORY"/$(basename "$downloadUrl")

    .log 7 "Docker image name to store '$dockerImageFilePath'"
    if [ -e "$dockerImageFilePath" ]; then
      .log 7 "Docker image '$dockerImageFilePath' exists. Removing it."
      rm "$dockerImageFilePath"
    fi

    .log 6 "Build Docker image for '$dockerImage:$version' from '$downloadUrl'"
    ./projector.sh build --tag "$dockerImage:$version" --url "$downloadUrl" --save-on-build --log-level "$PROJECTOR_CLI_LOG_LEVEL"

    if [ -e "$dockerImageFilePath" ]; then
      .log 6 "Docker image '$dockerImage:$version' successfully built"
    else
      .log 3 "Release process postponed! See error messages above!"
      exit 1
    fi
  done

  .log 6 "All images successfully processed"
else
  .log 4 "Skip build checks"
fi

.log 6 "Release tag '$RELEASE_TAG' will be created and pushed to remote. Continue? (default is 'no')"
release=$(selectWithDefault "yes" "no")
case $release in
  'yes' )
    release="yes"
    ;;
  '' | 'no')
    release="no"
    ;;
esac

if [ "$release" = "no" ]; then
  .log 6 "Release stopped by entered choice"
  exit 0
fi

.log 6 "Creating tag '$RELEASE_TAG'"
git tag "$RELEASE_TAG"
.log 6 "Push release tag '$RELEASE_TAG' to remote"
git push origin --tags

.log 6 "Changes pushed to remote!"
