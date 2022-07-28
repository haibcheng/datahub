#!/bin/bash

JAVA_OPTS="$JAVA_OPTS \
    -Djavax.net.ssl.keyStore=/etc/datahub/certs/server.keystore.bcfks \
    -Djavax.net.ssl.keyStorePassword=$SSL_KEYSTORE_PASS \
    -Djavax.net.ssl.trustStore=/etc/datahub/certs/server.truststore.bcfks \
    -Djavax.net.ssl.trustStorePassword=$SSL_TRUSTSTORE_PASS \
    -Djavax.net.ssl.keyStoreType=BCFKS \
    -Djavax.net.ssl.trustStoreType=BCFKS \
    -Djavax.net.ssl.keyStoreProvider=BCFIPS \
    -Djavax.net.ssl.trustStoreProvider=BCFIPS \
    -Djdk.tls.server.protocols=TLSv1.2"

export KAFKA_SSL_KEY_PASSWORD=$SSL_KEYSTORE_PASS
export KAFKA_SSL_KEYSTORE_LOCATION=/etc/datahub/certs/server.keystore.bcfks
export KAFKA_SSL_KEYSTORE_PASSWORD=$SSL_KEYSTORE_PASS
export KAFKA_SSL_KEYSTORE_TYPE=BCFIPS
export KAFKA_SSL_ENABLED_PROTOCOLS=TLSv1.2
export KAFKA_SSL_TRUSTSTORE_LOCATION=/etc/datahub/certs/server.truststore.bcfks
export KAFKA_SSL_TRUSTSTORE_PASSWORD=$SSL_TRUSTSTORE_PASS
export KAFKA_SSL_TRUSTSTORE_TYPE=BCFIPS
export KAFKA_SECURITY_INTER_BROKER_PROTOCOL=TLSv1.2

exec /etc/confluent/docker/run