#!/bin/bash

#
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

set -e # Any command which returns non-zero exit code will cause this shell script to exit immediately
set -x # Activate debugging to show execution details: all commands will be printed before execution

base_dir=$(cd "$(dirname "$0")"; pwd)

# Clone the Projector Client, stick to the particular version and apply necessary patches if needed
git clone https://github.com/JetBrains/projector-client.git "$base_dir"/../projector-client
cd "$base_dir"/../projector-client
git checkout 555e38b5885df2598fcd2639c687124e60e3218e

if [ -d "$base_dir/patches/projector-client" ]; then
    echo "Applying patches for Projector Client"
    find "$base_dir"/patches/projector-client -name "*.patch" -exec echo "Patching with {}" \; -exec git apply {} \;
fi

cd "$base_dir"

# Clone the Projector Server, stick to the particular version and apply necessary patches if needed.
# Link with Projector Client
git clone https://github.com/JetBrains/projector-server.git "$base_dir"/../projector-server
cd "$base_dir"/../projector-server
git checkout f8461f8aadefcdc2ebf3ceb97cf591df1e6d2f8f
echo "useLocalProjectorClient=true" > local.properties

if [ -d "$base_dir/patches/projector-server" ]; then
    echo "Applying patches for Projector Server"
    find "$base_dir"/patches/projector-server -name "*.patch" -exec echo "Patching with {}" \; -exec git apply {} \;
fi

cd "$base_dir"
