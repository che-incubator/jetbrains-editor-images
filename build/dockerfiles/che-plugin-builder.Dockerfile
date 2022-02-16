# Copyright (c) 2022 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

FROM registry.access.redhat.com/ubi8/ubi:8.5-214 as plugin-builder

RUN yum install java-11-openjdk-devel unzip -y --nodocs
RUN mkdir /plugin && mkdir /plugin-assembly

COPY /che-plugin /plugin

RUN mkdir -p ~/.gradle && echo "org.gradle.daemon=false" >> ~/.gradle/gradle.properties

WORKDIR /plugin

RUN ./gradlew clean
RUN ./gradlew build

RUN find build/distributions -type f -name "che-plugin-*.zip" -exec mv {} "/plugin-assembly/asset-che-plugin-assembly.zip" \;

FROM registry.access.redhat.com/ubi8/ubi-micro:8.5-744
WORKDIR /plugin
COPY --from=plugin-builder /plugin-assembly/asset-che-plugin-assembly.zip asset-che-plugin-assembly.zip
