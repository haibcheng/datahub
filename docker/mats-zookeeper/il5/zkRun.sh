#!/bin/bash

SSL_KEYSTORE_LOCATION=/etc/zookeeper/secrets/server.keystore.bcfks
SSL_KEYSTORE_PASS=$(cat "/etc/zookeeper/secrets/keystore.credentials")
SSL_TRUSTSTORE_LOCATION=/etc/zookeeper/secrets/server.truststore.bcfks
SSL_TRUSTSTORE_PASS=$(cat "/etc/zookeeper/secrets/truststore.credentials")

JAVA_OPTS="$JAVA_OPTS \
    -Djavax.net.ssl.keyStore=$SSL_KEYSTORE_LOCATION \
    -Djavax.net.ssl.keyStorePassword=$SSL_KEYSTORE_PASS \
    -Djavax.net.ssl.trustStore=$SSL_TRUSTSTORE_LOCATION \
    -Djavax.net.ssl.trustStorePassword=$SSL_TRUSTSTORE_PASS \
    -Djavax.net.ssl.keyStoreType=BCFKS \
    -Djavax.net.ssl.trustStoreType=BCFKS \
    -Djavax.net.ssl.keyStoreProvider=BCFIPS \
    -Djavax.net.ssl.trustStoreProvider=BCFIPS \
    -Djdk.tls.server.protocols=TLSv1.2"

export SERVER_JVMFLAGS="\
    -Dzookeeper.serverCnxnFactory=org.apache.zookeeper.server.NettyServerCnxnFactory \
    -Dzookeeper.ssl.keyStore.type=BCFKS \
    -Dzookeeper.ssl.keyStore.location=$SSL_KEYSTORE_LOCATION \
    -Dzookeeper.ssl.keyStore.password=$SSL_KEYSTORE_PASS \
    -Dzookeeper.ssl.trustStore.location=$SSL_TRUSTSTORE_LOCATION \
    -Dzookeeper.ssl.trustStore.password=$SSL_TRUSTSTORE_PASS \
    -Dzookeeper.ssl.trustStore.type=BCFKS"

exec /opt/apache-zookeeper/bin/zkServer.sh start-foreground