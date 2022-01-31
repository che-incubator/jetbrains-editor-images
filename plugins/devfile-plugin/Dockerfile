# Copyright (c) 2021 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

FROM registry.access.redhat.com/ubi8/ubi:8.5-214

RUN yum install java-11-openjdk-devel unzip -y --nodocs
COPY . /devfile-plugin-compilation

WORKDIR /devfile-plugin-compilation
RUN ./gradlew build

WORKDIR /devfile-plugin-compilation/build/distributions
RUN unzip devfile-plugin-1.0-SNAPSHOT.zip && mv devfile-plugin /
