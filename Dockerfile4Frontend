# Defining environment
ARG APP_ENV=prod
ARG FIPS=nonfips

FROM registry-rmc.webex.com/mats/alma8-${FIPS}:1.4.202303 as base
ENV USER=alma

COPY ./docker/mats-libs/alma8/adoptium.repo /etc/yum.repos.d/adoptium.repo

RUN yum install -y curl \
    && yum install -y temurin-11-jdk

ENV JAVA_HOME=/usr/lib/jvm/temurin-11-jdk

FROM base AS prod-build

COPY ./docker/mats-libs/alma8/almalinux.repo /etc/yum.repos.d/almalinux.repo

RUN yum module -y enable nodejs:16 \
    && yum install -y unzip npm perl && npm install --global yarn

ARG USE_SYSTEM_NODE="true"
ENV CI=true
ENV GRADLE_OPTS="-Xms512m -Xmx1g"

ADD ./docker/mats-libs/mats-monitoring-logback /root/.m2/repository/com/cisco/webex/mats/mats-monitoring-logback
COPY . /datahub-src

RUN cd /datahub-src \
    && rm -rf datahub-frontend/build \
    && ./gradlew :metadata-service:restli-servlet-impl:build -Prest.model.compatibility=ignore \
    && ./gradlew :datahub-web-react:build -x test -x yarnTest -x yarnLint \
    && ./gradlew :datahub-frontend:dist -PuseSystemNode=${USE_SYSTEM_NODE} -x test -x yarnTest -x yarnLint \
    && cp datahub-frontend/build/distributions/datahub-frontend.zip ../datahub-frontend.zip \
    && cd .. && rm -rf datahub-src && unzip datahub-frontend.zip

FROM base as prod-install

COPY --from=prod-build /datahub-frontend /datahub-frontend/
COPY ./docker/mats-frontend/start.sh /datahub-frontend/bin/start.sh

RUN chown -R $USER:$USER /datahub-frontend && chmod 755 /datahub-frontend

FROM base as dev-install
# Dummy stage for development. Assumes code is built on your machine and mounted to this image.
# See this excellent thread https://github.com/docker/cli/issues/1134
VOLUME [ "/datahub-frontend" ]

FROM ${APP_ENV}-install as final

ENV LOG_DIR=/tmp/datahub/logs/frontend
RUN mkdir -p $LOG_DIR && chown -R $USER:$USER /tmp/datahub

WORKDIR /
ARG SERVER_PORT=9002
ENV SERVER_PORT=$SERVER_PORT
RUN echo $SERVER_PORT
EXPOSE $SERVER_PORT

HEALTHCHECK --start-period=2m --retries=4 CMD curl --fail http://localhost:$SERVER_PORT/admin || exit 1

ENV JAVA_OPTS=""

CMD ["datahub-frontend/bin/start.sh"]