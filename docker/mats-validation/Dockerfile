ARG FIPS=nonfips
ARG OSAAS_VERSION=1.14.202212

FROM containers.cisco.com/rhel8/osaas-${FIPS}:${OSAAS_VERSION} as base

ENV PATH="/home/rhel/miniconda3/bin:${PATH}"
ARG PATH="/home/rhel/miniconda3/bin:${PATH}"

RUN yum install -y WBXjava-1.8.0-openjdk-hardening WBXFIPSJar curl wget

USER rhel
RUN wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-$(arch).sh \
    && mkdir /home/rhel/.conda \
    && bash Miniconda3-latest-Linux-$(arch).sh -b \
    && rm -f Miniconda3-latest-Linux-$(arch).sh

RUN python3 -m ensurepip --upgrade
RUN python3 -m pip install --upgrade pip wheel setuptools
RUN python3 -m pip install --upgrade acryl-datahub
RUN python3 -m pip install --upgrade great_expectations
RUN python3 -m pip install --upgrade 'acryl-datahub[great-expectations]'

RUN python3 -m pip install --upgrade sqlalchemy
RUN python3 -m pip install --upgrade psycopg2-binary
RUN python3 -m pip install --upgrade sqlalchemy-redshift
RUN python3 -m pip install --upgrade pyodbc
RUN python3 -m pip install --upgrade PyMySQL
RUN python3 -m pip install --upgrade trino
RUN python3 -m pip install --upgrade sqlalchemy-bigquery
RUN python3 -m pip install --upgrade snowflake-connector-python snowflake-sqlalchemy
RUN python3 -m pip install --upgrade presto-python-client
RUN python3 -m pip install --upgrade oracledb
#RUN python3 -m pip install --upgrade pinotdb

USER root

COPY ./datahub-actions/great_expectations /home/rhel/great_expectations
RUN rm -f /home/rhel/great_expectations/expectations/.ge_store_backend_id \
    && rm -rf /home/rhel/great_expectations/uncommitted/validations/*

COPY ./docker/mats-validation/start.sh /home/rhel/start.sh
RUN chown -R rhel:rhel /home/rhel/great_expectations \
    && chown rhel:rhel /home/rhel/start.sh \
    && chmod +x /home/rhel/start.sh

CMD ["/bin/sh", "-c", "/home/rhel/start.sh"]