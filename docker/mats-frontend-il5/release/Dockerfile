# Defining environment
ARG APP_ENV=prod
ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

RUN yum install -y java-1.8.0 WBXFIPSJar

COPY ./docker/mats-security/il5/java.security /usr/lib/jvm/jre/lib/security/java.security
RUN chmod 644 /usr/lib/jvm/jre/lib/security/java.security

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} AS prod-build

RUN yum install -y java-1.8.0-openjdk-devel && yum module -y enable nodejs:16 \
    && yum install -y unzip npm perl && npm install --global yarn

ARG ENABLE_EMBER="false"
ARG USE_SYSTEM_NODE="true"
ENV CI=true
ENV GRADLE_OPTS="-Xms256m -Xmx512m"

ADD ./docker/mats-log/mats-monitoring-logback /root/.m2/repository/com/cisco/webex/mats/mats-monitoring-logback
COPY . /datahub-src

RUN cd /datahub-src \
    && rm -rf datahub-frontend/build \
    && ./gradlew :metadata-service:restli-servlet-impl:build -Prest.model.compatibility=ignore \
    && ./gradlew :datahub-frontend:dist -PenableEmber=${ENABLE_EMBER} -PuseSystemNode=${USE_SYSTEM_NODE} -x test -x yarnTest -x yarnLint \
    && cp datahub-frontend/build/distributions/datahub-frontend.zip ../datahub-frontend.zip \
    && cd .. && unzip datahub-frontend.zip

FROM base as prod-install

COPY --from=prod-build /datahub-frontend /datahub-frontend/
COPY --from=prod-build /datahub-src/docker/mats-frontend-il5/start.sh /datahub-frontend/bin/start.sh

RUN chown -R 1000:1000 /datahub-frontend && chmod 755 /datahub-frontend

FROM base as dev-install
# Dummy stage for development. Assumes code is built on your machine and mounted to this image.
# See this excellent thread https://github.com/docker/cli/issues/1134
VOLUME [ "/datahub-frontend" ]

FROM ${APP_ENV}-install as final

WORKDIR /

ARG SERVER_PORT=9443
ENV SERVER_PORT=$SERVER_PORT
ENV SERVER_HOST=localhost

RUN echo $SERVER_PORT
EXPOSE $SERVER_PORT

HEALTHCHECK --start-period=2m --retries=4 CMD curl --fail https://$SERVER_HOST:$SERVER_PORT/admin || exit 1

ENV JAVA_OPTS=""

CMD ["datahub-frontend/bin/start.sh"]