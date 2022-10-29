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
# https://access.redhat.com/containers/?tab=tags#/registry.access.redhat.com/ubi8
FROM registry.access.redhat.com/ubi8/ubi:8.5-239.1651231664 as ubi-builder
COPY --chown=0:0 asset-required-rpms.txt /tmp/asset-required-rpms.txt

RUN mkdir -p /mnt/rootfs
RUN yum install unzip -y --nodocs && \
    yum install --installroot /mnt/rootfs \
        brotli libstdc++ coreutils glibc-minimal-langpack \
        jq shadow-utils wget git nss procps findutils which socat \
        java-11-openjdk-devel \
        python2 python39 \
        libXext libXrender libXtst libXi \
        $(cat /tmp/asset-required-rpms.txt) \
            --releasever 8 --setopt install_weak_deps=false --nodocs -y && \
    yum --installroot /mnt/rootfs clean all
RUN rm -rf /mnt/rootfs/var/cache/* /mnt/rootfs/var/log/dnf* /mnt/rootfs/var/log/yum.*

RUN mkdir -p /mnt/rootfs/projects && mkdir -p /mnt/rootfs/home/projector && mkdir -p /mnt/rootfs/projector && \
    cat /mnt/rootfs/etc/passwd | sed s#root:x.*#root:x:\${USER_ID}:\${GROUP_ID}::\${HOME}:/bin/bash#g > /mnt/rootfs/home/projector/.passwd.template  && \
    cat /mnt/rootfs/etc/group | sed s#root:x:0:#root:x:0:0,\${USER_ID}:#g > /mnt/rootfs/home/projector/.group.template

WORKDIR /mnt/rootfs/projector

COPY --chown=0:0 asset-ide-packaging.tar.gz .
RUN tar -xf asset-ide-packaging.tar.gz && rm asset-ide-packaging.tar.gz && \
    find . -maxdepth 1 -type d -name * -exec mv {} ide \;

COPY --chown=0:0 asset-projector-server-assembly.zip .
RUN unzip asset-projector-server-assembly.zip && rm asset-projector-server-assembly.zip && \
    find . -maxdepth 1 -type d -name projector-server-* -exec mv {} ide/projector-server \;

COPY --chown=0:0 asset-static-assembly.tar.gz .
RUN tar -xf asset-static-assembly.tar.gz && rm asset-static-assembly.tar.gz && \
    chown -R 0:0 static && \
    mv static/* . && rm -rf static && \
    chmod +x *.sh && \
    mv ide-projector-launcher.sh ide/bin

COPY --chown=0:0 asset-che-plugin-assembly.zip .
RUN unzip asset-che-plugin-assembly.zip && rm asset-che-plugin-assembly.zip && \
    find . -maxdepth 1 -type d -name che-plugin -exec mv {} ide/plugins/che-plugin \;

COPY --chown=0:0 asset-machine-exec ide/bin/machine-exec

RUN for f in "/mnt/rootfs/bin/" "/mnt/rootfs/home/projector" "/mnt/rootfs/etc/passwd" "/mnt/rootfs/etc/group" "/mnt/rootfs/projects" "/mnt/rootfs/projector/ide/bin" ; do\
           chgrp -R 0 ${f} && \
           chmod -R g+rwX ${f}; \
    done

RUN rm /mnt/rootfs/etc/hosts

# Stage 2. Copy from build environment Projector assembly to the runtime. Projector runs in headless mode.
# https://access.redhat.com/containers/?tab=tags#/registry.access.redhat.com/ubi8-minimal
FROM registry.access.redhat.com/ubi8-minimal:8.6-994
ENV HOME=/home/projector
ENV PROJECTOR_ASSEMBLY_DIR /projector
ENV PROJECTOR_CONFIG_DIR $HOME/.jetbrains
COPY --from=ubi-builder /mnt/rootfs/ /
USER 1001
EXPOSE 8887
ENTRYPOINT $PROJECTOR_ASSEMBLY_DIR/entrypoint.sh
