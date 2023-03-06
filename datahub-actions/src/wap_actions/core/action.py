import json
import logging
from abc import ABC, abstractmethod
from time import sleep
from threading import Thread
from datahub_actions.action.action import Action
from datahub_actions.event.event_envelope import EventEnvelope
from datahub_actions.pipeline.pipeline_context import PipelineContext
from wap_actions.core.atomic import AtomicInteger, AtomicDict
from wap_actions.core.cache import FileCache
from wap_actions.core.configuration import UrlNotificationConfig

logger = logging.getLogger(__name__)


class CommonAction(Action, ABC):

    SEPARATOR = "=/\\="
    CALL_INTERVAL = 60

    def act(self, event: EventEnvelope) -> None:
        event_json = json.loads(event.as_json()).get("event")
        re, target = self.matcher.matches(event_json)
        if not re:
            return
        if self.config.output_json:
            message = json.dumps(json.loads(event.as_json()), indent=4)
            logger.info(message)
        entity_urn = event_json.get("entityUrn")
        if entity_urn is not None:
            if target is None:
                self._urn_dict.add(key=entity_urn)
            else:
                self._urn_dict.add(key=entity_urn, value=json.dumps(target))
            self.call_interval_reset()
            logger.info("Received datasource -> %s", entity_urn)

    def close(self) -> None:
        self._close = True
        self._thread.join()
        self._cache.persist()
        logger.info("The thread[notify_change] has been stopped.")

    def _notify_change(self):
        while not self._close:
            sleep(1)
            if self._counter.increment_get() < self._call_interval:
                continue
            self._counter.reset()
            n_set = self._urn_dict.fetch_elements()
            if len(n_set) == 0:
                continue
            urls = dict()
            for e_urn in n_set:
                e_url = self._resolve_url(e_urn)
                if e_url is None:
                    logger.warning('Cannot find url for urn[%s]', e_urn)
                    continue
                if e_url not in urls.keys():
                    urls[e_url] = list()
                urls[e_url].append(e_urn)
            for e_url in urls.keys():
                self._send_notification(url=e_url, urns=urls[e_url])

    @abstractmethod
    def _matchers(self):
        pass

    @abstractmethod
    def _resolve_url(self, urn: str):
        pass

    @abstractmethod
    def _send_notification(self, url: str, urns: list[str]):
        pass

    def call_interval_add(self, num: int):
        if num < 0 or self._call_interval > 24 * 60 * 60:
            return
        self._call_interval += num

    def call_interval_reset(self):
        self._call_interval = self.CALL_INTERVAL

    def __init__(self, config: UrlNotificationConfig, ctx: PipelineContext):
        self.config = config
        for matcher in self._matchers():
            if matcher.matches_type():
                self.matcher = matcher
        self._cache = FileCache(config.actions_home + "/cache", ctx.pipeline_name + ".txt")
        self._counter = AtomicInteger()
        self._call_interval = self.CALL_INTERVAL
        self._urn_dict = AtomicDict(unique_value=True, cache=self._cache, separator=self.SEPARATOR)
        self._close = False
        self._thread = Thread(target=self._notify_change)
        self._thread.start()
        logger.info("The thread[notify_change] has been started.")
