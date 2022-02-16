# Copyright (c) 2022 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

FROM registry.access.redhat.com/ubi8-minimal:8.5-230

RUN microdnf install wget -y --nodocs
RUN mkdir /ide

WORKDIR /ide
ARG URL
RUN packagingOutputName=$(basename "$URL") && \
    # Use --timestamping option to allow local caching
    # Above option doesn't work with -O parameter, so hoping, that base file name wouldn't change
    wget --timestamping "$URL" && \
    mv "$packagingOutputName" "asset-ide-packaging.tar.gz" \
