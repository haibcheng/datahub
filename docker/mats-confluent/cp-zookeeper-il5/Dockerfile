ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV COMPONENT=zookeeper
RUN yum install -y java-1.8.0 WBXFIPSJar python3.9 python39-pip python39-setuptools git

ENV PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC=git+https://github.com/confluentinc/confluent-docker-utils@v0.0.52
RUN python3.9 -m pip install --prefer-binary --prefix=/usr/local --upgrade "${PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC}"

RUN yum remove -y git || true

COPY ./docker/mats-security/il5/java.security /usr/lib/jvm/jre/lib/security/java.security
RUN chmod 644 /usr/lib/jvm/jre/lib/security/java.security

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} AS prod-build

COPY ./docker/mats-confluent/confluent-7.2.0/bin/kafka-run-class /docker-build/usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/bin/zookeeper-server-start /docker-build/usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/bin/zookeeper-server-stop /docker-build/usr/bin/
COPY ./docker/mats-confluent/confluent-7.2.0/share/java/kafka /docker-build/usr/share/java/kafka
COPY ./docker/mats-confluent/cp-zookeeper/docker /docker-build/etc/confluent/docker/
COPY ./docker/mats-confluent/cp-zookeeper-il5/zk_run.sh /docker-build/etc/confluent/docker/

FROM base as prod-install

COPY --from=prod-build /docker-build/etc /etc/
COPY --from=prod-build /docker-build/usr /usr/

RUN mkdir -p /usr/logs /etc/kafka /var/lib/zookeeper/data /var/lib/zookeeper/log /etc/zookeeper/secrets \
    && chown -R 1000:1000 /usr/logs /etc/kafka /var/lib/zookeeper /etc/zookeeper /etc/confluent/docker \
    && chmod -R ug+w /etc/kafka /var/lib/zookeeper /etc/zookeeper /etc/confluent/docker \
    && chmod -R 755 /usr/bin && chmod -R 755 /usr/share/java

FROM prod-install as final

VOLUME ["/var/lib/zookeeper/data", "/var/lib/zookeeper/log", "/etc/zookeeper/secrets"]

EXPOSE 2281 2888 3888

ENV JAVA_OPTS=""

CMD ["/etc/confluent/docker/zk_run.sh"]