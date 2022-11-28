import logging
from datahub_actions.action.action import Action
from datahub_actions.pipeline.pipeline_context import PipelineContext
from wap_actions.core.matcher import EntityTypeMatcher, PropValueMatcher
from wap_actions.core.configuration import UrlNotificationConfig
from wap_actions.core.action import CommonAction
from wap_actions.service.notification import UrlNotificationService

logger = logging.getLogger(__name__)


class UrlNotificationAction(CommonAction):

    TYPE_CUD = "CUD"
    TYPE_UDP = "UDP"

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
        if self.config.type == self.TYPE_UDP:
            return self.config.callbacks[0].url
        elif self.config.type == self.TYPE_CUD:
            for c in self.config.callbacks:
                if urn.upper().endswith("," + c.region.upper() + ")"):
                    return c.url
        return None

    def _matchers(self):
        return [
            EntityTypeMatcher(config=self.config, match_type=self.TYPE_CUD),
            PropValueMatcher(config=self.config, match_type=self.TYPE_UDP, aspect_name="datasetProperties")
        ]

    def __init__(self, config: UrlNotificationConfig, ctx: PipelineContext):
        super().__init__(config=config, ctx=ctx)
        self.notification = UrlNotificationService(config.ci_config)

    @classmethod
    def create(cls, config_dict: dict, ctx: PipelineContext) -> "Action":
        action_config = UrlNotificationConfig.parse_obj(config_dict or {})
        return cls(action_config, ctx)
