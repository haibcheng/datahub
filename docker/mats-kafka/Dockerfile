ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV KAFKA_VERSION=3.3.1
RUN yum install -y WBXjava-1.8.0-openjdk-hardening WBXFIPSJar

FROM base as download

RUN yum install -y wget

RUN cd /root \
    && wget https://downloads.apache.org/kafka/$KAFKA_VERSION/kafka_2.13-$KAFKA_VERSION.tgz -O kafka.tgz \
    && tar -zxvf kafka.tgz

FROM base as  final

COPY --from=download /root/kafka_2.13-$KAFKA_VERSION /opt/kafka
COPY ./docker/mats-kafka/kafkaRun.sh /opt/kafka/bin/kafkaRun.sh
COPY ./docker/mats-kafka/server.properties /opt/kafka/config/server.properties

RUN echo "===> Setting up kafka dirs" \
    && mkdir -p /var/kafka/data /var/kafka/logs \
    && chown -R 1000:1000 /opt/kafka  \
    && chmod -R 755 /opt/kafka/bin \
    && chown -R 1000:1000 /var/kafka

VOLUME ["/var/kafka/data", "/var/kafka/logs"]

EXPOSE 9092

ENV JAVA_OPTS=""

CMD ["/opt/kafka/bin/kafkaRun.sh"]