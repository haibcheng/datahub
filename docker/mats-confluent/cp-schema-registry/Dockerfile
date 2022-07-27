ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV COMPONENT=schema-registry
RUN yum install -y java-1.8.0 WBXFIPSJar python3.9 python39-pip python39-setuptools git

ENV PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC=git+https://github.com/confluentinc/confluent-docker-utils@v0.0.52
RUN python3.9 -m pip install --prefer-binary --prefix=/usr/local --upgrade "${PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC}"

RUN yum remove -y git || true

#COPY ./docker/mats-security/il5/java.security /usr/lib/jvm/jre/lib/security/java.security
#RUN chmod 644 /usr/lib/jvm/jre/lib/security/java.security

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} AS prod-build

COPY ./docker/mats-confluent/confluent-7.2.0/bin/schema-registry-run-class /docker-build/usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/bin/schema-registry-start /docker-build/usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/bin/schema-registry-stop /docker-build/usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/bin/schema-registry-stop-service /docker-build/usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/kafka-serde-tools /docker-build/usr/share/java/kafka-serde-tools
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/cp-base-new /docker-build/usr/share/java/cp-base-new
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/rest-utils /docker-build/usr/share/java/rest-utils
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/confluent-common /docker-build/usr/share/java/confluent-common
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/schema-registry /docker-build/usr/share/java/schema-registry
COPY ./docker/mats-confluent/cp-schema-registry/docker /docker-build/etc/confluent/docker/

FROM base as prod-install

COPY --from=prod-build /docker-build/etc /etc/
COPY --from=prod-build /docker-build/usr /usr/

RUN echo "===> Setting up ${COMPONENT} dirs" \
    && mkdir -p /etc/${COMPONENT}/secrets /usr/logs \
    && chown 1000:1000 -R /etc/${COMPONENT} /usr/logs /etc/confluent/docker \
    && chmod -R ug+w /etc/${COMPONENT} /etc/${COMPONENT}/secrets /etc/confluent/docker \
    && chmod -R 755 /usr/bin && chmod -R 755 /usr/share/java

FROM prod-install as final

VOLUME ["/etc/schema-registry/secrets"]

EXPOSE 8081

ENV JAVA_OPTS=""

CMD ["/etc/confluent/docker/run"]