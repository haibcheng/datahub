ARG FIPS=nonfips
ARG OSAAS_VERSION=1.14.202212

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV COMPONENT=schema-registry
RUN yum install -y WBXjava-1.8.0-openjdk-hardening WBXFIPSJar python3.9 python39-pip python39-setuptools git

ENV PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC=git+https://github.com/confluentinc/confluent-docker-utils@v0.0.52
RUN python3.9 -m pip install --prefer-binary --prefix=/usr/local --upgrade "${PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC}"

RUN yum remove -y git || true

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

RUN echo "===> Setting up ${COMPONENT} dirs" \
    && mkdir -p /etc/${COMPONENT}/secrets /usr/logs \
    && chown -R 1000:1000 /etc/${COMPONENT} /usr/logs /etc/confluent/docker \
    && chmod -R ug+w /etc/${COMPONENT} /etc/confluent/docker \
    && chmod -R 755 /usr/bin && chmod -R 755 /usr/share/java

EXPOSE 8082

ENV JAVA_OPTS=""

CMD ["/etc/confluent/docker/run"]