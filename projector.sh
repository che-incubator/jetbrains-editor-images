#!/bin/bash

#
# Copyright (c) 2021 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

base_dir=$(
  cd "$(dirname "$0")" || exit
  pwd
)

CONTAINER_TAG=
URL=

COMMAND=
CONTAINER_TO_RUN=
VOLUMES="$HOME/projector-user:/home/projector-user,$HOME/projector-projects:/projects"
RUN_ON_BUILD=false
SAVE_ON_BUILD=false
SAVE_ON_BUILD_DIRECTORY="$base_dir"/build/docker

PROGRESS=auto
CONFIG_JSON_PATH="$base_dir"/compatible-ide.json
PREPARE_ASSEMBLY_ONLY=false

STATIC_ASSEMBLY_ASSET="$base_dir"/asset-static-assembly.tar.gz

PLUGIN_BUILDER_IMAGE="che-plugin:latest"
PLUGIN_ASSET_SRC=plugin/asset-che-plugin-assembly.zip
PLUGIN_ASSET_DEST=asset-che-plugin-assembly.zip

IDE_DOWNLOADER_IMAGE="ide-downloader:latest"
IDE_ASSET_SRC=ide/asset-ide-packaging.tar.gz
IDE_ASSET_DEST="$base_dir"/asset-ide-packaging.tar.gz

PROJECTOR_SERVER_IMAGE="projector:latest"
PROJECTOR_SERVER_ASSET_SRC=projector/asset-projector-server-assembly.zip
PROJECTOR_SERVER_ASSET_DEST="$base_dir"/asset-projector-server-assembly.zip

MACHINE_EXEC_IMAGE="machine-exec-provider:latest"
MACHINE_EXEC_ASSET_SRC=exec/machine-exec
MACHINE_EXEC_ASSET_DEST="$base_dir"/asset-machine-exec

DOC_URL=https://github.com/che-incubator/jetbrains-editor-images/tree/main/doc

# Logging configuration
# https://en.wikipedia.org/wiki/Syslog#Severity_level
_RD='\033[0;31m' # Red
_GR='\033[0;32m' # Green
_YL='\033[1;33m' # Yellow
_PL='\033[0;35m' # Purple
_LG='\033[0;37m' # Light Gray
_NC='\033[0m'    # No Color
VERBOSE_LEVEL=6
declare LOG_LEVELS
LOG_LEVELS=([0]="${_PL}emerg${_NC}" [3]="${_RD}err${_NC}" [4]="${_YL}warning${_NC}" [6]="${_GR}info${_NC}" [7]="${_LG}debug${_NC}")
function .log() {
  local LEVEL=${1}
  shift
  if [ $VERBOSE_LEVEL -ge "$LEVEL" ]; then
    echo -e "[${LOG_LEVELS[$LEVEL]}]" "$@"
  fi
}

function log:emerg() {
  .log 0 "$@"
}
function log:err() {
  .log 3 "$@"
}
function log:warning() {
  .log 4 "$@"
}
function log:info() {
  .log 6 "$@"
}
function log:debug() {
  .log 7 "$@"
}

read -r -d '' HELP_SUMMARY <<-EOM
Usage: $0 COMMAND [OPTIONS]

Projector-based container manager

Options:
  -h, --help              Display help information
  -v, --version           Display version information
  -l, --log-level string  Set the logging level ("debug"|"info"|"warn"|"error"|"fatal") (default "info")

Commands:
  build   Build an image for the particular IntelliJ-based IDE package
  run     Start a container with IntelliJ-based IDE

Run '$0 COMMAND --help' for more information on a command.
To get more help with the '$0' check out guides at $DOC_URL
EOM

read -r -d '' HELP_BUILD_COMMAND <<-EOM
Usage: $0 build [OPTIONS]

Build an image for the particular IntelliJ-based IDE package

Note, that if '--tag' or '--url' option is missed, then interactive wizard will be invoked to choose
the predefined IDE packaging from the default configuration.

