from wap_actions.service.ci_token import CITokenConfig
from wap_actions.service.notification import UrlNotificationService


def test_notify():

    ci_config = CITokenConfig(
        access_token_url='https://idbrokerbts.webex.com/idb/oauth2/v1/access_token',
        username='user',
        password='pass',
        bearer_token_url='https://idbrokerbts.webex.com/idb/token/6078fba4-49d9-4291-9f7b-80116aab6974'
                         '/v2/actions/GetBearerToken/invoke',
        machine_account_name='CTGWAP-DATAHUB',
        machine_account_pass='pass'
    )
    notification_s = UrlNotificationService(
        ci_config=ci_config
    )
    try:
        notification_s.notify(url='https://davis5.qa.webex.com/davis/api/v1/data-sources/refresh1')
    except Exception as error:
        print(repr(error))
