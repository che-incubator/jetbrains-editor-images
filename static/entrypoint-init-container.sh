#!/bin/bash
#
# Copyright (c) 2022 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

# necessary environment variables: PROJECTOR_VOLUME_MOUNT and PROJECTOR_ASSEMBLY_DIR
if [ -n "$PROJECTOR_VOLUME_MOUNT" ] || [ -n "$PROJECTOR_ASSEMBLY_DIR" ]; then
  echo "Found environment variable PROJECTOR_VOLUME_MOUNT: $PROJECTOR_VOLUME_MOUNT"
  echo "Found environment variable PROJECTOR_ASSEMBLY_DIR: $PROJECTOR_ASSEMBLY_DIR"
  cp -r "$PROJECTOR_ASSEMBLY_DIR"/* "$PROJECTOR_VOLUME_MOUNT"
  echo "Listing all copied files: "
  ls -lah "$PROJECTOR_VOLUME_MOUNT"
else
  echo "Environment variable PROJECTOR_VOLUME_MOUNT or PROJECTOR_ASSEMBLY_DIR is not set"
fi
