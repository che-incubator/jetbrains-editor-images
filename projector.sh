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
BUILD_DIRECTORY="$base_dir"/build
RUN_ON_BUILD=false
SAVE_ON_BUILD=false
SAVE_ON_BUILD_DIRECTORY="$BUILD_DIRECTORY"/docker
IDE_PACKAGING_DIRECTORY="$BUILD_DIRECTORY"/ide
CURRENT_IDE_PACKAGING_SYMLINK="$base_dir"/asset-ide-packaging.tar.gz
CURRENT_PROJECTOR_ASSEMBLY_SYMLINK="$base_dir"/asset-projector-server-assembly.zip
CURRENT_PROJECTOR_STATIC_ASSEMBLY="$base_dir"/asset-static-assembly.tar.gz
PROGRESS=auto
CONFIG_JSON=compatible-ide.json
CONFIG_JSON_PATH="$base_dir"/"$CONFIG_JSON"
PROJECTOR_CLIENT_DIR="$BUILD_DIRECTORY"/projector-client
PROJECTOR_SERVER_DIR="$BUILD_DIRECTORY"/projector-server
PREPARE_ASSEMBLY_ONLY=false

PROJECTOR_CLIENT_GIT=https://github.com/JetBrains/projector-client.git
PROJECTOR_SERVER_GIT=https://github.com/JetBrains/projector-server.git
PROJECTOR_CLIENT_SHA1=d80f9952cc520102bb2de6f1aefaad6aadb49138
PROJECTOR_SERVER_SHA1=e8ecfda9ee04b59e8b4276215e22a3cbff212083

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
      --config string           Specify the configuration file for predefined IDE package list (default "$CONFIG_JSON")
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
      .log 4 "Choose correct item" >&2
      continue
    }
    break
  done

  [ -n "$index" ] && printf %s "${@:index:1}"
}

checkConfigurationFileExists() {
  .log 7 "Check if configuration file '$CONFIG_JSON_PATH' exists"
  if [ ! -e "$CONFIG_JSON_PATH" ]; then
    .log 4 "Configuration file '$CONFIG_JSON_PATH' not found"
    exit 1
  fi
  .log 7 "Configuration file '$CONFIG_JSON_PATH' found"
}

selectPackagingFromPredefinedConfig() {
  .log 7 "Prompt user to choose IDE packaging from predefined configuration"

  IFS=$'\n' read -r -d '' -a IDEName < <(jq -c -r ".[] | .displayName" <"$CONFIG_JSON_PATH")
  .log 6 "Select the IDE package to build (default is '${IDEName[0]}'):"
  local selectedIDEName && selectedIDEName=$(selectWithDefault "${IDEName[@]}")
  case $selectedIDEName in
  '') selectedIDEName=${IDEName[0]} ;;
  esac
  .log 7 "Selected '$selectedIDEName' package"

  IFS=$'\n' read -r -d '' -a IDEVersion < <(jq -c -r ".[] | {displayName, dockerImage, productCode} + (.productVersion[]) | select(.displayName == \"$selectedIDEName\") | .version" <"$CONFIG_JSON_PATH")
  .log 6 "Select the IDE package version to build (default is '${IDEVersion[0]}'):"
  local selectedIDEVersion && selectedIDEVersion=$(selectWithDefault "${IDEVersion[@]}")
  case $selectedIDEVersion in
  '') selectedIDEVersion=${IDEVersion[0]} ;;
  esac
  .log 7 "Selected '$selectedIDEVersion' package version"

  IFS=$'\n' read -r -d '' -a dockerImageNameToSelect < <(jq -c -r ".[] | {displayName, dockerImage, productCode} + (.productVersion[]) | select(.displayName == \"$selectedIDEName\") | select(.version == \"$selectedIDEVersion\") | .dockerImage" <"$CONFIG_JSON_PATH")
  CONTAINER_TAG="${dockerImageNameToSelect[0]}:$selectedIDEVersion"
  .log 6 "Read the container name '$CONTAINER_TAG'"
  IFS=$'\n' read -r -d '' -a downloadUrlToSelect < <(jq -c -r ".[] | {displayName, dockerImage, productCode} + (.productVersion[]) | select(.displayName == \"$selectedIDEName\") | select(.version == \"$selectedIDEVersion\") | .downloadUrl" <"$CONFIG_JSON_PATH")
  URL=${downloadUrlToSelect[0]}
  .log 6 "Read the URL for IDE packaging '$URL'"
}

