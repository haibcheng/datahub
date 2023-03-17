# Defining environment
ARG APP_ENV=prod
ARG FIPS=nonfips
ARG OSAAS_VERSION=1.16.202302

FROM registry-rmc.webex.com/mats/rhel8-${FIPS}:${OSAAS_VERSION} as base

RUN yum install -y wget

RUN cd /root  \
    && wget https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.16.1%2B1/OpenJDK11U-jdk_x64_linux_hotspot_11.0.16.1_1.tar.gz -O openjdk.tar.gz \
    && tar -zxvf openjdk.tar.gz \
    && rm -f openjdk.tar.gz \
    && mv jdk-11.0.16.1+1 /usr/lib/jvm \
    && chmod -R +x /usr/lib/jvm/bin

ENV PATH="${PATH}:/usr/lib/jvm/bin"

RUN yum remove -y wget || true

FROM base as prod-install

RUN yum install -y unzip
COPY ./datahub-frontend/build/distributions/datahub-frontend.zip /datahub-frontend.zip
RUN cd / && unzip datahub-frontend.zip && rm -f datahub-frontend.zip

COPY ./docker/mats-frontend/start.sh /datahub-frontend/bin/start.sh
RUN chown -R 1000:1000 /datahub-frontend && chmod -R 755 /datahub-frontend

RUN yum remove -y unzip || true

FROM base as dev-install
# Dummy stage for development. Assumes code is built on your machine and mounted to this image.
# See this excellent thread https://github.com/docker/cli/issues/1134
VOLUME [ "/datahub-frontend" ]

FROM ${APP_ENV}-install as final

ENV LOG_DIR=/tmp/datahub/logs/frontend
RUN mkdir -p $LOG_DIR && chown -R rhel:rhel /tmp/datahub

WORKDIR /
ARG SERVER_PORT=9002
ENV SERVER_PORT=$SERVER_PORT
RUN echo $SERVER_PORT
EXPOSE $SERVER_PORT

HEALTHCHECK --start-period=2m --retries=4 CMD curl --fail http://localhost:$SERVER_PORT/admin || exit 1

ENV JAVA_OPTS=""

CMD ["datahub-frontend/bin/start.sh"]