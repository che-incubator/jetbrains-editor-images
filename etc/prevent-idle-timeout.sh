#!/bin/sh
#
# Copyright (c) 2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

# Ensure that main environment variable are configured
if [ -z "${CHE_API_INTERNAL}" ]; then
    echo "CHE_API_INTERNAL is not configured in environment."
    exit 1
fi
if [ -z "${CHE_MACHINE_TOKEN}" ]; then
    echo "CHE_MACHINE_TOKEN is not configured in environment."
    exit 1
fi
if [ -z "${CHE_WORKSPACE_ID}" ]; then
    echo "CHE_WORKSPACE_ID is not configured in environment."
    exit 1
fi

# Todo(vzhukovs): check for client certificates that might be included into request in case if script runs on minikube with self-signed certificates

# Start making requests to avoid idling, make a request to activity tracker each minute
while true; do
    curl -X PUT \
    -H "Authorization: Bearer ${CHE_MACHINE_TOKEN}" \
    "${CHE_API_INTERNAL}/activity/${CHE_WORKSPACE_ID}"

    sleep 1m
done
