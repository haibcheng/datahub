# Defining environment
ARG APP_ENV=prod
ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

RUN yum install -y WBXjava-1.8.0-openjdk-hardening

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
CMD ["datahub-frontend/bin/playBinary"]