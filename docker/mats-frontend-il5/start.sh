#!/bin/bash
set -x

JAVA_OPTS="$JAVA_OPTS -Dhttp.port=disabled \
    -Dhttps.port=$SERVER_PORT \
    -Dplay.server.https.keyStore.path=/etc/datahub/certs/server.keystore.bcfks \
    -Dplay.server.https.keyStore.password=$SSL_KEYSTORE_PASS \
    -Dplay.server.https.keyStore.type=BCFKS \
    -Dplay.server.akka.max-header-value-length=40960 \
    -Dconfig.file=datahub-frontend/conf/application.conf \
    -Djava.security.auth.login.config=datahub-frontend/conf/jaas.conf \
    -Dlogback.configurationFile=datahub-frontend/conf/logback.xml \
    -Dlogback.debug=false \
    -Dpidfile.path=/dev/null \
    -Djavax.net.ssl.keyStore=/etc/datahub/certs/server.keystore.bcfks \
    -Djavax.net.ssl.keyStorePassword=$SSL_KEYSTORE_PASS \
    -Djavax.net.ssl.trustStore=/etc/datahub/certs/server.truststore.bcfks \
    -Djavax.net.ssl.trustStorePassword=$SSL_TRUSTSTORE_PASS \
    -Djavax.net.ssl.keyStoreType=BCFKS \
    -Djavax.net.ssl.trustStoreType=BCFKS \
    -Djavax.net.ssl.keyStoreProvider=BCFIPS \
    -Djavax.net.ssl.trustStoreProvider=BCFIPS \
    -Djdk.tls.server.protocols=TLSv1.2"

source playBinary