Options:
  -t, --tag string              Name and optionally a tag in the 'name:tag' format for the result image
  -u, --url string              Downloadable URL of IntelliJ-based IDE package, should be a tar.gz archive
      --run-on-build            Run the container immediately after build
      --save-on-build           Save the image to a tar archive after build. Basename of --url.
      --mount-volumes [string]  Mount volumes to the container which was started using '--run-on-build' option
                                Volumes should be separated by comma, e.g. "/l/path_1:/r/path_1,/l/path_2:/r/path_2".
                                If option value is omitted, then default value is loaded.
                                Default value: \$HOME/projector-user:/home/projector-user,\$HOME/projector-projects:/projects
  -p, --progress string         Set type of progress output ("auto"|"plain") (default "auto")
      --config string           Specify the configuration file for predefined IDE package list (default "$CONFIG_JSON_PATH")
      --prepare                 Clone and build Projector only ignoring other options. Also downloads the IDE packaging
                                by the --url option. If --url option is omitted then interactive wizard is called to choose
                                the right packaging to prepare. Used when need to fetch Projector sources only, assembly
                                the binaries and download the IDE packaging.

EOM

read -r -d '' HELP_RUN_COMMAND <<-EOM
Usage: $0 run CONTAINER [OPTIONS]

Start a container with IntelliJ-based IDE

Options:
      --mount-volumes [string]  Mount volumes to the container which was started using '--run-on-build' option.
                                Volumes should be separated by comma, e.g. "/l/path_1:/r/path_1,/l/path_2:/r/path_2".
                                If option value is omitted, then default value is loaded.
                                Default value: \$HOME/projector-user:/home/projector-user,\$HOME/projector-projects:/projects

EOM

read -r -d '' GETOPT_UPDATE_NEEDED <<-EOM
getopt utility should be updated
         Need to perform:
           brew install gnu-getopt
           brew link --force gnu-getopt
EOM

selectWithDefault() {
  local item i=0 numItems=$#

  for item; do
    printf '%7s%s\n' "" "$((++i))) $item"
  done >&2

  while :; do
    printf %s "${PS3-#? }" >&2
    read -r index
    [ -z "$index" ] && break
    ((index >= 1 && index <= numItems)) 2>/dev/null || {
      log:warning "Choose correct item" >&2
      continue
    }
    break
  done

  [ -n "$index" ] && printf %s "${@:index:1}"
}

checkConfigurationFileExists() {
  log:debug "Check if configuration file '$CONFIG_JSON_PATH' exists"
  if [ ! -e "$CONFIG_JSON_PATH" ]; then
    log:warning "Configuration file '$CONFIG_JSON_PATH' not found"
    exit 1
  fi
  log:debug "Configuration file '$CONFIG_JSON_PATH' found"
}

selectPackagingFromPredefinedConfig() {
  log:debug "Prompt user to choose IDE packaging from predefined configuration"

  IFS=$'\n' read -r -d '' -a IDEName < <(jq -c -r ".[] | .displayName" <"$CONFIG_JSON_PATH")
  log:info "Select the IDE package to build (default is '${IDEName[0]}'):"
  local selectedIDEName && selectedIDEName=$(selectWithDefault "${IDEName[@]}")
  case $selectedIDEName in
  '') selectedIDEName=${IDEName[0]} ;;
  esac
  log:debug "Selected '$selectedIDEName' package"

  IFS=$'\n' read -r -d '' -a IDEVersion < <(jq -c -r ".[] | {displayName, dockerImage, productCode} + (.productVersion[]) | select(.displayName == \"$selectedIDEName\") | .version" <"$CONFIG_JSON_PATH")
  log:info "Select the IDE package version to build (default is '${IDEVersion[0]}'):"
  local selectedIDEVersion && selectedIDEVersion=$(selectWithDefault "${IDEVersion[@]}")
  case $selectedIDEVersion in
  '') selectedIDEVersion=${IDEVersion[0]} ;;
  esac
  log:debug "Selected '$selectedIDEVersion' package version"

  IFS=$'\n' read -r -d '' -a dockerImageNameToSelect < <(jq -c -r ".[] | {displayName, dockerImage, productCode} + (.productVersion[]) | select(.displayName == \"$selectedIDEName\") | select(.version == \"$selectedIDEVersion\") | .dockerImage" <"$CONFIG_JSON_PATH")
  CONTAINER_TAG="${dockerImageNameToSelect[0]}:$selectedIDEVersion"
  log:info "Read the container name '$CONTAINER_TAG'"
  IFS=$'\n' read -r -d '' -a downloadUrlToSelect < <(jq -c -r ".[] | {displayName, dockerImage, productCode} + (.productVersion[]) | select(.displayName == \"$selectedIDEName\") | select(.version == \"$selectedIDEVersion\") | .downloadUrl" <"$CONFIG_JSON_PATH")
  URL=${downloadUrlToSelect[0]}
  log:info "Read the URL for IDE packaging '$URL'"
}

