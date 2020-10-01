#!/bin/sh
#
# Copyright (c) 2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

if [ ! -e /JetBrains/IdeaIC/config/options ]; then
    mkdir -p /JetBrains/IdeaIC/config/options
    echo "Provide preliminary Intellij Idea configuration"
    cp /etc/default/jetbrains/other.xml /JetBrains/IdeaIC/config/options/other.xml
    cp /etc/default/jetbrains/ide.general.xml /JetBrains/IdeaIC/config/options/ide.general.xml
fi
