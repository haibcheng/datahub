#!/bin/bash

[ ! -f /etc/datahub/env/frontend.env ] || export $(grep -v '^#' /etc/datahub/env/frontend.env | xargs)

JAVA_OPTS="$JAVA_OPTS -DLOG_DIR=${LOG_DIR} \
    -Dhttp.port=$SERVER_PORT \
    -Dplay.server.akka.max-header-value-length=40960 \
    -Dconfig.file=datahub-frontend/conf/application.conf \
    -Djava.security.auth.login.config=datahub-frontend/conf/jaas.conf \
    -Dlogback.configurationFile=datahub-frontend/conf/logback.xml \
    -Dpidfile.path=/dev/null"

exec /datahub-frontend/bin/datahub-frontend