prepareProjectorServerAsset() {
  cd "$base_dir" || exit 1
  log:debug "Current working directory '$(pwd)'"
  if [ -f "$PROJECTOR_SERVER_ASSET_DEST" ]; then
      log:debug "Removing '$PROJECTOR_SERVER_ASSET_DEST'"
      rm "$PROJECTOR_SERVER_ASSET_DEST"
    fi

  read -r -d '' PRE_BUILD_SUMMARY <<-EOM
  Pre-build $PROJECTOR_SERVER_IMAGE container final summary
          Docker build progress configuration: $PROGRESS
          Container name: $PROJECTOR_SERVER_IMAGE
EOM
  log:debug "$PRE_BUILD_SUMMARY"
  log:info "Build '$PROJECTOR_SERVER_IMAGE'"

  docker build --progress="$PROGRESS" -f build/dockerfiles/projector-server-builder.Dockerfile -t "$PROJECTOR_SERVER_IMAGE" .
  # shellcheck disable=SC2181
  if [[ $? -eq 0 ]]; then
    log:info "Container '$PROJECTOR_SERVER_IMAGE' successfully built"
  else
    log:warning "Container build failed"
    exit 1
  fi

  extractFromContainer "$PROJECTOR_SERVER_IMAGE" "$PROJECTOR_SERVER_ASSET_SRC" "$PROJECTOR_SERVER_ASSET_DEST"
}

prepareChePluginAsset() {
  cd "$base_dir" || exit 1
  log:debug "Current working directory '$(pwd)'"
  if [ -f "$PLUGIN_ASSET_DEST" ]; then
    log:debug "Removing '$PLUGIN_ASSET_DEST'"
    rm "$PLUGIN_ASSET_DEST"
  fi

  read -r -d '' PRE_BUILD_SUMMARY <<-EOM
  Pre-build $PLUGIN_BUILDER_IMAGE container final summary
          Docker build progress configuration: $PROGRESS
          Container name: $PLUGIN_BUILDER_IMAGE
EOM
  log:debug "$PRE_BUILD_SUMMARY"
  log:info "Build '$PLUGIN_BUILDER_IMAGE'"

  docker build --progress="$PROGRESS" -f build/dockerfiles/che-plugin-builder.Dockerfile -t "$PLUGIN_BUILDER_IMAGE" .
  # shellcheck disable=SC2181
  if [[ $? -eq 0 ]]; then
    log:info "Container '$PLUGIN_BUILDER_IMAGE' successfully built"
  else
    log:warning "Container build failed"
    exit 1
  fi

  extractFromContainer "$PLUGIN_BUILDER_IMAGE" "$PLUGIN_ASSET_SRC" "$PLUGIN_ASSET_DEST"
}

prepareMachineExecBinary() {
  cd "$base_dir" || exit 1
  log:debug "Current working directory '$(pwd)'"
  if [ -f "$MACHINE_EXEC_ASSET_DEST" ]; then
    log:debug "Removing '$MACHINE_EXEC_ASSET_DEST'"
    rm "$MACHINE_EXEC_ASSET_DEST"
  fi

  read -r -d '' PRE_BUILD_SUMMARY <<-EOM
    Pre-build $MACHINE_EXEC_IMAGE container final summary
            Docker build progress configuration: $PROGRESS
            Container name: $MACHINE_EXEC_IMAGE
EOM
    log:debug "$PRE_BUILD_SUMMARY"
    log:info "Build '$MACHINE_EXEC_IMAGE'"

    docker build --progress="$PROGRESS" -f build/dockerfiles/machine-exec-provider.Dockerfile  -t "$MACHINE_EXEC_IMAGE" .
    # shellcheck disable=SC2181
    if [[ $? -eq 0 ]]; then
      log:info "Container '$MACHINE_EXEC_IMAGE' successfully built"
    else
      log:warning "Container build failed"
      exit 1
    fi

  extractFromContainer "$MACHINE_EXEC_IMAGE" "$MACHINE_EXEC_ASSET_SRC" "$MACHINE_EXEC_ASSET_DEST"
}

