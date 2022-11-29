import requests
import logging
from wap_actions.service.ci_token import CITokenConfig, CITokenService

logger = logging.getLogger(__name__)


class NotificationService:
    def notify(self, url: str, urns: list[str] = None):
        pass


class CudNotificationService(NotificationService):

    def notify(self, url: str, urns: list[str] = None):
        token = self.token_service.access_token_of()
        headers = {
            'Authorization': 'Bearer ' + token
        }
        body = None
        if urns is not None and len(urns) > 0:
            body = {
                "urns": urns
            }
        response = requests.post(url, headers=headers, data=body, verify=False, timeout=30)
        logger.info("Called API -> %s!!!", url)
        if response.status_code not in (200, 204):
            raise Exception(response.text)

    def __init__(self,
                 ci_config: CITokenConfig):
        self.token_service = CITokenService(ci_config=ci_config)


class UdpNotificationService(NotificationService):

    def notify(self, url: str, urns: list[str] = None):
        response = requests.get(url, verify=False, timeout=30)
        logger.info("Called API -> %s!!!", url)
        if response.status_code not in (200, 204):
            raise Exception(response.text)
