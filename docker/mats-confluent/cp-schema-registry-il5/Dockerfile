ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM registry-rmc.webex.com/mats/rhel8-osaas-${FIPS}:${OSAAS_VERSION} as base

ENV COMPONENT=schema-registry
RUN yum install -y WBXjava-1.8.0-openjdk-hardening WBXFIPSJar python3.9 python39-pip python39-setuptools git

ENV PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC=git+https://github.com/confluentinc/confluent-docker-utils@v0.0.52
RUN python3.9 -m pip install --prefer-binary --prefix=/usr/local --upgrade "${PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC}"

RUN yum remove -y git || true

COPY ./docker/mats-zookeeper/il5/java.security /usr/lib/jvm/jre/lib/security/java.security
RUN chmod 644 /usr/lib/jvm/jre/lib/security/java.security

COPY ./docker/mats-confluent/confluent-7.2.0/bin/schema-registry-run-class /usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/bin/schema-registry-start /usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/bin/schema-registry-stop /usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/bin/schema-registry-stop-service /usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/kafka-serde-tools /usr/share/java/kafka-serde-tools
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/cp-base-new /usr/share/java/cp-base-new
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/rest-utils /usr/share/java/rest-utils
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/confluent-common /usr/share/java/confluent-common
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/schema-registry /usr/share/java/schema-registry
COPY ./docker/mats-confluent/cp-schema-registry/docker /etc/confluent/docker/
COPY ./docker/mats-confluent/cp-schema-registry-il5/sr_run.sh /etc/confluent/docker/

RUN echo "===> Setting up ${COMPONENT} dirs" \
    && mkdir -p /etc/${COMPONENT}/secrets /usr/logs \
    && chown 1000:1000 -R /etc/${COMPONENT} /usr/logs /etc/confluent/docker \
    && chmod -R ug+w /etc/${COMPONENT} /etc/confluent/docker \
    && chmod -R 755 /usr/bin && chmod -R 755 /usr/share/java

VOLUME ["/etc/schema-registry/secrets"]

EXPOSE 8082

ENV JAVA_OPTS=""

CMD ["/etc/confluent/docker/sr_run.sh"]