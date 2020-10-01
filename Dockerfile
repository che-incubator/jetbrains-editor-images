# Copyright (c) 2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

FROM fedora:32
# which is used by novnc to find websockify
RUN yum install -y tigervnc-server supervisor wget java-11-openjdk-devel novnc fluxbox which

RUN mkdir /ideaIC-2020.2.2 && wget -qO- https://download.jetbrains.com/idea/ideaIC-2020.2.2.tar.gz | tar -zxv --strip-components=1 -C /ideaIC-2020.2.2 && \
    mkdir -p /JetBrains/IdeaIC && \
    mkdir /etc/default/jetbrains && \
    for f in "/JetBrains" "/ideaIC-2020.2.2" "/etc/passwd" "/etc/default/jetbrains"; do \
      echo "Changing permissions on ${f}" && chgrp -R 0 ${f} && \
      chmod -R g+rwX ${f}; \
    done

COPY --chown=0:0 etc/entrypoint.sh /entrypoint.sh
COPY --chown=0:0 etc/prevent-idle-timeout.sh /
COPY --chown=0:0 etc/preliminary-configuration.sh /
COPY --chown=0:0 etc/default/*.xml /etc/default/jetbrains/
COPY --chown=0:0 etc/supervisord.conf /etc/supervisord.conf
# disable toolbar + use another theme
COPY --chown=0:0 etc/fluxbox /home/user/.fluxbox/init
# no security/ custom geometry
COPY etc/tigervnc-config /etc/tigervnc/vncserver-config-mandatory
# Set permissions on /etc/passwd and /home to allow arbitrary users to write
COPY idea.properties /JetBrains/idea.properties
RUN mkdir -p /home/user && chgrp -R 0 /home && chmod -R g=u /etc/passwd /etc/group /home && chmod +x /entrypoint.sh && chmod +x /prevent-idle-timeout.sh && chmod +x /preliminary-configuration.sh
USER 10001
ENV HOME=/home/user
ENV IDEA_PROPERTIES=/JetBrains/idea.properties
WORKDIR /projects
ENTRYPOINT [ "/entrypoint.sh" ]
CMD ["tail", "-f", "/dev/null"]
