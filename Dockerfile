# Copyright (c) 2021 Red Hat, Inc.
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

# To build the cuurent Dockerfile there is the following flow:
#   $ ./clone-projector.sh
#       Clones the Projector Client and Projector Server sources.
#   $ ./build-container.sh [containerName [ideDownloadUrl]] or ./build-container-dev.sh [containerName [ideDownloadUrl]]
#       Perform build Docker container.
#       build-container.sh takes Projector Client and Projector Server sources and run Gradle build inside Stage 1.
#       build-container-dev.sh relies on built Projector Server on the host. Useful if doesn't require to build in Stage 1.

# Stage 1. Prepare JetBrains IDE with Projector.
#   1. Downloads JetBrains IDE packaging by given downloadUrl build argument.
#   2. If buildGradle build argument is set to false, then consumes built Projector assembly from the host.
#       2.1 Otherwise starts Gradle build of Projector Server and Projector Client.
#   3. Copies static files to the Projector assembly (entrypoint, launcher, configuration).
FROM registry.access.redhat.com/ubi8-minimal:8.3-298 as projectorAssembly
ENV PROJECTOR_DIR /projector
ENV JAVA_HOME /usr/lib/jvm/java-11
ARG downloadUrl
ARG buildGradle
ADD projector-client $PROJECTOR_DIR/projector-client
ADD projector-server $PROJECTOR_DIR/projector-server
RUN microdnf install -y --nodocs findutils tar gzip unzip java-11-openjdk-devel
WORKDIR $PROJECTOR_DIR/projector-server
RUN if [ "$buildGradle" = "true" ]; then ./gradlew clean; else echo "Skipping gradle build"; fi \
    && if [ "$buildGradle" = "true" ]; then ./gradlew --console=plain :projector-server:distZip; else echo "Skipping gradle build"; fi \
    && cd projector-server/build/distributions \
    && find . -maxdepth 1 -type f -name projector-server-*.zip -exec mv {} projector-server.zip \;
WORKDIR /downloads
RUN curl -SL $downloadUrl | tar -xz \
    && find . -maxdepth 1 -type d -name * -exec mv {} $PROJECTOR_DIR/ide \;
WORKDIR $PROJECTOR_DIR
RUN set -ex \
    && cp projector-server/projector-server/build/distributions/projector-server.zip . \
    && rm -rf projector-client \
    && rm -rf projector-server \
    && unzip projector-server.zip \
    && rm projector-server.zip \
    && find . -maxdepth 1 -type d -name projector-server-* -exec mv {} projector-server \; \
    && mv projector-server ide/projector-server \
    && chmod 644 ide/projector-server/lib/*
ADD jetbrains-editor-images/static $PROJECTOR_DIR
RUN set -ex \
    && mv ide-projector-launcher.sh ide/bin \
    && find . -exec chgrp 0 {} \; -exec chmod g+rwX {} \; \
    && find . -name "*.sh" -exec chmod +x {} \; \
    && mv projector-user/.config .default \
    && rm -rf projector-user

# Stage 2. Build the main image with necessary environment for running Projector
#   Doesn't require to be a desktop environment. Projector runs in headless mode.
FROM registry.access.redhat.com/ubi8-minimal:8.3-298
ENV PROJECTOR_USER_NAME projector-user
ENV PROJECTOR_DIR /projector
ENV HOME /home/$PROJECTOR_USER_NAME
ENV PROJECTOR_CONFIG_DIR $HOME/.config
RUN set -ex \
    && microdnf install -y --nodocs \
    shadow-utils wget git nss procps findutils which socat \
    # Packages required by JetBrains products.
    libsecret jq java-11-openjdk-devel python2 python39 \
    # Packages needed for AWT.
    libXext libXrender libXtst libXi libX11-xcb mesa-libgbm libdrm freetype \
    && adduser -r -u 1002 -G root -d $HOME -m -s /bin/sh $PROJECTOR_USER_NAME \
    && echo "%wheel ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers \
    && mkdir /projects \
    && for f in "${HOME}" "/etc/passwd" "/etc/group /projects"; do\
            chgrp -R 0 ${f} && \
            chmod -R g+rwX ${f}; \
       done \
    && cat /etc/passwd | sed s#root:x.*#root:x:\${USER_ID}:\${GROUP_ID}::\${HOME}:/bin/bash#g > ${HOME}/passwd.template \
    && cat /etc/group | sed s#root:x:0:#root:x:0:0,\${USER_ID}:#g > ${HOME}/group.template \
    # Change permissions to allow editing of files for openshift user
    && find $HOME -exec chgrp 0 {} \; -exec chmod g+rwX {} \;

COPY --chown=$PROJECTOR_USER_NAME:root --from=projectorAssembly $PROJECTOR_DIR $PROJECTOR_DIR

ENV DEV_MODE=false
USER $PROJECTOR_USER_NAME
WORKDIR /projects
EXPOSE 8887
CMD $PROJECTOR_DIR/entrypoint.sh
