#!/bin/bash

JAVA_OPTS="$JAVA_OPTS -Dhttp.port=$SERVER_PORT \
    -Dplay.server.akka.max-header-value-length=40960 \
    -Dconfig.file=datahub-frontend/conf/application.conf \
    -Djava.security.auth.login.config=datahub-frontend/conf/jaas.conf \
    -Dlogback.configurationFile=datahub-frontend/conf/logback.xml \
    -Dpidfile.path=/dev/null"

source /datahub-frontend/bin/datahub-frontend