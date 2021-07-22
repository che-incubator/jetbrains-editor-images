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
#   Requires build/ide-packaging which should point to the ide packaging downloaded
#   previously, usually tar.gz archive. Also requires build/projector-server-assembly
#   which points to the built Projector Server assembly.
FROM registry.access.redhat.com/ubi8-minimal:8.4-205 as projectorAssembly
ENV PROJECTOR_DIR /projector
ADD build/ide-packaging /tmp/ide-unpacked
ADD build/projector-server-assembly $PROJECTOR_DIR/projector-server.zip
ADD static $PROJECTOR_DIR
RUN set -ex \
    && microdnf install -y --nodocs findutils tar gzip unzip \
    && cd /tmp/ide-unpacked \
    && find . -maxdepth 1 -type d -name * -exec mv {} $PROJECTOR_DIR/ide \; \
    && cd $PROJECTOR_DIR \
    && rm -rf /tmp/ide-unpacked \
    && unzip projector-server.zip \
    && rm projector-server.zip \
    && find . -maxdepth 1 -type d -name projector-server-* -exec mv {} projector-server \; \
    && mv projector-server ide/projector-server \
    && chmod 644 ide/projector-server/lib/* \
    && mv ide-projector-launcher.sh ide/bin \
    && find . -exec chgrp 0 {} \; -exec chmod g+rwX {} \; \
    && find . -name "*.sh" -exec chmod +x {} \; \
    && mv projector-user/.config .default \
    && rm -rf projector-user

# Stage 2. Build the main image with necessary environment for running Projector
#   Doesn't require to be a desktop environment. Projector runs in headless mode.
FROM registry.access.redhat.com/ubi8-minimal:8.4-205
ENV PROJECTOR_USER_NAME projector-user
ENV PROJECTOR_DIR /projector
ENV HOME /home/$PROJECTOR_USER_NAME
ENV PROJECTOR_CONFIG_DIR $HOME/.config
RUN set -ex \
    && microdnf install -y --nodocs \
    shadow-utils wget git nss procps findutils which socat \
    # Packages required by JetBrains products.
    libsecret jq \
    # Java 11 support
    java-11-openjdk-devel \
    # Python support
    python2 python39 \
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

USER $PROJECTOR_USER_NAME
WORKDIR /projects
EXPOSE 8887
CMD $PROJECTOR_DIR/entrypoint.sh
