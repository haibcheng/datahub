#!/bin/bash

export ZOO_LOG_DIR=/var/zookeeper/logs

exec /opt/apache-zookeeper/bin/zkServer.sh start-foreground