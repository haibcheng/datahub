import json
import logging
from time import sleep
from typing import Optional
from threading import Thread
from pydantic import BaseModel

from datahub_actions.action.action import Action
from datahub_actions.event.event_envelope import EventEnvelope
from datahub_actions.pipeline.pipeline_context import PipelineContext
from wap_actions.core.atomic import AtomicInteger, AtomicList
from wap_actions.service.ci_token import CITokenConfig
from wap_actions.service.notification import UrlNotificationService
from wap_actions.core.cache import FileCache

logger = logging.getLogger(__name__)


class CallbackApi(BaseModel):
    region: str
    url: str


class UrlNotificationConfig(BaseModel):
    output_json: Optional[bool]
    cache_root: str
    entity_types: list[str]
    ci_config: CITokenConfig
    callbacks: list[CallbackApi]


class UrlNotificationAction(Action):
    @classmethod
    def create(cls, config_dict: dict, ctx: PipelineContext) -> "Action":
        action_config = UrlNotificationConfig.parse_obj(config_dict or {})
        return cls(action_config, ctx)

    def __init__(self, config: UrlNotificationConfig, ctx: PipelineContext):
        self.config = config
        self.notification = UrlNotificationService(
            ci_config=self.config.ci_config
        )
        self._cache = FileCache(config.cache_root, ctx.pipeline_name + ".txt")
        self._counter = AtomicInteger()
        self._urns = AtomicList(unique_value=True, cache=self._cache)
        self._close = False
        self._thread = Thread(target=self._notify_change)
        self._thread.start()
        logger.info("The thread[notify_change] has been started.")

    def act(self, event: EventEnvelope) -> None:
        event_json = json.loads(event.as_json()).get("event")
        entity_type = event_json.get("entityType")
        if entity_type is None or entity_type not in self.config.entity_types:
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
        self._cache.persist()
        logger.info("The thread[notify_change] has been stopped.")

    def _notify_change(self):
        while not self._close:
            sleep(1)
            if self._counter.increment_get() < 30:
                continue
            self._counter.reset()
            n_set = self._urns.fetch_elements()
            if len(n_set) == 0:
                continue
            self._send_notification(n_set)

    def _send_notification(self, urns: list[str]):
        urls = dict()
        for e_urn in urns:
            e_url = self._resolve_url_from_urn(e_urn)
            if e_url is None:
                logger.warning('Cannot find url for urn[%s]', e_urn)
                continue
            if e_url not in urls.keys():
                urls[e_url] = list()
            urls[e_url].append(e_urn)
        for e_url in urls.keys():
            e_urns = urls[e_url]
            urns_str = ','.join(e_urns)
            try:
                logger.info("The change[%s] is being notified...", urns_str)
                self.notification.notify(url=e_url, urns=e_urns)
                logger.info("The change[%s] has been notified!", urns_str)
            except Exception as ex:
                logger.error('Failed to notify the change[%s] -> %s', urns_str, repr(ex))
                self._urns.add_elements(e_urns)

    def _resolve_url_from_urn(self, urn: str):
        for c in self.config.callbacks:
            if urn.upper().endswith("," + c.region.upper() + ")"):
                return c.url
        return None
