from wap_actions.service.ci_token import CITokenService
from wap_actions.service.cu_dashboard import CustomerDashboardService


def test_change_notify():
    ci_token_s = CITokenService(
        access_token_url='https://idbrokerbts.webex.com/idb/oauth2/v1/access_token',
        username='user',
        password='pass',
        bearer_token_url='https://idbrokerbts.webex.com/idb/token/6078fba4-49d9-4291-9f7b-80116aab6974'
                         '/v2/actions/GetBearerToken/invoke',
        machine_act_name='CTGWAP-DATAHUB',
        machine_act_pass='pass'
    )
    cu_dashboard = CustomerDashboardService(
        token_service=ci_token_s,
        refresh_api='https://davis5.qa.webex.com/davis/api/v1/data-sources/refresh'
    )
    cu_dashboard.change_notify()
