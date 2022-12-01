#!/bin/bash

# Copyright 2021 Acryl Data, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

touch /home/rhel/actions/logs/actions.out

mkdir -p "$CACHE_ROOT_PATH"

if [ "$(ls -A /home/rhel/actions/conf/)" ]; then
    #.yml
    for file in /home/rhel/actions/conf/*.yml;
    do
        if [ -f "$file" ]; then
            config_files+="-c $file "
        fi
    done
    #.yaml
    for file in /home/rhel/actions/conf/*.yaml;
    do
        if [ -f "$file" ]; then
            config_files+="-c $file "
        fi
    done
else
    echo "No action configurations found. Not starting actions."
fi

datahub actions "$config_files"
