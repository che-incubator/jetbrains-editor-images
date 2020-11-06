#!/bin/sh
#
# Copyright (c) 2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

CA_CERT="/tmp/che/secret/ca.crt"


if [ -z "${CHE_API_INTERNAL}" ] || [ -z "${CHE_WORKSPACE_ID}" ]; then
    echo "CHE_API_INTERNAL or CHE_WORKSPACE_ID is not configured in environment."
    exit 1
fi

CURL_OPTS=

if [ -e $CA_CERT ]; then
    CURL_OPTS="$CURL_OPTS --cacert /tmp/che/secret/ca.crt"
fi

if [ -n "$CHE_MACHINE_TOKEN" ]; then
    CURL_OPTS="$CURL_OPTS -H \"Authorization: Bearer ${CHE_MACHINE_TOKEN}\""
fi

REQUEST_URL="${CHE_API_INTERNAL}/activity/${CHE_WORKSPACE_ID}"

while true; do
    echo "$(date) - Perform request: 'curl -X PUT $CURL_OPTS $REQUEST_URL'."
    eval curl -X PUT "${CURL_OPTS}" "${REQUEST_URL}"
    echo "$(date) - Sleep for 1 minute."

    sleep 60
done
