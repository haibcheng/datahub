# Defining environment
ARG APP_ENV=prod
ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM registry-rmc.webex.com/mats/rhel8-osaas-${FIPS}:${OSAAS_VERSION} as base

ENV USER=rhel

RUN yum install -y curl wget

RUN cd /root  \
    && wget https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.16.1%2B1/OpenJDK11U-jdk_x64_linux_hotspot_11.0.16.1_1.tar.gz -O openjdk.tar.gz \
    && tar -zxvf openjdk.tar.gz \
    && rm -f openjdk.tar.gz \
    && mv jdk-11.0.16.1+1 /usr/lib/jvm \
    && chmod -R +x /usr/lib/jvm/bin

ENV PATH="${PATH}:/usr/lib/jvm/bin"

ENV DR_VERSION v0.6.1

RUN if [ $(arch) = "aarch64" ]; then \
      DR_ARCH='aarch64';\
    elif [ $(arch) = "x86_64" ]; then \
      DR_ARCH='amd64'; \
    else \
      echo >&2 "Unsupported architecture $(arch)" ; exit 1; \
    fi \
    && curl https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-runner/9.4.48.v20220622/jetty-runner-9.4.48.v20220622.jar --output jetty-runner.jar \
    && curl https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-jmx/9.4.48.v20220622/jetty-jmx-9.4.48.v20220622.jar --output jetty-jmx.jar \
    && curl https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-util/9.4.48.v20220622/jetty-util-9.4.48.v20220622.jar --output jetty-util.jar \
    && wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.4.1/opentelemetry-javaagent-all.jar \
    && wget https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.16.1/jmx_prometheus_javaagent-0.16.1.jar -O jmx_prometheus_javaagent.jar \
    && curl -L https://github.com/treff7es/dockerize/releases/download/$DR_VERSION/dockerize-linux-${DR_ARCH}-$DR_VERSION.tar.gz | tar -C /usr/local/bin -xzv

RUN yum remove -y wget || true

FROM base AS prod-build

RUN yum install -y perl

ADD ./docker/mats-libs/mats-monitoring-logback /root/.m2/repository/com/cisco/webex/mats/mats-monitoring-logback
COPY . /datahub-src

RUN cd /datahub-src \
    && ./gradlew :metadata-service:restli-servlet-impl:build -Prest.model.compatibility=ignore \
    && ./gradlew :metadata-service:war:build -x test

RUN cp /datahub-src/metadata-service/war/build/libs/war.war /war.war

FROM base as prod-install

COPY --from=prod-build /war.war /datahub/datahub-gms/bin/war.war
COPY --from=prod-build /datahub-src/metadata-models/src/main/resources/entity-registry.yml /datahub/datahub-gms/resources/entity-registry.yml
COPY --from=prod-build /datahub-src/docker/mats-gms/start.sh /datahub/datahub-gms/scripts/start.sh
COPY --from=prod-build /datahub-src/docker/mats-gms/jetty.xml /datahub/datahub-gms/scripts/jetty.xml
COPY --from=prod-build /datahub-src/docker/monitoring/client-prometheus-config.yaml /datahub/datahub-gms/scripts/prometheus-config.yaml

RUN chown -R $USER:$USER /datahub
RUN chmod +x /datahub/datahub-gms/scripts/start.sh

FROM base as dev-install
# Dummy stage for development. Assumes code is built on your machine and mounted to this image.
# See this excellent thread https://github.com/docker/cli/issues/1134

FROM ${APP_ENV}-install as final

RUN mkdir -p /etc/datahub/plugins/auth/resources

ENV JMX_OPTS=""
ENV JAVA_OPTS=""

EXPOSE 8080

HEALTHCHECK --start-period=2m --retries=4 CMD curl --fail http://localhost:8080/health || exit 1

CMD /datahub/datahub-gms/scripts/start.sh