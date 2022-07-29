ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV COMPONENT=kafka
RUN yum install -y java-1.8.0 WBXFIPSJar python3.9 python39-pip python39-setuptools git

ENV PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC=git+https://github.com/confluentinc/confluent-docker-utils@v0.0.52
RUN python3.9 -m pip install --prefer-binary --prefix=/usr/local --upgrade "${PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC}"

RUN yum remove -y git || true

COPY ./docker/mats-zookeeper/il5/java.security /usr/lib/jvm/jre/lib/security/java.security
RUN chmod 644 /usr/lib/jvm/jre/lib/security/java.security

COPY ./docker/mats-confluent/confluent-7.2.0/bin/kafka-run-class /usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/bin/kafka-server-start /usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/bin/kafka-server-stop /usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/kafka /usr/share/java/kafka
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/cp-base-new /usr/share/java/cp-base-new
COPY ./docker/mats-confluent/cp-kafka/docker /etc/confluent/docker/
COPY ./docker/mats-confluent/cp-kafka-il5/ka_run.sh /etc/confluent/docker/

RUN echo "===> Setting up ${COMPONENT} dirs" \
    && mkdir -p /usr/logs /var/lib/kafka/data /etc/kafka/secrets \
    && chown -R 1000:1000 /usr/logs /var/lib/kafka /etc/kafka /etc/confluent/docker \
    && chmod -R ug+w /etc/kafka /etc/confluent/docker \
    && chmod -R 755 /usr/bin && chmod -R 755 /usr/share/java

VOLUME ["/var/lib/kafka/data", "/etc/kafka/secrets"]

EXPOSE 9093

ENV JAVA_OPTS=""

CMD ["/etc/confluent/docker/ka_run.sh"]