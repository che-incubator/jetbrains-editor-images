#!/bin/bash -e
# Copyright (c) 2021 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

# Arch specific installs of libsecret and libsecret-devel (required by JetBrains products)
if [[ $(uname -m) == "s390x" ]]; then
    cd /tmp
    curl -sSLO https://rpmfind.net/linux/fedora-secondary/releases/34/Everything/s390x/os/Packages/l/libsecret-0.20.4-2.fc34.s390x.rpm
    curl -sSLO https://rpmfind.net/linux/fedora-secondary/releases/34/Everything/s390x/os/Packages/l/libsecret-devel-0.20.4-2.fc34.s390x.rpm
    microdnf install -y --nodocs glib2-devel pcre-cpp pcre-devel pcre-utf16 pcre-utf32
    rpm --excludedocs -i /tmp/libsecret-*.rpm
    rm -f /tmp/libsecret*.rpm
elif [[ $(uname -m) == "ppc64le" ]]; then 
    cd /tmp
    curl -sSLO https://rpmfind.net/linux/centos/8-stream/BaseOS/ppc64le/os/Packages/libsecret-devel-0.18.6-1.el8.ppc64le.rpm
    microdnf install -y --nodocs libsecret
    rpm --excludedocs -i /tmp/libsecret-*.rpm
    rm -f /tmp/libsecret*.rpm
elif [[ $(uname -m) == "x86_64" ]]; then 
    # NOTE: in Brew, must also install libsecret-devel but that's not available upstream
    microdnf install -y --nodocs libsecret
fi
