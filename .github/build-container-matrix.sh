#!/bin/bash

#
# Copyright (c) 2021 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

set -e

CONTAINER_MATRIX="all"
CONTAINER_MATRIX_CONFIG_DIR="quay.io"

while getopts ":m:" opt; do
  case $opt in
    m)
      CONTAINER_MATRIX=$OPTARG
      ;;
    \?)
      exit 1
      ;;
  esac
done

base_dir=$(cd "$(dirname "$0")"; pwd)

if [ ! -d "$base_dir/$CONTAINER_MATRIX_CONFIG_DIR" ]; then
    exit 2
fi

SEARCH_FILTER=""

if [ "$CONTAINER_MATRIX" = "all" ]; then
  SEARCH_FILTER=".[]"
elif [ "$CONTAINER_MATRIX" = "latest" ]; then
  SEARCH_FILTER=".[] | select(.imageTag == \"latest\")"
else
  exit 3
fi

find "$base_dir/$CONTAINER_MATRIX_CONFIG_DIR" -name "*.json" -print0 -type f | xargs -0 cat | jq -c -r "$SEARCH_FILTER" | paste -sd "," -
