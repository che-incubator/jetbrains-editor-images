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
#   $ ./projector.sh build [OPTIONS]

# Stage 1. Prepare JetBrains IDE with Projector.
#   Requires the following assets:
#       * asset-ide-packaging.tar.gz - IDE packaging downloaded previously;
#       * asset-projector-server-assembly.zip - Projector Server assembly;
#       * asset-static-assembly.tar.gz - archived `static/` directory.
# https://access.redhat.com/containers/?tab=tags#/registry.access.redhat.com/ubi8-minimal
FROM registry.access.redhat.com/ubi8-minimal:8.4-212 as projectorAssembly
ENV PROJECTOR_DIR /projector
COPY asset-ide-packaging.tar.gz /tmp/ide-unpacked/
COPY asset-projector-server-assembly.zip $PROJECTOR_DIR/
COPY asset-static-assembly.tar.gz $PROJECTOR_DIR/
RUN set -ex \
    && microdnf install -y --nodocs findutils tar gzip unzip \
    && cd /tmp/ide-unpacked \
    && tar xf asset-ide-packaging.tar.gz \
    && rm asset-ide-packaging.tar.gz \
    && find . -maxdepth 1 -type d -name * -exec mv {} $PROJECTOR_DIR/ide \; \
    && cd $PROJECTOR_DIR \
    && rm -rf /tmp/ide-unpacked \
    && unzip asset-projector-server-assembly.zip \
    && rm asset-projector-server-assembly.zip \
    && find . -maxdepth 1 -type d -name projector-server-* -exec mv {} projector-server \; \
    && mv projector-server ide/projector-server \
    && chmod 644 ide/projector-server/lib/* \
    && tar -xf asset-static-assembly.tar.gz \
    && rm asset-static-assembly.tar.gz \
    && mv static/* . \
    && rm -rf static \
    && mv ide-projector-launcher.sh ide/bin \
    && find . -exec chgrp 0 {} \; -exec chmod g+rwX {} \; \
    && find . -name "*.sh" -exec chmod +x {} \; \
    && mv projector-user/.config .default \
    && rm -rf projector-user

# Stage 2. Build the main image with necessary environment for running Projector
#   Doesn't require to be a desktop environment. Projector runs in headless mode.
# https://access.redhat.com/containers/?tab=tags#/registry.access.redhat.com/ubi8-minimal
FROM registry.access.redhat.com/ubi8-minimal:8.4-212
ENV PROJECTOR_USER_NAME projector-user
ENV PROJECTOR_DIR /projector
ENV HOME /home/$PROJECTOR_USER_NAME
ENV PROJECTOR_CONFIG_DIR $HOME/.config
COPY install-platform-dependencies.sh /tmp
RUN set -ex \
    && microdnf install -y --nodocs \
    jq shadow-utils wget git nss procps findutils which socat \
    # Java 11 support
    java-11-openjdk-devel \
    # Python support
    python2 python39 \
    # Packages needed for AWT.
    libXext libXrender libXtst libXi libX11-xcb mesa-libgbm libdrm freetype \
    # Arch specific installs of libsecret and libsecret-devel (required by JetBrains products)
    && chmod +x /tmp/install-platform-dependencies.sh && /tmp/install-platform-dependencies.sh && rm -f /tmp/install-platform-dependencies.sh \
    # create user configuration
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

USER $PROJECTOR_USER_NAME
WORKDIR /projects
EXPOSE 8887
CMD $PROJECTOR_DIR/entrypoint.sh
