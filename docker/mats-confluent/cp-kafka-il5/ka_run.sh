#!/bin/bash

export KAFKA_SSL_KEYSTORE_TYPE=BCFKS
export KAFKA_SSL_ENABLED_PROTOCOLS=TLSv1.2
export KAFKA_SSL_TRUSTSTORE_TYPE=BCFKS
export KAFKA_SECURITY_INTER_BROKER_PROTOCOL=SSL

export KAFKA_SSL_KEY_CREDENTIALS=key.credentials
export KAFKA_SSL_KEYSTORE_FILENAME=server.keystore.bcfks
export KAFKA_SSL_KEYSTORE_CREDENTIALS=keystore.credentials
export KAFKA_SSL_TRUSTSTORE_FILENAME=server.truststore.bcfks
export KAFKA_SSL_TRUSTSTORE_CREDENTIALS=truststore.credentials

export KAFKA_DATA_DIRS=/var/kafka/data

JAVA_OPTS="$JAVA_OPTS \
    -Djavax.net.ssl.keyStore=/etc/kafka/secrets/server.keystore.bcfks \
    -Djavax.net.ssl.keyStorePassword=$(cat "/etc/kafka/secrets/$KAFKA_SSL_KEYSTORE_CREDENTIALS") \
    -Djavax.net.ssl.trustStore=/etc/kafka/secrets/server.truststore.bcfks \
    -Djavax.net.ssl.trustStorePassword=$(cat "/etc/kafka/secrets/$KAFKA_SSL_TRUSTSTORE_CREDENTIALS") \
    -Djavax.net.ssl.keyStoreType=BCFKS \
    -Djavax.net.ssl.trustStoreType=BCFKS \
    -Djavax.net.ssl.keyStoreProvider=BCFIPS \
    -Djavax.net.ssl.trustStoreProvider=BCFIPS \
    -Djdk.tls.server.protocols=TLSv1.2"

exec /etc/confluent/docker/run