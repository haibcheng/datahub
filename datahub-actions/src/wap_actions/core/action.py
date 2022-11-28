import json
import logging
from abc import ABC, abstractmethod
from time import sleep
from threading import Thread
from datahub_actions.action.action import Action
from datahub_actions.event.event_envelope import EventEnvelope
from datahub_actions.pipeline.pipeline_context import PipelineContext
from wap_actions.core.atomic import AtomicInteger, AtomicList
from wap_actions.core.cache import FileCache
from wap_actions.core.configuration import UrlNotificationConfig

logger = logging.getLogger(__name__)


class CommonAction(Action, ABC):

    def act(self, event: EventEnvelope) -> None:
        event_json = json.loads(event.as_json()).get("event")
        matches = False
        for matcher in self.matchers:
            if matcher.matches(event_json):
                matches = True
                break
        if not matches:
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

    @abstractmethod
    def _send_notification(self, urns: list[str]):
        pass

    @abstractmethod
    def _matchers(self):
        pass

    def __init__(self, config: UrlNotificationConfig, ctx: PipelineContext):
        self.config = config
        self.matchers = self._matchers()
        self._cache = FileCache(config.cache_root, ctx.pipeline_name + ".txt")
        self._counter = AtomicInteger()
        self._urns = AtomicList(unique_value=True, cache=self._cache)
        self._close = False
        self._thread = Thread(target=self._notify_change)
        self._thread.start()
        logger.info("The thread[notify_change] has been started.")