checkProjectorClientSources() {
  .log 6 "Check Projector Client source directory"
  if [ ! -d "$PROJECTOR_CLIENT_DIR" ]; then
    .log 4 "Projector Client source directory '$PROJECTOR_CLIENT_DIR' doesn't exist"
    .log 6 "Cloning Projector Client sources to '$PROJECTOR_CLIENT_DIR'"

    # Clone the Projector Client, stick to the particular version and apply necessary patches if needed
    git clone --quiet "$PROJECTOR_CLIENT_GIT" "$PROJECTOR_CLIENT_DIR"
    cd "$PROJECTOR_CLIENT_DIR" || exit 1
    .log 7 "Current working directory '$(pwd)'"
    .log 6 "Checkout Projector Client to SHA1 '$PROJECTOR_CLIENT_SHA1'"
    .log 6 "$(git reset --hard $PROJECTOR_CLIENT_SHA1)"

    # Apply patches for Projector Client
    if [ -d "$base_dir/patches/projector-client" ]; then
      .log 6 "Applying patches for Projector Client"
      find "$base_dir"/patches/projector-client -name "*.patch" -exec printf '%7s%s\n' "" "Patching Projector Client with '{}'" \; -exec git apply {} \;
    fi
    cd "$base_dir" || exit 1
    .log 7 "Current working directory '$(pwd)'"
  else
    .log 7 "Projector Client source directory '$PROJECTOR_CLIENT_DIR' exists"
    cd "$PROJECTOR_CLIENT_DIR" || exit 1
    .log 7 "Current working directory '$(pwd)'"

    local projectorServerHead && projectorServerHead=$(git rev-parse HEAD)
    if [ "$PROJECTOR_CLIENT_SHA1" == "$projectorServerHead" ]; then
      .log 6 "Projector Client HEAD '$projectorServerHead' is the same as provided in configuration '$PROJECTOR_CLIENT_SHA1'"
    else
      read -r -d '' HEAD_CHECK_FAILED <<-EOM
Projector Client HEAD '$projectorServerHead' is different than configured '$PROJECTOR_CLIENT_SHA1'.
          In this case build of container image may be unpredictable.
          Consider to remove '$PROJECTOR_CLIENT_DIR' directory or move it to a different place and re-run build again.
EOM
      .log 4 "$HEAD_CHECK_FAILED"
      exit 1
    fi

    cd "$base_dir" || exit 1
    .log 7 "Current working directory '$(pwd)'"
  fi
}

checkProjectorServerSources() {
  .log 6 "Check Projector Server source directory"
  if [ ! -d "$PROJECTOR_SERVER_DIR" ]; then
    .log 4 "Projector Server source directory '$PROJECTOR_SERVER_DIR' doesn't exist"
    .log 6 "Cloning Projector Server sources to '$PROJECTOR_SERVER_DIR'"

    # Clone the Projector Server, stick to the particular version and apply necessary patches if needed
    git clone --quiet "$PROJECTOR_SERVER_GIT" "$PROJECTOR_SERVER_DIR"
    cd "$PROJECTOR_SERVER_DIR" || exit 1
    .log 7 "Current working directory '$(pwd)'"
    .log 6 "Checkout Projector Client to SHA1 '$PROJECTOR_SERVER_SHA1'"
    .log 6 "$(git reset --hard $PROJECTOR_SERVER_SHA1)"
    echo "useLocalProjectorClient=true" >local.properties

    # Apply patches for Projector Server
    if [ -d "$base_dir/patches/projector-server" ]; then
      .log 6 "Applying patches for Projector Server"
      find "$base_dir"/patches/projector-server -name "*.patch" -exec printf '%7s%s\n' "" "Patching Projector Server with '{}'" \; -exec git apply {} \;
    fi
    cd "$base_dir" || exit 1
    .log 7 "Current working directory '$(pwd)'"
  else
    .log 7 "Projector Server source directory '$PROJECTOR_SERVER_DIR' exists"
    cd "$PROJECTOR_SERVER_DIR" || exit 1
    .log 7 "Current working directory '$(pwd)'"

    local projectorServerHead && projectorServerHead=$(git rev-parse HEAD)
    if [ "$PROJECTOR_SERVER_SHA1" == "$projectorServerHead" ]; then
      .log 6 "Projector Server HEAD '$projectorServerHead' is the same as provided in configuration '$PROJECTOR_SERVER_SHA1'"
    else
      read -r -d '' HEAD_CHECK_FAILED <<-EOM
Projector Server HEAD '$projectorServerHead' is different than configured '$PROJECTOR_SERVER_SHA1'.
          In this case build of container image may be unpredictable.
          Consider to remove '$PROJECTOR_SERVER_DIR' directory or move it to a different place and re-run build again.
EOM
      .log 4 "$HEAD_CHECK_FAILED"
      exit 1
    fi

    cd "$base_dir" || exit 1
    .log 7 "Current working directory '$(pwd)'"
  fi
}

