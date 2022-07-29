#!/bin/bash

SSL_KEYSTORE_LOCATION=/etc/datahub/certs/server.keystore.bcfks
SSL_KEYSTORE_PASS=$(cat "/etc/datahub/certs/keystore.credentials")
SSL_TRUSTSTORE_LOCATION=/etc/datahub/certs/server.truststore.bcfks
SSL_TRUSTSTORE_PASS=$(cat "/etc/datahub/certs/truststore.credentials")
ELASTICSEARCH_PASS=$(cat "/etc/datahub/certs/elasticsearch.credentials")
DATAHUB_SECRET_C=$(cat "/etc/datahub/certs/datahub.secret")
AUTH_OIDC_CLIENT_SECRET_C=$(cat "/etc/datahub/certs/oidc.client.secret")

JAVA_OPTS="$JAVA_OPTS -Dhttp.port=disabled \
    -Dhttps.port=$SERVER_PORT \
    -Dplay.server.https.keyStore.path=$SSL_KEYSTORE_LOCATION \
    -Dplay.server.https.keyStore.password=$SSL_KEYSTORE_PASS \
    -Dplay.server.https.keyStore.type=BCFKS \
    -Dplay.server.akka.max-header-value-length=40960 \
    -Dconfig.file=datahub-frontend/conf/application.conf \
    -Djava.security.auth.login.config=datahub-frontend/conf/jaas.conf \
    -Dlogback.configurationFile=datahub-frontend/conf/logback.xml \
    -Dlogback.debug=false \
    -Dpidfile.path=/dev/null \
    -Djavax.net.ssl.keyStore=$SSL_KEYSTORE_LOCATION \
    -Djavax.net.ssl.keyStorePassword=$SSL_KEYSTORE_PASS \
    -Djavax.net.ssl.keyStoreType=BCFKS \
    -Djavax.net.ssl.keyStoreProvider=BCFIPS \
    -Djavax.net.ssl.trustStore=$SSL_TRUSTSTORE_LOCATION \
    -Djavax.net.ssl.trustStorePassword=$SSL_TRUSTSTORE_PASS \
    -Djavax.net.ssl.trustStoreType=BCFKS \
    -Djavax.net.ssl.trustStoreProvider=BCFIPS \
    -Djdk.tls.server.protocols=TLSv1.2"

export ELASTIC_CLIENT_PASSWORD="$ELASTICSEARCH_PASS"
export ES_PASSWORD="$ELASTIC_CLIENT_PASSWORD"
export DATAHUB_SECRET="$DATAHUB_SECRET_C"
export AUTH_OIDC_CLIENT_SECRET="$AUTH_OIDC_CLIENT_SECRET_C"

source /datahub-frontend/bin/playBinary