# Defining environment
ARG APP_ENV=prod
ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV USER=rhel
RUN yum install -y WBXjava-1.8.0-openjdk-hardening

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} AS prod-build

RUN yum install -y java-1.8.0-openjdk-devel && yum module -y enable nodejs:16 \
    && yum install -y unzip npm perl && npm install --global yarn

ARG ENABLE_EMBER="false"
ARG USE_SYSTEM_NODE="true"
ENV CI=true
ENV GRADLE_OPTS="-Xms256m -Xmx512m"

COPY . /datahub-src
RUN cd /datahub-src  \
    && ./gradlew :metadata-service:restli-servlet-impl:build -Prest.model.compatibility=ignore \
    && ./gradlew :datahub-frontend:dist -PenableEmber=${ENABLE_EMBER} -PuseSystemNode=${USE_SYSTEM_NODE} -x test -x yarnTest -x yarnLint \
    && cp datahub-frontend/build/distributions/datahub-frontend.zip ../datahub-frontend.zip \
    && cd .. && rm -rf datahub-src && unzip datahub-frontend.zip

FROM base as prod-install

COPY --from=prod-build /datahub-frontend /datahub-frontend/
RUN chown -R $USER:$USER /datahub-frontend && chmod 755 /datahub-frontend

FROM base as dev-install
# Dummy stage for development. Assumes code is built on your machine and mounted to this image.
# See this excellent thread https://github.com/docker/cli/issues/1134
VOLUME [ "/datahub-frontend" ]

FROM ${APP_ENV}-install as final

ARG SERVER_PORT=9002
ENV SERVER_PORT=$SERVER_PORT
RUN echo $SERVER_PORT
EXPOSE $SERVER_PORT

HEALTHCHECK --start-period=2m --retries=4 CMD curl --fail http://localhost:$SERVER_PORT/admin || exit 1

ENV JAVA_OPTS=" \
   -Xms512m \
   -Xmx1024m \
   -Dhttp.port=$SERVER_PORT \
   -Dplay.server.akka.max-header-value-length=40960 \
   -Dconfig.file=datahub-frontend/conf/application.conf \
   -Djava.security.auth.login.config=datahub-frontend/conf/jaas.conf \
   -Dlogback.configurationFile=datahub-frontend/conf/logback.xml \
   -Dlogback.debug=false \
   -Dpidfile.path=/dev/null"
CMD ["/datahub-frontend/bin/playBinary"]