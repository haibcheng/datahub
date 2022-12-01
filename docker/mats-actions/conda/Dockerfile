ARG FIPS=nonfips
ARG OSAAS_VERSION=1.9.202207

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV PATH="/home/rhel/miniconda3/bin:${PATH}"
ARG PATH="/home/rhel/miniconda3/bin:${PATH}"

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

RUN chown -R rhel /home/rhel/datahub-actions \
    && chmod +x /home/rhel/datahub-actions/setup.py \
    && chown rhel /home/rhel/start_datahub_actions.sh \
    && chmod a+x /home/rhel/start_datahub_actions.sh

RUN mkdir -p /home/rhel/actions/conf  \
    && mkdir -p /home/rhel/actions/logs

COPY datahub-actions/config /home/rhel/actions/conf

RUN chown -R rhel /home/rhel/actions

USER rhel
RUN wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-$(arch).sh \
    && mkdir /home/rhel/.conda \
    && bash Miniconda3-latest-Linux-$(arch).sh -b \
    && rm -f Miniconda3-latest-Linux-$(arch).sh

RUN python3 -m ensurepip --upgrade \
    && python3 -m pip install --upgrade pip wheel setuptools \
    && python3 -m pip install --upgrade acryl-datahub \
    && python3 -m pip install --upgrade acryl-datahub-actions \
    && pip install 'acryl-datahub-actions[executor]'

RUN cd /home/rhel/datahub-actions  \
    && pip install . \
    && cd .. && rm -rf datahub-actions

USER root

STOPSIGNAL SIGINT

CMD dockerize -wait http://$DATAHUB_GMS_HOST:$DATAHUB_GMS_PORT/health -timeout 240s /home/rhel/start_datahub_actions.sh