buildAssembly() {
  cd "$base_dir" || exit 1
  log:debug "Current working directory '$(pwd)'"

  read -r -d '' PRE_BUILD_SUMMARY <<-EOM
Pre-build $CONTAINER_TAG final summary
        Docker build progress configuration: $PROGRESS
        Container name: $CONTAINER_TAG
        IDE package URL: $URL
EOM
  log:debug "$PRE_BUILD_SUMMARY"
  log:info "Build '$CONTAINER_TAG'"

  DOCKER_BUILDKIT=1 \
    docker build \
    --progress="$PROGRESS" \
    -t "$CONTAINER_TAG" \
    -f Dockerfile .
  # shellcheck disable=SC2181
  if [[ $? -eq 0 ]]; then
    log:info "Container '$CONTAINER_TAG' successfully built"
  else
    log:warning "Container build failed"
    exit 1
  fi
}

runContainerImage() {
  local containerToStart && containerToStart=${1}

  local mountVolumes && mountVolumes=${2}
  local mountOptions=()
  while IFS=',' read -ra MOUNT; do
    for i in "${MOUNT[@]}"; do
      mountOptions+=(-v "$i")
      log:debug "Adding volume mount '$i'"
    done
  done <<<"$mountVolumes"

  log:info "Run container '$containerToStart'"
  docker run --rm -p 8887:8887 "${mountOptions[@]}" -it "$containerToStart" 2>&1 | awk '{print "       "$0}'
}

saveImageOnBuild() {
  if [ $SAVE_ON_BUILD == true ]; then
    if [ ! -e "$SAVE_ON_BUILD_DIRECTORY" ]; then
      mkdir -p "$SAVE_ON_BUILD_DIRECTORY"
    fi
    local imageOutputName && imageOutputName=$(basename "$URL")
    log:info "Saving '$CONTAINER_TAG' to '$SAVE_ON_BUILD_DIRECTORY/$imageOutputName'"
    docker save "$CONTAINER_TAG" -o "$SAVE_ON_BUILD_DIRECTORY"/"$imageOutputName"
    log:info "Image '$CONTAINER_TAG' saved to '$SAVE_ON_BUILD_DIRECTORY/$imageOutputName'"
  fi
}

runContainerOnBuild() {
  log:debug "Check if container should be run after built"
  if [ $RUN_ON_BUILD == true ]; then
    runContainerImage "$CONTAINER_TAG" "$VOLUMES"
  fi
}

prepareStaticAsset() {
  cd "$base_dir" || exit 1
  log:debug "Current working directory '$(pwd)'"
  if [ -f "$STATIC_ASSEMBLY_ASSET" ]; then
    log:debug "Removing '$STATIC_ASSEMBLY_ASSET'"
    rm "$STATIC_ASSEMBLY_ASSET"
  fi

  log:debug "Creating archive for Projector static files '$STATIC_ASSEMBLY_ASSET'"
  tar -czf "$STATIC_ASSEMBLY_ASSET" static
}

prepareIdePackagingAsset() {
  cd "$base_dir" || exit 1
  log:debug "Current working directory '$(pwd)'"
  if [ -f "$IDE_ASSET_DEST" ]; then
    log:debug "Removing '$IDE_ASSET_DEST'"
    rm "$IDE_ASSET_DEST"
  fi

  read -r -d '' PRE_BUILD_SUMMARY <<-EOM
  Pre-build $IDE_DOWNLOADER_IMAGE container final summary
          Docker build progress configuration: $PROGRESS
          Container name: $IDE_DOWNLOADER_IMAGE
EOM
  log:debug "$PRE_BUILD_SUMMARY"
  log:info "Build '$IDE_DOWNLOADER_IMAGE'"

  docker build --progress="$PROGRESS" -f build/dockerfiles/ide-downloader.Dockerfile --build-arg "URL=$URL" -t "$IDE_DOWNLOADER_IMAGE" .
  # shellcheck disable=SC2181
  if [[ $? -eq 0 ]]; then
    log:info "Container '$IDE_DOWNLOADER_IMAGE' successfully built"
  else
    log:warning "Container build failed"
    exit 1
  fi

  extractFromContainer "$IDE_DOWNLOADER_IMAGE" "$IDE_ASSET_SRC" "$IDE_ASSET_DEST"
}

