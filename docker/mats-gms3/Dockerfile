# Defining environment
ARG APP_ENV=prod
ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV USER=rhel
ENV FIPS_KEYSTORE_PASS=testfips
ENV FIPS_TRUSTSTORE_PASS=testfips
ENV TOMCAT_HOME=/opt/apache-tomcat

RUN yum install -y WBXjava-1.8.0-openjdk-hardening WBXFIPSJar curl wget WBXtomcat-8.5.75

COPY ./docker/mats-security/java.security /usr/lib/jvm/jre/lib/security/java.security
COPY ./docker/mats-security/certs $TOMCAT_HOME/ssl

RUN cp /opt/WBXFIPSJar/bc-fips-1.0.2.1.jar $TOMCAT_HOME/lib/. \
    && chmod 644 /usr/lib/jvm/jre/lib/security/java.security \
    && chmod -R 644 $TOMCAT_HOME/ssl

ENV DR_VERSION v0.6.1

RUN if [ $(arch) = "aarch64" ]; then \
        DR_ARCH='aarch64';\
    elif [ $(arch) = "x86_64" ]; then \
        DR_ARCH='amd64'; \
    else \
        echo >&2 "Unsupported architecture $(arch)" ; exit 1; \
    fi \
    && wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.4.1/opentelemetry-javaagent-all.jar \
    && wget https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.16.1/jmx_prometheus_javaagent-0.16.1.jar -O jmx_prometheus_javaagent.jar \
    && curl -L https://github.com/treff7es/dockerize/releases/download/$DR_VERSION/dockerize-linux-${DR_ARCH}-$DR_VERSION.tar.gz | tar -C /usr/local/bin -xzv

RUN yum remove -y wget || true

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} AS prod-build

RUN yum install -y java-1.8.0-openjdk-devel perl

ADD ./docker/mats-log/mats-monitoring-logback /root/.m2/repository/com/cisco/webex/mats/mats-monitoring-logback
COPY . /datahub-src

RUN cd /datahub-src \
    && ./gradlew :metadata-service:restli-servlet-impl:build -Prest.model.compatibility=ignore \
    && ./gradlew :metadata-service:war:build -x test

RUN cp /datahub-src/metadata-service/war/build/libs/war.war /war.war

FROM base as prod-install

COPY --from=prod-build /war.war /datahub/datahub-gms/bin/war.war
COPY --from=prod-build /datahub-src/metadata-models/src/main/resources/entity-registry.yml /datahub/datahub-gms/resources/entity-registry.yml
COPY --from=prod-build /datahub-src/docker/monitoring/client-prometheus-config.yaml /datahub/datahub-gms/scripts/prometheus-config.yaml

COPY --from=prod-build /datahub-src/docker/mats-gms3/start.sh /datahub/datahub-gms/scripts/start.sh
COPY --from=prod-build /datahub-src/docker/mats-gms3/server.xml $TOMCAT_HOME/conf/server.xml
COPY --from=prod-build /datahub-src/docker/mats-gms3/tomcat-users.xml $TOMCAT_HOME/conf/tomcat-users.xml
COPY --from=prod-build /datahub-src/docker/mats-gms3/catalina.sh $TOMCAT_HOME/bin/catalina.sh

RUN rm -f $TOMCAT_HOME/conf/web.xml

RUN chown -R $USER:$USER /home/$USER
RUN chown -R $USER:$USER /datahub
RUN chown -R $USER:$USER $TOMCAT_HOME

RUN chmod -R +x /home/$USER
RUN chmod +x /datahub/datahub-gms/scripts/start.sh
RUN chmod +x $TOMCAT_HOME/bin/catalina.sh
RUN chmod -R 655 $TOMCAT_HOME/lib

FROM base as dev-install
# Dummy stage for development. Assumes code is built on your machine and mounted to this image.
# See this excellent thread https://github.com/docker/cli/issues/1134

FROM ${APP_ENV}-install as final

ENV JMX_OPTS=""
ENV JAVA_OPTS="-Djavax.net.debug=all \
    -Djavax.net.ssl.keyStore=$TOMCAT_HOME/ssl/server.keystore.bcfks \
    -Djavax.net.ssl.keyStorePassword=$FIPS_KEYSTORE_PASS \
    -Djavax.net.ssl.trustStore=$TOMCAT_HOME/ssl/server.truststore.bcfks \
    -Djavax.net.ssl.trustStorePassword=$FIPS_TRUSTSTORE_PASS \
    -Djavax.net.ssl.keyStoreType=BCFKS \
    -Djavax.net.ssl.trustStoreType=BCFKS \
    -Djavax.net.ssl.keyStoreProvider=BCFIPS \
    -Djavax.net.ssl.trustStoreProvider=BCFIPS \
    -Djdk.tls.server.protocols=TLSv1.2 \
    -Djdk.tls.server.cipherSuites=TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384 \
    -Djavax.net.ssl.SSLContext=TLS \
    -Djdk.tls.server.SignatureSchemes=SHA384withRSA"

EXPOSE 8443
#EXPOSE 8444

HEALTHCHECK --start-period=2m --retries=4 CMD curl --fail https://localhost:8443/health || exit 1

CMD /datahub/datahub-gms/scripts/start.sh