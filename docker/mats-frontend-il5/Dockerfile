# Defining environment
ARG APP_ENV=prod
ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

RUN yum install -y java-1.8.0 WBXFIPSJar

COPY ./docker/mats-security/il5/java.security /usr/lib/jvm/jre/lib/security/java.security
RUN chmod 644 /usr/lib/jvm/jre/lib/security/java.security

FROM base as prod-install

RUN yum install -y unzip
COPY ./datahub-frontend/build/distributions/datahub-frontend.zip /datahub-frontend.zip
RUN cd / && unzip datahub-frontend.zip && rm -f datahub-frontend.zip

COPY ./docker/mats-frontend-il5/start.sh /datahub-frontend/bin/start.sh
RUN chown -R 1000:1000 /datahub-frontend && chmod -R 755 /datahub-frontend

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

HEALTHCHECK --start-period=2m --retries=4 CMD curl -k --fail https://localhost:$SERVER_PORT/admin || exit 1

ENV JAVA_OPTS=""

CMD ["datahub-frontend/bin/start.sh"]