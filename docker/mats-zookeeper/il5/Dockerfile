ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

RUN yum install -y java-1.8.0 WBXFIPSJar WBXzookeeper

COPY ./docker/mats-zookeeper/il5/java.security /usr/lib/jvm/jre/lib/security/java.security
COPY ./docker/mats-zookeeper/il5/zoo.cfg /opt/apache-zookeeper-3.6.3/conf/zoo.cfg
COPY ./docker/mats-zookeeper/il5/log4j.properties /opt/apache-zookeeper-3.6.3/conf/log4j.properties
COPY ./docker/mats-zookeeper/il5/zk_run.sh /opt/apache-zookeeper-3.6.3/bin/zk_run.sh

RUN mkdir -p /var/zookeeper/data /var/zookeeper/datalog \
    && chmod 644 /usr/lib/jvm/jre/lib/security/java.security \
    && chmod 644 /opt/apache-zookeeper-3.6.3/conf/zoo.cfg \
    && chmod -R 755 /opt/apache-zookeeper-3.6.3 \
    && chown -R 1000:1000 /var/zookeeper

VOLUME ["/var/zookeeper/data", "/var/zookeeper/datalog", "/etc/zookeeper/secrets"]

EXPOSE 2281 2888 3888

ENV JAVA_OPTS=""

CMD ["/opt/apache-zookeeper-3.6.3/bin/zk_run.sh"]