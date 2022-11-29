import logging
import json
from datahub_actions.action.action import Action
from datahub_actions.pipeline.pipeline_context import PipelineContext
from wap_actions.core.matcher import EntityTypeMatcher, PropValueMatcher
from wap_actions.core.configuration import UrlNotificationConfig
from wap_actions.core.action import CommonAction
from wap_actions.service.notification import CudNotificationService, UdpNotificationService

logger = logging.getLogger(__name__)


class UrlNotificationAction(CommonAction):

    TYPE_CUD = "CUD"
    TYPE_UDP = "UDP"

    def _matchers(self):
        return [
            EntityTypeMatcher(config=self.config, match_type=self.TYPE_CUD),
            PropValueMatcher(config=self.config, match_type=self.TYPE_UDP, aspect_name="datasetProperties")
        ]

    def _resolve_url(self, urn: str):
        if self.config.type == self.TYPE_UDP:
            return self.config.callbacks[0].url
        elif self.config.type == self.TYPE_CUD:
            for c in self.config.callbacks:
                if urn.upper().endswith("," + c.region.upper() + ")"):
                    return c.url
        return None

    def _send_notification(self, url: str, urns: list[str]):
        if self.config.type == self.TYPE_UDP:
            for urn in urns:
                urn_value = json.loads(self._urn_dict.value_of(urn))
                node = urn_value.get("udp_callback_nodeid")
                status = urn_value.get("udp_callback_status")
                target_url = url + "?nodeId=" + node + "&status=" + status
                try:
                    logger.info("The change[%s] is being notified...", urn)
                    self.notification.notify(url=target_url)
                    logger.info("The change[%s] has been notified!", urn)
                except Exception as ex:
                    logger.error('Failed to notify the change[%s] -> %s', urn, repr(ex))
                    self._urn_dict.add_elements(urn)
        elif self.config.type == self.TYPE_CUD:
            urns_str = ','.join(urns)
            try:
                logger.info("The change[%s] is being notified...", urns_str)
                self.notification.notify(url=url, urns=urns)
                logger.info("The change[%s] has been notified!", urns_str)
            except Exception as ex:
                logger.error('Failed to notify the change[%s] -> %s', urns_str, repr(ex))
                self._urn_dict.add_elements(urns)

    def __init__(self, config: UrlNotificationConfig, ctx: PipelineContext):
        super().__init__(config=config, ctx=ctx)
        if self.config.type == self.TYPE_UDP:
            self.notification = UdpNotificationService()
        elif self.config.type == self.TYPE_CUD:
            self.notification = CudNotificationService(config.ci_config)

    @classmethod
    def create(cls, config_dict: dict, ctx: PipelineContext) -> "Action":
        action_config = UrlNotificationConfig.parse_obj(config_dict or {})
        return cls(action_config, ctx)
