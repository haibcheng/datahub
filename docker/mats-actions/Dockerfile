ARG FIPS=nonfips
ARG OSAAS_VERSION=1.14.202212

FROM registry-rmc.webex.com/mats/rhel8-osaas-${FIPS}:${OSAAS_VERSION} as base

RUN yum install -y WBXjava-1.8.0-openjdk-hardening WBXFIPSJar python39 curl

ENV DATAHUB_ACTIONS_HOME=/opt/datahub/actions
ENV DATAHUB_ACTIONS_CONF=/etc/datahub/actions

ENV DR_VERSION v0.6.1
RUN if [ $(arch) = "aarch64" ]; then \
      DR_ARCH='aarch64';\
    elif [ $(arch) = "x86_64" ]; then \
      DR_ARCH='amd64'; \
    else \
      echo >&2 "Unsupported architecture $(arch)" ; exit 1; \
    fi \
    && curl -L https://github.com/treff7es/dockerize/releases/download/$DR_VERSION/dockerize-linux-${DR_ARCH}-$DR_VERSION.tar.gz | tar -C /usr/local/bin -xzv

RUN python3 -m ensurepip --upgrade \
    && python3 -m pip install --upgrade pip wheel setuptools \
    && python3 -m pip install --upgrade acryl-datahub==v0.9.5 \
    && python3 -m pip install --upgrade acryl-datahub-actions==v0.0.10 \
    && pip install 'acryl-datahub-actions[executor]'

COPY datahub-actions/src/wap_actions /datahub-actions/src/wap_actions
COPY datahub-actions/setup.py /datahub-actions/setup.py
COPY docker/mats-actions/start.sh /start_datahub_actions.sh

RUN chmod +x /datahub-actions/setup.py \
    && cd /datahub-actions  \
    && pip install . \
    && cd .. && rm -rf /datahub-actions \
    && chmod a+x /start_datahub_actions.sh

RUN mkdir -p $DATAHUB_ACTIONS_HOME \
    && mkdir -p $DATAHUB_ACTIONS_CONF  \
    && mkdir -p /tmp/datahub/logs/actions/system

COPY datahub-actions/config $DATAHUB_ACTIONS_CONF
COPY docker/mats-actions/executor.yaml $DATAHUB_ACTIONS_CONF/system/

RUN chown -R rhel:rhel $DATAHUB_ACTIONS_HOME \
    && chmod -R 755 $DATAHUB_ACTIONS_CONF \
    && chown -R rhel:rhel /tmp/datahub

RUN mkdir -p /home/rhel/.cache \
    && mv /root/.cache/pip /home/rhel/.cache/pip \
    && chown -R rhel /home/rhel/.cache/pip

STOPSIGNAL SIGINT

CMD dockerize -wait http://$DATAHUB_GMS_HOST:$DATAHUB_GMS_PORT/health -timeout 240s /start_datahub_actions.sh
