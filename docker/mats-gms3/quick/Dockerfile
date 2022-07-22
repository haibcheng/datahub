# Defining environment
ARG APP_ENV=prod
ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV FIPS_KEYSTORE_PASS=testfips
ENV FIPS_TRUSTSTORE_PASS=testfips
ENV TOMCAT_HOME=/opt/apache-tomcat

RUN yum install -y java-1.8.0 WBXFIPSJar curl wget WBXtomcat-8.5.75

COPY ./docker/mats-security/java.security /usr/lib/jvm/jre/lib/security/java.security
COPY ./docker/mats-security/certs $TOMCAT_HOME/ssl

RUN chmod 644 /usr/lib/jvm/jre/lib/security/java.security \
    && chmod -R 755 $TOMCAT_HOME/ssl

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

FROM base as prod-install

COPY ./metadata-service/war/build/libs/war.war /datahub/datahub-gms/bin/war.war
COPY ./metadata-models/src/main/resources/entity-registry.yml /datahub/datahub-gms/resources/entity-registry.yml
COPY ./docker/monitoring/client-prometheus-config.yaml /datahub/datahub-gms/scripts/prometheus-config.yaml

COPY ./docker/mats-gms3/start.sh /datahub/datahub-gms/scripts/start.sh
COPY ./docker/mats-gms3/server.xml $TOMCAT_HOME/conf/server.xml
COPY ./docker/mats-gms3/tomcat-users.xml $TOMCAT_HOME/conf/tomcat-users.xml
COPY ./docker/mats-gms3/catalina.sh $TOMCAT_HOME/bin/catalina.sh

RUN rm -f $TOMCAT_HOME/conf/web.xml

RUN chown -R wbxtomcat:wbxgroup /datahub
RUN chown -R wbxtomcat:wbxgroup $TOMCAT_HOME

RUN chmod -R +x /home/$USER
RUN chmod +x /datahub/datahub-gms/scripts/start.sh
RUN chmod +x $TOMCAT_HOME/bin/catalina.sh

FROM base as dev-install
# Dummy stage for development. Assumes code is built on your machine and mounted to this image.
# See this excellent thread https://github.com/docker/cli/issues/1134

FROM ${APP_ENV}-install as final

ENV JMX_OPTS=""
ENV JAVA_OPTS="-Xms2g -Xmx4g \
    -Djavax.net.ssl.keyStore=$TOMCAT_HOME/ssl/server.keystore.bcfks \
    -Djavax.net.ssl.keyStorePassword=$FIPS_KEYSTORE_PASS \
    -Djavax.net.ssl.trustStore=$TOMCAT_HOME/ssl/server.truststore.bcfks \
    -Djavax.net.ssl.trustStorePassword=$FIPS_TRUSTSTORE_PASS \
    -Djavax.net.ssl.keyStoreType=BCFKS \
    -Djavax.net.ssl.trustStoreType=BCFKS \
    -Djavax.net.ssl.keyStoreProvider=BCFIPS \
    -Djavax.net.ssl.trustStoreProvider=BCFIPS \
    -Djdk.tls.server.protocols=TLSv1.2"

EXPOSE 8443

HEALTHCHECK --start-period=2m --retries=4 CMD curl --fail https://localhost:8443/health || exit 1

ENTRYPOINT ["gosu","wbxtomcat:wbxgroup","/usr/local/bin/dumb-init","/main.sh"]
CMD /datahub/datahub-gms/scripts/start.sh