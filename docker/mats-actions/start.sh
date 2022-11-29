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

touch /tmp/datahub/logs/actions/actions.out

mkdir -p "$CACHE_ROOT_PATH"
mkdir -p "$ACTION_ROOT_PATH/conf"
mkdir -p "$ACTION_ROOT_PATH/system/conf"

# Deploy System Actions
if [ "$(ls -A "$ACTION_ROOT_PATH"/system/conf/)" ]; then
    config_files=""
    # .yml 
    for file in "$ACTION_ROOT_PATH"/system/conf/*.yml;
    do
        if [ -f "$file" ]; then
            config_files+="-c $file "
        fi
    done
    #.yaml
    for file in "$ACTION_ROOT_PATH"/system/conf/*.yaml;
    do
        if [ -f "$file" ]; then
            config_files+="-c $file "
        fi
    done
else
    echo "No system action configurations found. Not starting system actions."
fi

# Deploy User Actions
if [ "$(ls -A "$ACTION_ROOT_PATH"/conf/)" ]; then
    # .yml
    for file in "$ACTION_ROOT_PATH"/conf/*.yml;
    do
        if [ -f "$file" ]; then
            config_files+="-c $file "
        fi
    done
    #.yaml
    for file in "$ACTION_ROOT_PATH"/conf/*.yaml;
    do
        if [ -f "$file" ]; then
            config_files+="-c $file "
        fi
    done
else
    echo "No user action configurations found. Not starting user actions."
fi

datahub actions "$config_files"
