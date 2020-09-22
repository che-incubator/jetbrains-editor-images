# Copyright (c) 2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

FROM debian:10.5

RUN echo "deb http://ftp.debian.org/debian/ testing main contrib non-free" >> /etc/apt/sources.list && \
    apt-get update && apt-get install -y git supervisor tightvncserver wget openjdk-11-jdk ttf-mscorefonts-installer vnc4server novnc fluxbox && apt-get clean
RUN mkdir /ideaIC-2020.2.2 && wget -qO- https://download.jetbrains.com/idea/ideaIC-2020.2.2.tar.gz | tar -zxv --strip-components=1 -C /ideaIC-2020.2.2 && \
    mkdir /intellij-config && \
    for f in "/intellij-config" "/ideaIC-2020.2.2" "/etc/passwd"; do \
      echo "Changing permissions on ${f}" && chgrp -R 0 ${f} && \
      chmod -R g+rwX ${f}; \
    done

COPY --chown=0:0 entrypoint.sh /entrypoint.sh
# Set permissions on /etc/passwd and /home to allow arbitrary users to write
COPY entrypoint.sh /
COPY supervisord.conf /etc/supervisord.conf
COPY fluxbox /etc/X11/fluxbox/init
RUN mkdir -p /home/user && chgrp -R 0 /home && chmod -R g=u /etc/passwd /etc/group /home && chmod +x /entrypoint.sh
USER 10001
ENV HOME=/home/user
WORKDIR /projects
ENTRYPOINT [ "/entrypoint.sh" ]
CMD ["tail", "-f", "/dev/null"]