checkProjectorSourcesExist() {
  checkProjectorClientSources
  checkProjectorServerSources
}

projectorBuild() {
  .log 6 "Build Projector on localhost"
  cd "$PROJECTOR_SERVER_DIR" || exit 1
  .log 7 "Current working directory '$(pwd)'"
  if [ -f "$CURRENT_PROJECTOR_ASSEMBLY_SYMLINK" ]; then
    .log 7 "Removing symlink '$CURRENT_PROJECTOR_ASSEMBLY_SYMLINK'"
    unlink "$CURRENT_PROJECTOR_ASSEMBLY_SYMLINK"
  fi
  ./gradlew --quiet --console="$PROGRESS" :projector-server:distZip
  find projector-server/build/distributions -type f -name "projector-server-*.zip" -exec ln {} "$CURRENT_PROJECTOR_ASSEMBLY_SYMLINK" \;
  .log 7 "Creating symlink '$CURRENT_PROJECTOR_ASSEMBLY_SYMLINK'"
  cd "$base_dir" || exit 1
  .log 7 "Current working directory '$(pwd)'"
}

runBuild() {
  read -r -d '' PRE_BUILD_SUMMARY <<-EOM
Pre-build container final summary
        Docker build progress configuration: $PROGRESS
        Container name: $CONTAINER_TAG
        IDE package URL: $URL
EOM
  .log 7 "$PRE_BUILD_SUMMARY"
  .log 6 "Build '$CONTAINER_TAG'"

  DOCKER_BUILDKIT=1 \
    docker build \
    --progress="$PROGRESS" \
    -t "$CONTAINER_TAG" \
    -f Dockerfile .
  # shellcheck disable=SC2181
  if [[ $? -eq 0 ]]; then
    .log 6 "Container '$CONTAINER_TAG' successfully built"
  else
    .log 4 "Container build failed"
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
      .log 7 "Adding volume mount '$i'"
    done
  done <<<"$mountVolumes"

  .log 6 "Run container '$containerToStart'"
  docker run --rm -p 8887:8887 "${mountOptions[@]}" -it "$containerToStart" 2>&1 | awk '{print "       "$0}'
}

saveOnBuild() {
  if [ $SAVE_ON_BUILD == true ]; then
    if [ ! -e "$SAVE_ON_BUILD_DIRECTORY" ]; then
      mkdir -p "$SAVE_ON_BUILD_DIRECTORY"
    fi
    local imageOutputName && imageOutputName=$(basename "$URL")
    .log 6 "Saving '$CONTAINER_TAG' to '$SAVE_ON_BUILD_DIRECTORY/$imageOutputName'"
    docker save "$CONTAINER_TAG" -o "$SAVE_ON_BUILD_DIRECTORY"/"$imageOutputName"
    .log 6 "Image '$CONTAINER_TAG' saved to '$SAVE_ON_BUILD_DIRECTORY/$imageOutputName'"
  fi
}

runOnBuild() {
  .log 7 "Check if container should be run after built"
  if [ $RUN_ON_BUILD == true ]; then
    runContainerImage "$CONTAINER_TAG" "$VOLUMES"
  fi
}

