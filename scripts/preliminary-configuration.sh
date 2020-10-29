#!/bin/sh
#
# Copyright (c) 2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

if [ ! -e ${JETBRAINS_BASE_MOUNT_FOLDER}/${JETBRAINS_PRODUCT}/config/options ]; then
    mkdir -p ${JETBRAINS_BASE_MOUNT_FOLDER}/${JETBRAINS_PRODUCT}/config/options
    echo "Provide preliminary configuration for JetBrains product."
    cp /etc/default/jetbrains/other.xml ${JETBRAINS_BASE_MOUNT_FOLDER}/${JETBRAINS_PRODUCT}/config/options/other.xml
    cp /etc/default/jetbrains/ide.general.xml ${JETBRAINS_BASE_MOUNT_FOLDER}/${JETBRAINS_PRODUCT}/config/options/ide.general.xml
fi
