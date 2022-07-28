ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

RUN yum install -y java-1.8.0 WBXFIPSJar WBXzookeeper

COPY ./docker/mats-zookeeper/il5/java.security /usr/lib/jvm/jre/lib/security/java.security
COPY ./docker/mats-zookeeper/il5/zoo.cfg /opt/apache-zookeeper-3.6.3/conf/zoo.cfg
COPY ./docker/mats-zookeeper/il5/zk_run.sh /opt/apache-zookeeper-3.6.3/bin/zk_run.sh

RUN chmod 644 /usr/lib/jvm/jre/lib/security/java.security \
    && chmod 644 /opt/apache-zookeeper-3.6.3/conf/zoo.cfg \
    && chmod 644 /opt/apache-zookeeper-3.6.3/bin/zk_run.sh

FROM base as final

VOLUME ["/var/lib/zookeeper/data", "/var/lib/zookeeper/log", "/etc/zookeeper/secrets"]

EXPOSE 2281 2888 3888

ENV JAVA_OPTS=""

CMD ["/opt/apache-zookeeper-3.6.3/bin/zk_run.sh"]