prepareBuildAssets() {
  log:info "Prepare build assets"
  if [ -z "$CONTAINER_TAG" ] || [ -z "$URL" ]; then
    log:debug "Ignoring --tag and --url option"
    checkConfigurationFileExists

    # Run interactive wizard to choose IDE packaging from predefined configuration
    selectPackagingFromPredefinedConfig
  fi

  prepareStaticAsset
  prepareIdePackagingAsset
  prepareProjectorServerAsset
  prepareChePluginAsset
  prepareMachineExecBinary
}

buildContainerImage() {
  prepareBuildAssets

  buildAssembly
  saveImageOnBuild
  runContainerOnBuild
}

printVersion() {
  read -r -d '' VERSION_INFO <<-EOM
$0 - CLI tool for build Projector-based IDE in Eclipse Che
       Revision: $(git show -s --format='%h %s')
EOM
  log:info "$VERSION_INFO"
}

rebaseProjectorSources() {
  log:info "Using git $(which git) $(git --version)"
  PROJECTOR_CLIENT_PREFIX="projector-client"
  PROJECTOR_CLIENT_UPSTREAM_NAME="upstream-projector-client"
  PROJECTOR_CLIENT_UPSTREAM_VERSION=$(git rev-parse upstream-projector-client/master)

  PROJECTOR_SERVER_PREFIX="projector-server"
  PROJECTOR_SERVER_UPSTREAM_NAME="upstream-projector-server"
  PROJECTOR_SERVER_UPSTREAM_VERSION=$(git rev-parse upstream-projector-server/master)

  gitSubTreePull "${PROJECTOR_CLIENT_PREFIX}" "${PROJECTOR_CLIENT_UPSTREAM_NAME}" "${PROJECTOR_CLIENT_UPSTREAM_VERSION}"
  gitSubTreePull "${PROJECTOR_SERVER_PREFIX}" "${PROJECTOR_SERVER_UPSTREAM_NAME}" "${PROJECTOR_SERVER_UPSTREAM_VERSION}"
}

# $1 is the prefix name
# $2 is the upstream name
# $3 is the revision to pull
gitSubTreePull() {
  log:info "Perform git subtree pull for '$1' '$2' '$3'"
  git subtree pull --prefix "$1" "$2" "$3" --squash -m "$(getCommitMessage "$3")"
}

# $1 is the revision
getCommitMessage() {
  echo "Rebase against the upstream ${1}"
  echo "upstream-sha1: ${1}"
}

# $1 is the container name
# $2 is the path to extract from the container
# $3 is the destination path to where located extracted path
extractFromContainer() {
  log:info "Extract '$2' from '$1' container to '$3'"
  tmpContainer="$(echo "$1" | tr "/:" "--")-$(date +%s)"

  log:info "Using temporary container '$tmpContainer'"
  docker create --name="$tmpContainer" "$1" sh >/dev/null 2>&1
  docker export "$tmpContainer" > "/tmp/$tmpContainer.tar"

  tmpDir="/tmp/$tmpContainer"
  log:info "Created temporary directory '$tmpDir'"
  rm -rf "$tmpDir" || true
  mkdir -p "$tmpDir"

  log:info "Trying to unpack container '$tmpContainer'"
  tar -xf "/tmp/$tmpContainer.tar" -C "$tmpDir" --no-same-owner "$2" || exit 1

  log:info "Moving '$tmpDir/$2' to '$3'"
  mv "$tmpDir/$2" "$3"

  log:info "Clean up the temporary container and directory"
  docker rm -f "$tmpContainer" >/dev/null 2>&1
  rm -rf "/tmp/$tmpContainer.tar"
  rm -rf "$tmpDir" || true
}

