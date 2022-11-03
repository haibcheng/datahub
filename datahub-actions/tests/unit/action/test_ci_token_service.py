from wap_actions.service.ci_token import CITokenService


def test_access_token():
    ci_token_s = CITokenService(
        access_token_url='https://idbroker.webex.com/idb/oauth2/v1/access_token',
        username='user',
        password='pass',
        bearer_token_url='https://idbroker.webex.com/idb/token/6078fba4-49d9-4291-9f7b-80116aab6974'
                         '/v2/actions/GetBearerToken/invoke',
        machine_act_name='CTGWAP-DATAHUB',
        machine_act_pass='pass'
    )
    token = ci_token_s.access_token_of()
    print(token)
