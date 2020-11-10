# Copyright (c) 2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

FROM fedora:32

# Product name:
#   ideaIC    - IntelliJ Idea Community
#   ideaIU    - IntelliJ Idea Ultimate
#   WebStorm  - WebStorm
ARG PRODUCT_NAME="ideaIC"

# Product version
ARG PRODUCT_VERSION="2020.2.3"

# Product url constructs based on PRODUCT_NAME and PRODUCT_VERSION:
#   https://download.jetbrains.com/idea/ideaIC-2020.2.3.tar.gz
#   https://download.jetbrains.com/idea/ideaIU-2020.2.3.tar.gz
#   https://download.jetbrains.com/webstorm/WebStorm-2020.2.3.tar.gz

ARG BASE_MOUNT_FOLDER="/JetBrains"

# which is used by novnc to find websockify
RUN yum install -y tigervnc-server supervisor wget java-11-openjdk-devel novnc fluxbox git which

RUN mkdir /${PRODUCT_NAME}-${PRODUCT_VERSION} && \
    case ${PRODUCT_NAME} in \
        "ideaIC"|"ideaIU") \
            wget -qO- https://download.jetbrains.com/idea/${PRODUCT_NAME}-${PRODUCT_VERSION}.tar.gz | tar -zxv --strip-components=1 -C /${PRODUCT_NAME}-${PRODUCT_VERSION} && \
            ln -s /${PRODUCT_NAME}-${PRODUCT_VERSION}/bin/idea.sh /opt/run-ide.sh \
            ;; \
        "WebStorm") \
            wget -qO- https://download.jetbrains.com/webstorm/${PRODUCT_NAME}-${PRODUCT_VERSION}.tar.gz | tar -zxv --strip-components=1 -C /${PRODUCT_NAME}-${PRODUCT_VERSION} && \
            ln -s /${PRODUCT_NAME}-${PRODUCT_VERSION}/bin/webstorm.sh /opt/run-ide.sh \
            ;; \
    esac && \
    mkdir -p ${BASE_MOUNT_FOLDER}/${PRODUCT_NAME} && \
    mkdir /etc/default/jetbrains && \
    for f in "${BASE_MOUNT_FOLDER}" "/${PRODUCT_NAME}-${PRODUCT_VERSION}" "/etc/passwd" "/etc/default/jetbrains"; do \
      echo "Changing permissions on ${f}" && chgrp -R 0 ${f} && \
      chmod -R g+rwX ${f}; \
    done && \
    echo "idea.config.path=${BASE_MOUNT_FOLDER}/${PRODUCT_NAME}/config" > /${PRODUCT_NAME}-${PRODUCT_VERSION}/bin/idea.properties && \
    echo "idea.system.path=${BASE_MOUNT_FOLDER}/${PRODUCT_NAME}/caches" >> /${PRODUCT_NAME}-${PRODUCT_VERSION}/bin/idea.properties && \
    echo "idea.plugins.path=${BASE_MOUNT_FOLDER}/${PRODUCT_NAME}/plugins" >> /${PRODUCT_NAME}-${PRODUCT_VERSION}/bin/idea.properties && \
    echo "idea.log.path=${BASE_MOUNT_FOLDER}/${PRODUCT_NAME}/logs" >> /${PRODUCT_NAME}-${PRODUCT_VERSION}/bin/idea.properties

# Copy fluxbox configuration
COPY --chown=0:0 config/fluxbox /home/user/.fluxbox/init

# Copy predefined configs
COPY --chown=0:0 config/etc /etc/

# Copy sh scripts
COPY --chown=0:0 scripts/*.sh /opt/

RUN mkdir -p /home/user && \
    chgrp -R 0 /home && \
    chmod -R g=u /etc/passwd /etc/group /home && \
    chmod +x /opt/*.sh

USER 10001

ENV HOME=/home/user
ENV JETBRAINS_PRODUCT=${PRODUCT_NAME}
ENV JETBRAINS_PRODUCT_VERSION=${PRODUCT_VERSION}
ENV JETBRAINS_BASE_MOUNT_FOLDER=${BASE_MOUNT_FOLDER}
WORKDIR /projects
ENTRYPOINT [ "/opt/entrypoint.sh" ]
CMD ["tail", "-f", "/dev/null"]
