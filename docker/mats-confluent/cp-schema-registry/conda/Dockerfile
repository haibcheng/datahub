ARG FIPS=nonfips
ARG OSAAS_VERSION=1.14.202212

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV PATH="/home/rhel/miniconda3/bin:${PATH}"
ARG PATH="/home/rhel/miniconda3/bin:${PATH}"

ENV COMPONENT=schema-registry
RUN yum install -y WBXjava-1.8.0-openjdk-hardening WBXFIPSJar wget

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
    && chmod -R 755 /usr/bin && chmod -R 755 /usr/share/java \
    && dnf -y install hostname

USER rhel

RUN wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-$(arch).sh \
    && mkdir /home/rhel/.conda \
    && bash Miniconda3-latest-Linux-$(arch).sh -b \
    && rm -f Miniconda3-latest-Linux-$(arch).sh \
    && conda install git

RUN python3 -m ensurepip --upgrade \
    && python3 -m pip install --upgrade pip wheel setuptools

ENV PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC=git+https://github.com/confluentinc/confluent-docker-utils@v0.0.52
RUN python3 -m pip install --upgrade "${PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC}"

EXPOSE 8081

ENV JAVA_OPTS=""

ENTRYPOINT ["/usr/local/bin/dumb-init","/main.sh"]

CMD ["/etc/confluent/docker/run"]