prepareStaticFiles() {
  cd "$base_dir" || exit 1
  .log 7 "Current working directory '$(pwd)'"
  if [ -f "$CURRENT_PROJECTOR_STATIC_ASSEMBLY" ]; then
    .log 7 "Removing symlink '$CURRENT_PROJECTOR_STATIC_ASSEMBLY'"
    rm "$CURRENT_PROJECTOR_STATIC_ASSEMBLY"
  fi

  .log 7 "Creating archive for Projector static files '$CURRENT_PROJECTOR_STATIC_ASSEMBLY'"
  tar -czf "$CURRENT_PROJECTOR_STATIC_ASSEMBLY" static
}

prepareAssembly() {
  if [ -z "$URL" ]; then
    .log 7 "Ignoring --tag and --url option"
    checkConfigurationFileExists

    # Run interactive wizard to choose IDE packaging from predefined configuration
    selectPackagingFromPredefinedConfig
  fi
  prepareStaticFiles
  downloadIdePackaging
  checkProjectorSourcesExist
  projectorBuild
}

downloadIdePackaging() {
  if [ ! -e "$IDE_PACKAGING_DIRECTORY" ]; then
    mkdir -p "$IDE_PACKAGING_DIRECTORY"
    .log 7 "Creating directory for storing downloaded IDEs '$IDE_PACKAGING_DIRECTORY'"
  fi

  local packagingOutputName && packagingOutputName=$(basename "$URL")
  cd "$IDE_PACKAGING_DIRECTORY" || exit 1
  .log 7 "Current working directory '$(pwd)'"

  if [ -f "$CURRENT_IDE_PACKAGING_SYMLINK" ]; then
    .log 7 "Removing symlink '$CURRENT_IDE_PACKAGING_SYMLINK'"
    unlink "$CURRENT_IDE_PACKAGING_SYMLINK"
  fi
  # Use --timestamping option to allow local caching
  # Above option doesn't work with -O parameter, so hoping, that base file name wouldn't change
  wget --timestamping "$URL"
  ln "$packagingOutputName" "$CURRENT_IDE_PACKAGING_SYMLINK"
  .log 7 "Creating symlink '$CURRENT_IDE_PACKAGING_SYMLINK'"
  cd "$base_dir" || exit 1
  .log 7 "Current working directory '$(pwd)'"
}

buildContainerImage() {
  .log 7 "Executing build command"
  if [ -z "$CONTAINER_TAG" ] || [ -z "$URL" ]; then
    .log 7 "Ignoring --tag and --url option"
    checkConfigurationFileExists

    # Run interactive wizard to choose IDE packaging from predefined configuration
    selectPackagingFromPredefinedConfig
  fi

  prepareStaticFiles
  downloadIdePackaging
  checkProjectorSourcesExist
  projectorBuild

  runBuild
  saveOnBuild
  runOnBuild
}

printVersion() {
  read -r -d '' VERSION_INFO <<-EOM
$0 - CLI tool for build Projector-based IDE in Eclipse Che
       Revision: $(git show -s --format='%h %s')
EOM
  .log 6 "$VERSION_INFO"
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

OPTS=$(getopt -o 'hvt:up:l:' --longoptions 'help,version,tag:,url:,mount-volumes:,run-on-build,save-on-build,progress:,log-level:,config:,prepare' -u -n "$0" -- "$@")
# shellcheck disable=SC2181
if [[ $? -ne 0 ]]; then
  .log 4 "Failed parsing options."
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
      .log 4 "Unable to parse logging level: $2"
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
        .log 4 "$RUN_MISSING_IMAGE_NAME_MESSAGE"
        exit 1
      fi
      CONTAINER_TO_RUN=$3
      shift 2
      ;;
    '')
      echo "$HELP_SUMMARY"
      exit 1
      ;;
    *)
      .log 4 "$0: '$2' is not a valid command. See '$0 --help'."
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
  if [ ! -e "$BUILD_DIRECTORY" ]; then
    mkdir "$BUILD_DIRECTORY"
    .log 7 "Creating build directory '$BUILD_DIRECTORY'"
  fi
  if [ $PREPARE_ASSEMBLY_ONLY == true ]; then
    prepareAssembly
  else
    buildContainerImage
  fi
elif [ "$COMMAND" == "run" ]; then
  .log 7 "Executing run command"
  runContainerImage "$CONTAINER_TO_RUN" "$VOLUMES"
else
  .log 7 "Found invalid command to execute."
  exit 1
fi
