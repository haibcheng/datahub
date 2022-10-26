#!/bin/bash
set -x

sed -i "s/\${KAFKA_BROKER_ID}/${KAFKA_BROKER_ID}/g" /opt/kafka/config/server.properties
sed -i "s/\${KAFKA_INTER_BROKER_LISTENER_NAME}/${KAFKA_INTER_BROKER_LISTENER_NAME}/g" /opt/kafka/config/server.properties
sed -i "s+\${KAFKA_LOG_DIRS}+${KAFKA_LOG_DIRS}+g" /opt/kafka/config/server.properties
sed -i "s+\${KAFKA_LISTENERS}+${KAFKA_LISTENERS}+g" /opt/kafka/config/server.properties
sed -i "s+\${KAFKA_ADVERTISED_LISTENERS}+${KAFKA_ADVERTISED_LISTENERS}+g" /opt/kafka/config/server.properties
sed -i "s/\${KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR}/${KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR}/g" /opt/kafka/config/server.properties
sed -i "s/\${KAFKA_ZOOKEEPER_CONNECT}/${KAFKA_ZOOKEEPER_CONNECT}/g" /opt/kafka/config/server.properties
sed -i "s/\${KAFKA_LISTENER_SECURITY_PROTOCOL_MAP}/${KAFKA_LISTENER_SECURITY_PROTOCOL_MAP}/g" /opt/kafka/config/server.properties
sed -i "s/\${KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS}/${KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS}/g" /opt/kafka/config/server.properties

exec /opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/server.properties