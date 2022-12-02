FROM acryldata/datahub-actions:v0.0.8 AS base

USER root

COPY datahub-actions/src/wap_actions /home/datahub/datahub-actions/src/wap_actions
COPY datahub-actions/setup.py /home/datahub/datahub-actions/setup.py

RUN cd /home/datahub/datahub-actions  \
    && pip install --upgrade pip  \
    && pip install . \
    && cd .. && rm -rf datahub-actions

RUN mv /root/.cache/pip /home/datahub/.cache/pip \
    && chown -R datahub /home/datahub/.cache/pip

STOPSIGNAL SIGINT

USER datahub

CMD ["/bin/sh", "-c", "dockerize -wait http://$DATAHUB_GMS_HOST:$DATAHUB_GMS_PORT/health -timeout 240s /start_datahub_actions.sh"]