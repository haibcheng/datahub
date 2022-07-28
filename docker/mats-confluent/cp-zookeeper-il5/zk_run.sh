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
    -Djdk.tls.server.protocols=TLSv1.2 \
    -Dzookeeper.serverCnxnFactory=org.apache.zookeeper.server.NettyServerCnxnFactory \
    -Dzookeeper.ssl.keyStore.location=/etc/datahub/certs/server.keystore.bcfks \
    -Dzookeeper.ssl.keyStore.password=$SSL_KEYSTORE_PASS \
    -Dzookeeper.ssl.keyStore.type=BCFIPS \
    -Dzookeeper.ssl.trustStore.location=/etc/datahub/certs/server.truststore.bcfks \
    -Dzookeeper.ssl.trustStore.password=$SSL_TRUSTSTORE_PASS \
    -Dzookeeper.ssl.trustStore.type=BCFIPS \
    -Dzookeeper.ssl.quorum.keyStore.location=/etc/datahub/certs/server.keystore.bcfks \
    -Dzookeeper.ssl.quorum.keyStore.password=$SSL_KEYSTORE_PASS \
    -Dzookeeper.ssl.quorum.keyStore.type=BCFIPS \
    -Dzookeeper.ssl.quorum.trustStore.location=/etc/datahub/certs/server.truststore.bcfks \
    -Dzookeeper.ssl.quorum.trustStore.password=$SSL_TRUSTSTORE_PASS \
    -Dzookeeper.ssl.quorum.trustStore.type=BCFIPS \
    -Dzookeeper.ssl.quorum.hostnameVerification=false"

. run