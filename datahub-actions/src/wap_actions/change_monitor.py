import logging
import json
from datahub_actions.action.action import Action
from datahub_actions.pipeline.pipeline_context import PipelineContext
from wap_actions.core.matcher import EntityTypeMatcher, PropValueMatcher
from wap_actions.core.configuration import UrlNotificationConfig
from wap_actions.core.action import CommonAction
from wap_actions.service.notification import CudNotificationService, UdpNotificationService

logger = logging.getLogger(__name__)


class CudDatasourceRefreshAction(CommonAction):

    def _matchers(self):
        return EntityTypeMatcher(config=self.config)

    def _resolve_url(self, urn: str):
        for c in self.config.callbacks:
            if urn.upper().endswith("," + c.region.upper() + ")"):
                return c.url
        return None

    def _send_notification(self, url: str, urns: list[str]):
        urns_str = ','.join(urns)
        try:
            logger.info("%s >> the change[%s] is being notified...", type(self).__name__, urns_str)
            self.notification.notify(url=url, urns=urns)
            logger.info("%s >> the change[%s] has been notified!", type(self).__name__, urns_str)
            self.call_interval_reset()
        except Exception as ex:
            logger.error('%s >> failed to notify the change[%s] -> %s', type(self).__name__, urns_str, repr(ex))
            self._urn_dict.add_elements(urns)
            self.call_interval_add(self.CALL_INTERVAL)

    def __init__(self, config: UrlNotificationConfig, ctx: PipelineContext):
        super().__init__(config=config, ctx=ctx)
        self.notification = CudNotificationService(config.ci_config)

    @classmethod
    def create(cls, config_dict: dict, ctx: PipelineContext) -> "Action":
        action_config = UrlNotificationConfig.parse_obj(config_dict or {})
        return cls(action_config, ctx)


class CudDatasetCacheRefreshAction(CommonAction):

    def _matchers(self):
        return PropValueMatcher(config=self.config, aspect_name="datasetProperties")

    def _resolve_url(self, urn: str):
        return self.config.callbacks[0].url

    def _send_notification(self, url: str, urns: list[str]):
        for urn in urns:
            urn_value = json.loads(self._urn_dict.value_of(urn))
            table_name = urn_value.get("TABLE_NAME")
            target_url = url + table_name
            try:
                logger.info("%s >> the change[%s] is being notified...", type(self).__name__, urn)
                self.notification.notify(url=target_url)
                logger.info("%s >> the change[%s] has been notified!", type(self).__name__, urn)
                self.call_interval_reset()
            except Exception as ex:
                logger.error('%s >> failed to notify the change[%s] -> %s', type(self).__name__, urn, repr(ex))
                self._urn_dict.add_elements(urn)
                self.call_interval_add(self.CALL_INTERVAL)

    def __init__(self, config: UrlNotificationConfig, ctx: PipelineContext):
        super().__init__(config=config, ctx=ctx)
        self.notification = CudNotificationService(config.ci_config)

    @classmethod
    def create(cls, config_dict: dict, ctx: PipelineContext) -> "Action":
        action_config = UrlNotificationConfig.parse_obj(config_dict or {})
        return cls(action_config, ctx)


class UdpNodeStatusAction(CommonAction):

    def _matchers(self):
        return PropValueMatcher(config=self.config, aspect_name="datasetProperties")

    def _resolve_url(self, urn: str):
        return self.config.callbacks[0].url

    def _send_notification(self, url: str, urns: list[str]):
        for urn in urns:
            urn_value = json.loads(self._urn_dict.value_of(urn))
            node = urn_value.get("udp_callback_nodeid")
            status = urn_value.get("udp_callback_status")
            target_url = url + "?nodeId=" + node + "&status=" + status
            try:
                logger.info("%s >> the change[%s] is being notified...", type(self).__name__, urn)
                self.notification.notify(url=target_url)
                logger.info("%s >> the change[%s] has been notified!", type(self).__name__, urn)
                self.call_interval_reset()
            except Exception as ex:
                logger.error('%s >> failed to notify the change[%s] -> %s', type(self).__name__, urn, repr(ex))
                self._urn_dict.add_elements(urn)
                self.call_interval_add(self.CALL_INTERVAL)

    def __init__(self, config: UrlNotificationConfig, ctx: PipelineContext):
        super().__init__(config=config, ctx=ctx)
        self.notification = UdpNotificationService()

    @classmethod
    def create(cls, config_dict: dict, ctx: PipelineContext) -> "Action":
        action_config = UrlNotificationConfig.parse_obj(config_dict or {})
        return cls(action_config, ctx)
