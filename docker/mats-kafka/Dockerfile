ARG FIPS=nonfips
ARG OSAAS_VERSION=1.16.202302

FROM registry-rmc.webex.com/mats/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV KAFKA_VERSION=3.3.2
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
COPY ./docker/mats-kafka/log4j.properties /opt/kafka/config/log4j.properties

RUN chown -R 1000:1000 /opt/kafka && chmod -R 755 /opt/kafka/bin

EXPOSE 9092

ENV JAVA_OPTS=""

CMD ["/opt/kafka/bin/kafkaRun.sh"]