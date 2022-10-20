ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV ZK_VERSION=3.7.1
RUN yum install -y WBXjava-1.8.0-openjdk-hardening WBXFIPSJar

FROM base as download

RUN yum install -y wget

RUN cd /root \
    && wget https://dlcdn.apache.org/zookeeper/zookeeper-$ZK_VERSION/apache-zookeeper-$ZK_VERSION-bin.tar.gz -O apache-zookeeper.tar.gz \
    && tar -zxvf apache-zookeeper.tar.gz

FROM base as  final

COPY --from=download /root/apache-zookeeper-$ZK_VERSION-bin /opt/apache-zookeeper
COPY ./docker/mats-zookeeper/il5/java.security /usr/lib/jvm/jre/lib/security/java.security
COPY ./docker/mats-zookeeper/il5/zoo.cfg /opt/apache-zookeeper/conf/zoo.cfg
COPY ./docker/mats-zookeeper/il5/zkRun.sh /opt/apache-zookeeper/bin/zkRun.sh

RUN echo "===> Setting up zookeeper dirs" \
    && mkdir -p /var/zookeeper/data /var/zookeeper/datalog /var/zookeeper/logs \
    && chmod 644 /usr/lib/jvm/jre/lib/security/java.security \
    && chmod 644 /opt/apache-zookeeper/conf/zoo.cfg \
    && chmod -R 755 /opt/apache-zookeeper/bin \
    && chown -R 1000:1000 /var/zookeeper

VOLUME ["/var/zookeeper/data", "/var/zookeeper/datalog", "/var/zookeeper/logs", "/etc/zookeeper/secrets"]

EXPOSE 2281 2888 3888

ENV JAVA_OPTS=""

CMD ["/opt/apache-zookeeper/bin/zkRun.sh"]