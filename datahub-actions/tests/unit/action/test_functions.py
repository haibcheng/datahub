import os
import unittest
from wap_actions.service.ci_token import CITokenConfig, CITokenService


class TestFunctions(unittest.TestCase):

    def test_notify(self):

        pa = os.getenv('CLIENT_PASSWORD')
        mpa = os.getenv('MACHINE_ACCOUNT_PASS')

        ci_config = CITokenConfig(
            access_token_url='https://idbrokerbts.webex.com/idb/oauth2/v1/access_token',
            username='C4d7507eca0d56509f82db6919ec550fd1cd76dd67d125fc14e1fe9c2b24d5e10',
            password=pa,
            bearer_token_url='https://idbrokerbts.webex.com/idb/token/6078fba4-49d9-4291-9f7b-80116aab6974'
                             '/v2/actions/GetBearerToken/invoke',
            machine_account_name='CTGWAP-DATAHUB',
            machine_account_pass=mpa
        )

        token_service = CITokenService(ci_config=ci_config)
        token = token_service.access_token_of()
        print("BTS: " + token)
        self.assertTrue(token != '')

        pcp = os.getenv('PRO_CLIENT_PASSWORD')
        ci_config = CITokenConfig(
            access_token_url='https://idbroker.webex.com/idb/oauth2/v1/access_token',
            username='C51206cd701b886ee1ae9aa9157c0c8624787c8f10bfa9ce267164dda01f5df91',
            password=pcp,
            bearer_token_url='https://idbroker.webex.com/idb/token/6078fba4-49d9-4291-9f7b-80116aab6974'
                             '/v2/actions/GetBearerToken/invoke',
            machine_account_name='CTGWAP-DATAHUB',
            machine_account_pass=mpa
        )

        token_service = CITokenService(ci_config=ci_config)
        token = token_service.access_token_of()
        print("PROD: " + token)
        self.assertTrue(token != '')


if __name__ == '__main__':
    unittest.main()
