# Defining environment
ARG APP_ENV=prod
ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

RUN yum install -y java-1.8.0 WBXFIPSJar

ENV FIPS_KEYSTORE_PASS=testfips
ENV FIPS_TRUSTSTORE_PASS=testfips
ENV SSL_HOME=/home/$USER/ssl

COPY ./docker/mats-security/il5/java.security /usr/lib/jvm/jre/lib/security/java.security
COPY ./docker/mats-security/il5/certs $SSL_HOME

RUN chmod 644 /usr/lib/jvm/jre/lib/security/java.security \
    && chmod -R 755 $SSL_HOME

FROM base as prod-install

RUN yum install -y unzip
COPY ./datahub-frontend/build/distributions/datahub-frontend.zip /datahub-frontend.zip
RUN cd / \
    && unzip datahub-frontend.zip \
    && rm -f datahub-frontend.zip \
    && chown -R 1000:1000 /datahub-frontend \
    && chmod -R 755 /datahub-frontend

RUN yum remove -y unzip || true

FROM base as dev-install
# Dummy stage for development. Assumes code is built on your machine and mounted to this image.
# See this excellent thread https://github.com/docker/cli/issues/1134
VOLUME [ "/datahub-frontend" ]

FROM ${APP_ENV}-install as final

WORKDIR /
ARG SERVER_PORT=9443
ENV SERVER_PORT=$SERVER_PORT
RUN echo $SERVER_PORT
EXPOSE $SERVER_PORT

HEALTHCHECK --start-period=2m --retries=4 CMD curl --fail http://localhost:$SERVER_PORT/admin || exit 1

ENV JAVA_OPTS=" \
   -Xms1g \
   -Xmx2g \
   -Dhttp.port=disabled \
   -Dhttps.port=$SERVER_PORT \
   -Dplay.server.https.keyStore.path=$SSL_HOME/server.keystore.bcfks \
   -Dplay.server.https.keyStore.password=$FIPS_KEYSTORE_PASS \
   -Dplay.server.https.keyStore.type=BCFKS \
   -Dplay.server.akka.max-header-value-length=40960 \
   -Dconfig.file=datahub-frontend/conf/application.conf \
   -Djava.security.auth.login.config=datahub-frontend/conf/jaas.conf \
   -Dlogback.configurationFile=datahub-frontend/conf/logback.xml \
   -Dlogback.debug=false \
   -Dpidfile.path=/dev/null \
   -Djavax.net.ssl.keyStore=$SSL_HOME/server.keystore.bcfks \
   -Djavax.net.ssl.keyStorePassword=$FIPS_KEYSTORE_PASS \
   -Djavax.net.ssl.trustStore=$SSL_HOME/server.truststore.bcfks \
   -Djavax.net.ssl.trustStorePassword=$FIPS_TRUSTSTORE_PASS \
   -Djavax.net.ssl.keyStoreType=BCFKS \
   -Djavax.net.ssl.trustStoreType=BCFKS \
   -Djavax.net.ssl.keyStoreProvider=BCFIPS \
   -Djavax.net.ssl.trustStoreProvider=BCFIPS \
   -Djdk.tls.server.protocols=TLSv1.2"

CMD ["datahub-frontend/bin/playBinary"]