ARG FIPS=nonfips
ARG OSAAS_VERSION=1.16.202302

FROM registry-rmc.webex.com/mats/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV ZK_VERSION=3.7.1
RUN yum install -y WBXjava-1.8.0-openjdk-hardening WBXFIPSJar

FROM base as download

RUN yum install -y wget

RUN cd /root \
    && wget https://dlcdn.apache.org/zookeeper/zookeeper-$ZK_VERSION/apache-zookeeper-$ZK_VERSION-bin.tar.gz -O apache-zookeeper.tar.gz \
    && tar -zxvf apache-zookeeper.tar.gz

FROM base as  final

COPY --from=download /root/apache-zookeeper-$ZK_VERSION-bin /opt/apache-zookeeper
COPY ./docker/mats-zookeeper/zkRun.sh /opt/apache-zookeeper/bin/zkRun.sh
COPY ./docker/mats-zookeeper/zoo.cfg /opt/apache-zookeeper/conf/zoo.cfg
COPY ./docker/mats-zookeeper/log4j.properties /opt/apache-zookeeper/conf/log4j.properties

RUN echo "===> Setting up zookeeper dirs" \
    && mkdir -p /var/zookeeper/data /var/zookeeper/datalog /opt/apache-zookeeper/logs \
    && chown -R rhel:rhel /opt/apache-zookeeper/conf /opt/apache-zookeeper/logs \
    && chmod -R 755 /opt/apache-zookeeper/conf \
    && chmod -R 755 /opt/apache-zookeeper/bin \
    && chown -R rhel:rhel /var/zookeeper

VOLUME ["/var/zookeeper/data", "/var/zookeeper/datalog"]

EXPOSE 2181 2888 3888

ENV JAVA_OPTS=""

CMD ["/opt/apache-zookeeper/bin/zkRun.sh"]