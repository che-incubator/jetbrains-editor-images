#!/bin/sh
#
# Copyright (c) 2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

# JETBRAINS_PRODUCT: product type used to be activated, can be:
#   ideaIC      - IntelliJ Idea Community
#   ideaIU      - IntelliJ Idea Ultimate
#   WebStorm    - WebStorm

# File with the activation code for offline usage should match the following pattern: /tmp/${JETBRAINS_PRODUCT}.key

set -e

JETBRAINS_PROPERTIES_FILE="/${JETBRAINS_PRODUCT}-${JETBRAINS_PRODUCT_VERSION}/bin/idea.properties"
JETBRAINS_OUTPUT_LICENSE_FILE=""
JETBRAINS_OUTPUT_LICENSE_HEADER="\xFF\xFF\x3C\x00\x63\x00\x65\x00\x72\x00\x74\x00\x69\x00\x66\x00\x69\x00\x63\x00\x61\x00\x74\x00\x65\x00\x2D\x00\x6B\x00\x65\x00\x79\x00\x3E\x00\x0A\x00"
JETBRAINS_LICENSE_FILE="/tmp/${JETBRAINS_PRODUCT}.key"
JETBRAINS_CONFIG_PATH_KEY="idea.config.path"
ACTIVATION_CODE_CONTENTS=""


if [ -z "${JETBRAINS_PRODUCT}" ]; then
    echo "License configuration is not provided. JETBRAINS_PRODUCT variable is not set."
    exit 1
fi

case $JETBRAINS_PRODUCT in
    "ideaIC")
        echo "License configuration for IntelliJ IDEA Community is redundant."
        exit 0
        ;;
    "ideaIU")
        echo "Provide license configuration for IntelliJ IDEA Ultimate."
        JETBRAINS_OUTPUT_LICENSE_FILE="idea.key"
        ;;
    "WebStorm")
        echo "Provide license configuration for WebStorm."
        JETBRAINS_OUTPUT_LICENSE_FILE="webstorm.key"
        ;;
esac

# Work with config path

if [ ! -e "${JETBRAINS_PROPERTIES_FILE}" ]; then
    echo "Configuration file ${JETBRAINS_PROPERTIES_FILE} doesn't exist."
    exit 1
fi

JETBRAINS_CONFIG_PATH=$(grep "$JETBRAINS_CONFIG_PATH_KEY" < "$JETBRAINS_PROPERTIES_FILE" | cut -d'=' -f2)

echo "Found configuration path '${JETBRAINS_CONFIG_PATH}'."

if [ ! -e "${JETBRAINS_CONFIG_PATH}" ]; then
    echo "Configuration path ${JETBRAINS_CONFIG_PATH} doesn't exist."
    exit 1
fi

echo "Configuration path '${JETBRAINS_CONFIG_PATH}' exists."

if [ -e "${JETBRAINS_CONFIG_PATH}/${JETBRAINS_OUTPUT_LICENSE_FILE}" ]; then
    rm "${JETBRAINS_CONFIG_PATH}/${JETBRAINS_OUTPUT_LICENSE_FILE}"
fi

# Work with offline activation code

if [ ! -e "${JETBRAINS_LICENSE_FILE}" ]; then 
    echo "File with the activation code for offline usage doesn't exist."
    exit 1
fi

echo "Found file with the activation code for offline usage '${JETBRAINS_LICENSE_FILE}'."

ACTIVATION_CODE_CONTENTS=$(printf "%s\n" "$(cat "${JETBRAINS_LICENSE_FILE}")" | sed 's/./ &/g')

touch "${JETBRAINS_CONFIG_PATH}/${JETBRAINS_OUTPUT_LICENSE_FILE}"

echo "Create license file '${JETBRAINS_CONFIG_PATH}/${JETBRAINS_OUTPUT_LICENSE_FILE}'."

printf "%b" ${JETBRAINS_OUTPUT_LICENSE_HEADER} > "${JETBRAINS_CONFIG_PATH}/${JETBRAINS_OUTPUT_LICENSE_FILE}"

echo "License header has been successfully written to the file."

for ch in ${ACTIVATION_CODE_CONTENTS}; do
    printf "%s" "$ch" >> "${JETBRAINS_CONFIG_PATH}/${JETBRAINS_OUTPUT_LICENSE_FILE}"
    printf "\x00" >> "${JETBRAINS_CONFIG_PATH}/${JETBRAINS_OUTPUT_LICENSE_FILE}"
done

echo "License content has been successfully written to the file."
