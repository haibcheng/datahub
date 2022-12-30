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
  common_active="$DATAHUB_ACTIONS_CONF/$action_type"
  if [ "$(ls -A "$common_active"/*.yml 2> /dev/null)" ] || [ "$(ls -A "$common_active"/*.yaml 2> /dev/null)" ]; then
    for file in "$common_active"/*.yml;
    do
      if [ -f "$file" ]; then
        config_files+="-c $file "
      fi
    done
    for file in "$common_active"/*.yaml;
    do
      if [ -f "$file" ]; then
        config_files+="-c $file "
      fi
    done
  else
    echo "No action[$common_active] configurations found."
  fi

  if [ -n "$DATAHUB_ACTIONS_PROFILES_ACTIVE" ]; then
    profiles_active="$DATAHUB_ACTIONS_CONF/$action_type/$DATAHUB_ACTIONS_PROFILES_ACTIVE"
    if [ "$(ls -A "$profiles_active"/*.yml 2> /dev/null)" ] || [ "$(ls -A "$profiles_active"/*.yaml 2> /dev/null)" ]; then
      for file in "$profiles_active"/*.yml;
      do
        if [ -f "$file" ]; then
          config_files+="-c $file "
        fi
      done
      for file in "$profiles_active"/*.yaml;
      do
        if [ -f "$file" ]; then
          config_files+="-c $file "
        fi
      done
    else
      echo "No action[$profiles_active] configurations found."
    fi
  fi

done

datahub-actions actions $config_files
