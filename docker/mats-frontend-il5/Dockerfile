# Defining environment
ARG APP_ENV=prod

FROM containers.cisco.com/rhel8/osaas-fips:1.8.202206 as base
RUN yum install -y WBXjava-1.8.0-openjdk-hardening unzip

ENV CI=true
ENV USER=rhel
ENV GRADLE_OPTS="-Xms256m -Xmx512m"

COPY ./datahub-frontend/build/distributions/datahub-frontend.zip datahub-frontend.zip
RUN unzip datahub-frontend.zip && rm -f datahub-frontend.zip

FROM base as prod-install

COPY /datahub-frontend /datahub-frontend/
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
CMD ["datahub-frontend/bin/playBinary"]