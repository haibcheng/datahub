ARG FIPS=nonfips

FROM registry-rmc.webex.com/mats/alma8-${FIPS}:1.4.202303 as base
ENV USER=alma

ENV PATH="/home/$USER/miniconda3/bin:${PATH}"
ARG PATH="/home/$USER/miniconda3/bin:${PATH}"

ENV COMPONENT=schema-registry
RUN yum install -y WBXjava-1.8.0-openjdk-hardening wget

USER $USER

RUN wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-$(arch).sh \
    && mkdir /home/$USER/.conda \
    && bash Miniconda3-latest-Linux-$(arch).sh -b \
    && rm -f Miniconda3-latest-Linux-$(arch).sh \
    && conda install git

RUN python3 -m ensurepip --upgrade \
    && python3 -m pip install --upgrade pip wheel setuptools

ENV PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC=git+https://github.com/confluentinc/confluent-docker-utils@v0.0.52
RUN python3 -m pip install --upgrade "${PYTHON_CONFLUENT_DOCKER_UTILS_INSTALL_SPEC}"

USER root

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
    && chown -R $USER:$USER /etc/${COMPONENT} /usr/logs /etc/confluent/docker \
    && chmod -R ug+w /etc/${COMPONENT} /etc/confluent/docker \
    && chmod -R 755 /usr/bin && chmod -R 755 /usr/share/java \
    && dnf -y install hostname

EXPOSE 8081

ENV JAVA_OPTS=""

CMD ["/etc/confluent/docker/run"]