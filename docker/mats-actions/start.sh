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

mkdir -p "$DATAHUB_ACTIONS_HOME"

config_files=""

IFS=',' read -r -a action_types <<< "$DATAHUB_ACTIONS_TYPES"

for action_type in "${action_types[@]}"
do
  if [ "$(ls -A "$DATAHUB_ACTIONS_CONF/$action_type")" ]; then
    #.yml
    for file in "$DATAHUB_ACTIONS_CONF/$action_type"/*.yml;
    do
      if [ -f "$file" ]; then
        config_files+="-c $file "
      fi
    done
    #.yaml
    for file in "$DATAHUB_ACTIONS_CONF/$action_type"/*.yaml;
    do
      if [ -f "$file" ]; then
        config_files+="-c $file "
      fi
    done
  else
    echo "No action[$action_type] configurations found. Not starting actions."
  fi
done

datahub-actions actions $config_files
