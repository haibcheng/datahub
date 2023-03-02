ARG FIPS=nonfips
ARG OSAAS_VERSION=1.16.202302

FROM registry-rmc.webex.com/mats/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV PATH="/home/rhel/miniconda3/bin:${PATH}"
ARG PATH="/home/rhel/miniconda3/bin:${PATH}"

ENV DATAHUB_ACTIONS_HOME=/opt/datahub/actions
ENV DATAHUB_ACTIONS_CONF=/etc/datahub/actions

RUN yum install -y WBXjava-1.8.0-openjdk-hardening WBXFIPSJar curl wget

ENV DR_VERSION v0.6.1
RUN if [ $(arch) = "aarch64" ]; then \
      DR_ARCH='aarch64';\
    elif [ $(arch) = "x86_64" ]; then \
      DR_ARCH='amd64'; \
    else \
      echo >&2 "Unsupported architecture $(arch)" ; exit 1; \
    fi \
    && curl -L https://github.com/treff7es/dockerize/releases/download/$DR_VERSION/dockerize-linux-${DR_ARCH}-$DR_VERSION.tar.gz | tar -C /usr/local/bin -xzv

COPY datahub-actions/src/wap_actions /home/rhel/datahub-actions/src/wap_actions
COPY datahub-actions/setup.py /home/rhel/datahub-actions/setup.py
COPY docker/mats-actions/start.sh /home/rhel/start_datahub_actions.sh

RUN chown -R 1000:1000 /home/rhel/datahub-actions \
    && chmod +x /home/rhel/datahub-actions/setup.py \
    && chown 1000:1000 /home/rhel/start_datahub_actions.sh \
    && chmod a+x /home/rhel/start_datahub_actions.sh

RUN mkdir -p $DATAHUB_ACTIONS_HOME \
    && mkdir -p $DATAHUB_ACTIONS_CONF  \
    && mkdir -p /tmp/datahub/logs/actions/system

COPY datahub-actions/config $DATAHUB_ACTIONS_CONF
COPY docker/mats-actions/executor.yaml $DATAHUB_ACTIONS_CONF

RUN chown -R 1000:1000 $DATAHUB_ACTIONS_HOME \
    && chown -R 1000:1000 $DATAHUB_ACTIONS_CONF \
    && chmod -R 755 $DATAHUB_ACTIONS_CONF \
    && chown -R 1000:1000 /tmp/datahub

USER rhel
RUN wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-$(arch).sh \
    && mkdir /home/rhel/.conda \
    && bash Miniconda3-latest-Linux-$(arch).sh -b \
    && rm -f Miniconda3-latest-Linux-$(arch).sh

RUN python3 -m ensurepip --upgrade \
    && python3 -m pip install --upgrade pip wheel setuptools \
    && python3 -m pip install --upgrade acryl-datahub==v0.9.5 \
    && python3 -m pip install --upgrade acryl-datahub-actions==v0.0.10 \
    && pip install 'acryl-datahub-actions[executor]'

RUN cd /home/rhel/datahub-actions  \
    && pip install . \
    && cd .. && rm -rf datahub-actions

USER root

STOPSIGNAL SIGINT

CMD ["/bin/sh", "-c", "dockerize -wait http://$DATAHUB_GMS_HOST:$DATAHUB_GMS_PORT/health -timeout 240s /home/rhel/start_datahub_actions.sh"]