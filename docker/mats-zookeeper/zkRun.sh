#!/bin/bash
set -x

if [[ -v ZOOKEEPER_SERVERS ]];
then
  IFS=';' read -r -a servers <<< "$ZOOKEEPER_SERVERS"
  for index in "${!servers[@]}"
  do
    echo "server.$(($index+1))=${servers[index]}" >> /opt/apache-zookeeper/conf/zoo.cfg
  done
fi

if [ ! -f /var/zookeeper/data/myid ]
then
  echo "$ZOOKEEPER_SERVER_ID" >> /var/zookeeper/data/myid
fi

exec /opt/apache-zookeeper/bin/zkServer.sh start-foreground
