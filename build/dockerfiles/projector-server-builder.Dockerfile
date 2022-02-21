# Copyright (c) 2022 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
FROM registry.access.redhat.com/ubi8/ubi:8.5-214 as projector-builder

RUN yum install java-11-openjdk unzip -y --nodocs
RUN mkdir /projector && mkdir /projector-assembly

COPY /projector-client /projector/projector-client
COPY /projector-server /projector/projector-server

RUN echo "useLocalProjectorClient=true" > /projector/projector-server/local.properties
RUN mkdir -p ~/.gradle && echo "org.gradle.daemon=false" >> ~/.gradle/gradle.properties

WORKDIR /projector/projector-server

RUN ./gradlew clean
RUN ./gradlew :projector-server:distZip

RUN find projector-server/build/distributions -type f -name "projector-server-*.zip" -exec mv {} "/projector-assembly/asset-projector-server-assembly.zip" \;

FROM registry.access.redhat.com/ubi8/ubi-micro:8.5-744
WORKDIR /projector
COPY --from=projector-builder /projector-assembly/asset-projector-server-assembly.zip asset-projector-server-assembly.zip
