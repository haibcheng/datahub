import os
import trino
import unittest
from wap_actions.service.ci_token import CITokenConfig, CITokenService


class TestFunctions(unittest.TestCase):

    def test_trino(self):
        pa = os.getenv('MY_TRINO_PASSWORD')
        conn = trino.dbapi.connect(
            host='hdsj1ptc001.webex.com',
            port=8443,
            catalog='wpsj1_pinot',
            http_scheme='https',
            schema='default',
            auth=trino.auth.BasicAuthentication(username='haicheng', password=pa),
            verify='/Users/haicheng/Documents/App/trino/trinoroot.pem'
        )
        cur = conn.cursor()
        cur.arraysize = 1000  # sets the number of records the cursor will fetch at a time
        cur.execute('SELECT * FROM wme_metrics_customer_report limit 5')
        rows = cur.fetchmany()
        for row in rows:
            print(row)
        self.assertTrue(len(rows) == 5)

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
        print(token)
        self.assertTrue(token != '')


if __name__ == '__main__':
    unittest.main()
