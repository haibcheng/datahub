import requests
import json
import base64
import time


class CITokenService:

    def access_token_of(self):

        if self._access_token is not None:
            time_c = time.time() - self._access_token_generated_in_seconds
            if time_c < self._expires_in_seconds - 60:
                return self._access_token

        bearer_token = self._bearer_token_of()
        ci_auth = "Basic {0}".format(str(base64.b64encode(
            bytes(self.username + ":" + self.password, 'utf-8')), 'utf_8'))
        headers = {
            'Content-type': 'application/x-www-form-urlencoded',
            'Authorization': ci_auth
        }
        body = {
            "grant_type": "urn:ietf:params:oauth:grant-type:saml2-bearer",
            "scope": "Identity:SCIM",
            "assertion": bearer_token
        }
        response = requests.post(self.access_token_url, data=body, headers=headers)
        if response.status_code != 200:
            raise Exception(response.text)

        res_json = json.loads(response.text)
        self._expires_in_seconds = res_json.get("expires_in")
        self._access_token = res_json.get("access_token")
        self._access_token_generated_in_seconds = time.time()
        return self._access_token

    def _bearer_token_of(self):
        headers = {'Content-type': 'application/json'}
        body = {
            "name": self.machine_act_name,
            "password": self.machine_act_pass
        }
        response = requests.post(self.bearer_token_url,
                                 data=json.dumps(body),
                                 headers=headers)
        if response.status_code != 200:
            raise Exception(response.text)

        res_json = json.loads(response.text)
        return res_json.get("BearerToken")

    def __init__(self,
                 access_token_url: str,
                 username: str,
                 password: str,
                 bearer_token_url: str,
                 machine_act_name: str,
                 machine_act_pass: str):
        self.access_token_url = access_token_url
        self.username = username
        self.password = password
        self.bearer_token_url = bearer_token_url
        self.machine_act_name = machine_act_name
        self.machine_act_pass = machine_act_pass
        self._expires_in_seconds = 0
        self._access_token = None
        self._access_token_generated_in_seconds = None
