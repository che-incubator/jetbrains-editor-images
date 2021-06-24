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

SEARCH_FILTER=""

if [ "$CONTAINER_MATRIX" = "all" ]; then
  SEARCH_FILTER=".[] | {displayName, dockerImage, productCode} + (.productVersion[])"
elif [ "$CONTAINER_MATRIX" = "latest" ]; then
  SEARCH_FILTER=".[] | {displayName, dockerImage, productCode} + (.productVersion[]) | select(.latest == true)"
else
  exit 3
fi

jq -c -r "$SEARCH_FILTER" < "$base_dir"/../compatible-ide.json | paste -sd "," -
