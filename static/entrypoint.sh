#!/bin/bash
#
# Copyright (c) 2021 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

# Ensure $HOME exists when starting
if [ ! -d "${HOME}" ]; then
  mkdir -p "${HOME}"
fi

# Setup $PS1 for a consistent and reasonable prompt
if [ -w "${HOME}" ] && [ ! -f "${HOME}"/.bashrc ]; then
  echo "PS1='\s-\v \w \$ '" > "${HOME}"/.bashrc
fi

# Add current (arbitrary) user to /etc/passwd and /etc/group
if ! whoami &> /dev/null; then
  if [ -w /etc/passwd ]; then
    echo "${USER_NAME:-user}:x:$(id -u):0:${USER_NAME:-user} user:${HOME}:/bin/bash" >> /etc/passwd
    echo "${USER_NAME:-user}:x:$(id -u):" >> /etc/group
  fi
fi

# Prevent workspace from shutdown
preventWorkspaceFromShutdown() {
  caCert="/tmp/che/secret/ca.crt"

  if [ -z "${CHE_API_INTERNAL}" ] || [ -z "${CHE_WORKSPACE_ID}" ]; then
      echo "CHE_API_INTERNAL or CHE_WORKSPACE_ID is not configured in environment."
      exit 1
  fi

  curlOpts=

  if [ -e $caCert ]; then
      curlOpts="$curlOpts --cacert /tmp/che/secret/ca.crt"
  fi

  if [ -n "$CHE_MACHINE_TOKEN" ]; then
      curlOpts="$curlOpts -H \"Authorization: Bearer ${CHE_MACHINE_TOKEN}\""
  fi

  requestUrl="${CHE_API_INTERNAL}/activity/${CHE_WORKSPACE_ID}"

  while true; do
      echo "$(date) - Perform request: 'curl -X PUT $curlOpts $requestUrl'."
      eval curl -X PUT "${curlOpts}" "${requestUrl}"
      echo "$(date) - Sleep for 1 minute."

      sleep 60
  done
}

if [ -n "$CHE_WORKSPACE_ID" ]; then
  echo "Prevent workspace from unexpected shutdown"
  preventWorkspaceFromShutdown &
fi

# Copy default configuration if home directory is mounted
if [ ! -d "$PROJECTOR_CONFIG_DIR" ]; then
  echo "Copy default configuration"
  cp -rp "$PROJECTOR_DIR"/.default "$PROJECTOR_CONFIG_DIR"
fi

# Setup necessary envrionment variables
echo "export JAVA_HOME=/usr/lib/jvm/java-11" >> "${HOME}"/.bashrc

# Detect IDE's product code
productCode=$(jq -r .productCode < "$PROJECTOR_DIR"/ide/product-info.json)

# Offline activation key registration
# Depending on the product code id taken from $PROJECTOR_DIR/ide/product-info.json looks for:
#   /tmp/idea.key
#   /tmp/webstorm.key
#   /tmp/pycharm.key
#   /tmp/phpstorm.key
#   /tmp/goland.key
keyName=""
case $productCode in
  "IU")
      keyName="idea.key"
      ;;
  "WS")
      keyName="webstorm.key"
      ;;
  "PY")
      keyName="pycharm.key"
      ;;
  "PS")
      keyName="phpstorm.key"
      ;;
  "GO")
      keyName="goland.key"
      ;;
esac
if [ -n "$keyName" ] && [ -e "/tmp/$keyName" ]; then
  echo "Found offline activation code: /tmp/$keyName"
  offlineActivationKeyContent=$(printf "%s\n" "$(cat /tmp/"$keyName")" | sed 's/./ &/g')
  outputLicenseFilePath="$PROJECTOR_CONFIG_DIR/config/$keyName"
  touch "$outputLicenseFilePath"
  licenseHeader="\xFF\xFF\x3C\x00\x63\x00\x65\x00\x72\x00\x74\x00\x69\x00\x66\x00\x69\x00\x63\x00\x61\x00\x74\x00\x65\x00\x2D\x00\x6B\x00\x65\x00\x79\x00\x3E\x00\x0A\x00"
  printf "%b" $licenseHeader > "$outputLicenseFilePath"
  for ch in $offlineActivationKeyContent; do
    printf "%s" "$ch" >> "$outputLicenseFilePath"
    printf "\x00" >> "$outputLicenseFilePath"
  done
  echo "Offline activation code provided in $outputLicenseFilePath"
fi

# Overwrite default configuration paths
cat <<EOT >> "$PROJECTOR_DIR"/ide/bin/idea.properties
idea.config.path=$PROJECTOR_CONFIG_DIR/config
idea.system.path=$PROJECTOR_CONFIG_DIR/caches
idea.plugins.path=$PROJECTOR_CONFIG_DIR/plugins
idea.log.path=$PROJECTOR_CONFIG_DIR/logs
EOT

# Start projector launcher
cd /projector/ide/bin || exit

./ide-projector-launcher.sh