# getopt necessary checks
getopt -T &>/dev/null
if [[ $? -ne 4 ]]; then
  log:warning "Found outdated version of 'getopt'."
  if [[ "$OSTYPE" == "darwin"* ]]; then
    log:warning "$GETOPT_UPDATE_NEEDED"
  fi
  exit 1
fi

OPTS=$(getopt -o 'hvt:up:l:' --longoptions 'help,version,tag:,url:,mount-volumes:,run-on-build,save-on-build,progress:,log-level:,config:,prepare' -u -n "$0" -- "$@")
# shellcheck disable=SC2181
if [[ $? -ne 0 ]]; then
  log:warning "Failed parsing options."
  exit 1
fi
# shellcheck disable=SC2086
set -- $OPTS

while true; do
  case $1 in
  -h | --help)
    shift
    case $2 in
    build)
      echo "$HELP_BUILD_COMMAND"
      exit 0
      ;;
    run)
      echo "$HELP_RUN_COMMAND"
      exit 0
      ;;
    *)
      echo "$HELP_SUMMARY"
      exit 0
      ;;
    esac
    ;;
  -v | --version)
    printVersion
    exit 0
    ;;
  -t | --tag)
    CONTAINER_TAG=$2
    shift 2
    ;;
  -u | --url)
    URL=$2
    shift 2
    ;;
  --mount-volumes)
    case $2 in
    --)
      shift
      ;;
    *)
      VOLUMES=$2
      shift 2
      ;;
    esac
    ;;
  --run-on-build)
    RUN_ON_BUILD=true
    shift
    ;;
  --save-on-build)
    SAVE_ON_BUILD=true
    shift
    ;;
  --prepare)
    PREPARE_ASSEMBLY_ONLY=true
    shift
    ;;
  -p | --progress)
    case $2 in
    -- | auto)
      PROGRESS=auto
      shift 2
      ;;
    plain)
      PROGRESS=plain
      shift 2
      ;;
    esac
    ;;
  -l | --log-level)
    case $2 in
    debug)
      VERBOSE_LEVEL=7
      shift 2
      ;;
    info)
      VERBOSE_LEVEL=6
      shift 2
      ;;
    warn)
      VERBOSE_LEVEL=4
      shift 2
      ;;
    error)
      VERBOSE_LEVEL=3
      shift 2
      ;;
    fatal)
      VERBOSE_LEVEL=0
      shift 2
      ;;
    *)
      log:warning "Unable to parse logging level: $2"
      exit 1
      ;;
    esac
    ;;
  --config)
    CONFIG_JSON_PATH=$2
    shift 2
    ;;
  --)
    case $2 in
    build)
      COMMAND=build
      shift
      ;;
    run)
      COMMAND=run
      if [ -z "$3" ]; then
        read -r -d '' RUN_MISSING_IMAGE_NAME_MESSAGE <<-EOM
$0: '$2' requires at least 1 argument.
See: '$0 run --help'.

Usage: $0 run IMAGE [OPTIONS]

Start Projector-based container
EOM
        log:warning "$RUN_MISSING_IMAGE_NAME_MESSAGE"
        exit 1
      fi
      CONTAINER_TO_RUN=$3
      shift 2
      ;;
    rebase)
      COMMAND=rebase
      shift
      ;;
    '')
      echo "$HELP_SUMMARY"
      exit 1
      ;;
    *)
      log:warning "$0: '$2' is not a valid command. See '$0 --help'."
      exit 1
      ;;
    esac
    shift
    break
    ;;
  *)
    break
    ;;
  esac
done

if [ "$COMMAND" == "build" ]; then
  if [ $PREPARE_ASSEMBLY_ONLY == true ]; then
    prepareBuildAssets
  else
    buildContainerImage
  fi
elif [ "$COMMAND" == "run" ]; then
  log:debug "Executing run command"
  runContainerImage "$CONTAINER_TO_RUN" "$VOLUMES"
elif [ "$COMMAND" == "rebase" ]; then
  log:debug "Executing Projector rebase command"
  rebaseProjectorSources
else
  log:debug "Found invalid command to execute."
  exit 1
fi
