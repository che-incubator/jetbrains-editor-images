#!/bin/bash
#
# Copyright (c) 2022 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

# necessary environment variable: PROJECTOR_ASSEMBLY_DIR
if [ -n "$PROJECTOR_ASSEMBLY_DIR" ]; then
  cd "$PROJECTOR_ASSEMBLY_DIR"/ide/bin || exit
  ./ide-projector-launcher.sh
else
  echo "Environment variable PROJECTOR_ASSEMBLY_DIR is not set"
fi
