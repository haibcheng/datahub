import requests
from wap_actions.service.ci_token import CITokenConfig, CITokenService


class UrlNotificationService:

    def notify(self, urns: [str] = None):
        token = self.token_service.access_token_of()
        headers = {
            'Authorization': 'Bearer ' + token
        }
        body = None
        if urns is not None and len(urns) > 0:
            body = {
                "urns": urns
            }
        response = requests.post(self.callback_api, headers=headers, data=body, verify=False)
        if response.status_code not in (200, 204):
            raise Exception(response.text)

    def __init__(self,
                 ci_config: CITokenConfig,
                 callback_api: str):
        self.token_service = CITokenService(ci_config=ci_config)
        self.callback_api = callback_api
