import json
import logging
from time import sleep
from typing import Optional
from threading import Thread
from pydantic import BaseModel

from datahub_actions.action.action import Action
from datahub_actions.event.event_envelope import EventEnvelope
from datahub_actions.pipeline.pipeline_context import PipelineContext
from wap_actions.atomic.atomic_int import AtomicInteger
from wap_actions.atomic.atomic_set import AtomicSet
from wap_actions.service.ci_token import CITokenService
from wap_actions.service.cu_dashboard import CustomDashboardService

logger = logging.getLogger(__name__)


class DatasourceMonitorConfig(BaseModel):
    output_json: Optional[bool]
    access_token_url: str
    username: str
    password: str
    bearer_token_url: str
    machine_account_name: str
    machine_account_pass: str
    datasource_refresh_api: str


class DatasourceMonitorAction(Action):
    @classmethod
    def create(cls, config_dict: dict, ctx: PipelineContext) -> "Action":
        action_config = DatasourceMonitorConfig.parse_obj(config_dict or {})
        return cls(action_config, ctx)

    def __init__(self, config: DatasourceMonitorConfig, ctx: PipelineContext):
        self.config = config
        ci_token_s = CITokenService(
            access_token_url=self.config.access_token_url,
            username=self.config.username,
            password=self.config.password,
            bearer_token_url=self.config.bearer_token_url,
            machine_act_name=self.config.machine_account_name,
            machine_act_pass=self.config.machine_account_pass
        )
        self.cu_dashboard = CustomDashboardService(
            token_service=ci_token_s,
            refresh_api=self.config.datasource_refresh_api
        )
        self._counter = AtomicInteger()
        self._urns = AtomicSet()
        self._urns.add("__first_run__")
        self._close = False
        self._thread = Thread(target=self._notify_change)
        self._thread.start()
        logger.info("The thread[notify_change] has been started.")

    def act(self, event: EventEnvelope) -> None:
        event_json = json.loads(event.as_json()).get("event")
        entity_type = event_json.get("entityType")
        if entity_type is None or entity_type != 'datasource':
            return
        if self.config.output_json:
            message = json.dumps(json.loads(event.as_json()), indent=4)
            logger.info(message)
        entity_urn = event_json.get("entityUrn")
        if entity_urn is not None:
            self._urns.add(entity_urn)
            logger.info("Received datasource -> %s", entity_urn)

    def close(self) -> None:
        self._close = True
        self._thread.join()
        logger.info("The thread[notify_change] has been stopped.")

    def _notify_change(self):
        while not self._close:
            sleep(1)
            if self._counter.increment_get() < 30:
                continue
            self._counter.reset()
            n_set = self._urns.copy_of()
            if len(n_set) == 0:
                continue
            if len(n_set) > 1:
                n_set.remove("__first_run__")
            urns = ', '.join(n_set)
            try:
                logger.info("The change[%s] is being notified...", urns)
                self.cu_dashboard.change_notify()
                logger.info("The change[%s] has been notified!", urns)
            except Exception as error:
                self._urns.add_set(n_set)
                logger.error('Failed to notify the change[%s] -> %s', urns, repr(error))
