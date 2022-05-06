#!/bin/bash
#
# Copyright (c) 2022 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

#
# Copyright 2019-2020 JetBrains s.r.o.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

bash "$PROJECTOR_ASSEMBLY_DIR"/default-configuration-provider.sh

# provide necessary environment variables
echo "export JAVA_HOME=/usr/lib/jvm/java-11" >> "${HOME}"/.bashrc

# offline activation key registration
# depending on the product code id taken from $PROJECTOR_ASSEMBLY_DIR/ide/product-info.json looks for:
#   /tmp/idea.key
#   /tmp/webstorm.key
#   /tmp/pycharm.key
#   /tmp/phpstorm.key
#   /tmp/goland.key
keyName=""
case $(jq -r .productCode < "$PROJECTOR_ASSEMBLY_DIR"/ide/product-info.json) in
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

# search for IDE runner sh file:
THIS_FILE_NAME=$(basename "$0")

ideRunnerCandidates=($(grep -lr --include=*.sh com.intellij.idea.Main .))

# remove this file from candidates:
for i in "${!ideRunnerCandidates[@]}"; do
    if [[ ${ideRunnerCandidates[i]} = *$THIS_FILE_NAME* ]]; then
        unset 'ideRunnerCandidates[i]'
    elif [[ ${ideRunnerCandidates[i]} = *"projector"* ]]; then
        unset 'ideRunnerCandidates[i]'
    elif [[ ${ideRunnerCandidates[i]} = *"game-tools.sh" ]]; then
        unset 'ideRunnerCandidates[i]'
    fi
done

if [[ ${#ideRunnerCandidates[@]} != 1 ]]; then
    echo "Can't find a single candidate to be IDE runner script so can't select a single one:"
    echo ${ideRunnerCandidates[*]}
    exit 1
fi

ideRunnerCandidate=${ideRunnerCandidates[@]}
ideRunnerWithoutPrefix=${ideRunnerCandidate/"./"/""}
IDE_RUN_FILE_NAME=${ideRunnerWithoutPrefix/".sh"/""}
echo "Found IDE: $IDE_RUN_FILE_NAME"

cp "$IDE_RUN_FILE_NAME.sh" "$IDE_RUN_FILE_NAME-projector.sh"

# change
# classpath "$CLASSPATH"
# to
# classpath "$CLASSPATH:$IDE_HOME/projector-server/lib/*"
sed -i 's+classpath "$CLASSPATH"+classpath "$CLASSPATH:$IDE_HOME/projector-server/lib/*"+g' "$IDE_RUN_FILE_NAME-projector.sh"

# update classpath for 2022
sed -i 's+classpath "$CLASS_PATH"+classpath "$CLASS_PATH:$IDE_HOME/projector-server/lib/*"+g' "$IDE_RUN_FILE_NAME-projector.sh"

# change
# com.intellij.idea.Main
# to
# -Dorg.jetbrains.projector.server.classToLaunch=com.intellij.idea.Main org.jetbrains.projector.server.ProjectorLauncher
sed -i 's+com.intellij.idea.Main+-Drsch.send.usage.stat=false -Djb.consents.confirmation.enabled=false -Didea.suppress.statistics.report=true -Dide.browser.jcef.enabled=false -Dorg.jetbrains.projector.server.classToLaunch=com.intellij.idea.Main org.jetbrains.projector.server.ProjectorLauncher+g' "$IDE_RUN_FILE_NAME-projector.sh"

pathCandidate=""

# che specific configuration to auto open projects
if [ -x "$(command -v yq)" ] && [ -f "$DEVWORKSPACE_FLATTENED_DEVFILE" ] && [ -d "$PROJECTS_ROOT" ]; then
  configuredProjectNames=$(yq -r ".projects[]?.name" < "$DEVWORKSPACE_FLATTENED_DEVFILE")
  if [ "$(echo -n "$configuredProjectNames" | grep "^.*$" -c)" == 1 ]; then
    pathCandidate="$PROJECTS_ROOT/$configuredProjectNames"
  else
    pathCandidate="$PROJECTS_ROOT"
  fi
fi

if [ -n "$pathCandidate" ] && [ -d "$pathCandidate" ]; then
  bash "$IDE_RUN_FILE_NAME-projector.sh" "nosplash" "$pathCandidate"
else
  bash "$IDE_RUN_FILE_NAME-projector.sh" "nosplash"
fi

rm "$IDE_RUN_FILE_NAME-projector.sh"

