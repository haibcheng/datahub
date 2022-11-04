import requests
from wap_actions.service.ci_token import CITokenService


class CustomerDashboardService:

    def change_notify(self):
        token = self.token_service.access_token_of()
        headers = {
            'Authorization': 'Bearer ' + token
        }
        response = requests.post(self.refresh_api, headers=headers, verify=False)
        if response.status_code not in (200, 204):
            raise Exception(response.text)

    def __init__(self,
                 token_service: CITokenService,
                 refresh_api: str):
        self.token_service = token_service
        self.refresh_api = refresh_api
