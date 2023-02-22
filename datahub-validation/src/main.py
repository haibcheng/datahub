import os
import logging
import json
import yaml
import pymysql
from constant import Constant


def mysqlconnect(suite):
    host = os.getenv(Constant.db_store_host)
    port = int(os.getenv(Constant.db_store_port))
    user = os.getenv(Constant.db_store_user)
    password = os.getenv(Constant.db_store_password)
    name = os.getenv(Constant.db_store_name)

    sql = "select value from ge_expectations_store where expectation_suite_name = %s"
    connection = pymysql.connect(
        host=host,
        port=port,
        user=user,
        password=password,
        db=name,
        cursorclass=pymysql.cursors.DictCursor
    )
    try:
        cursor = connection.cursor()
        cursor.execute(sql, suite)
        for row in cursor:
            return row["value"]
    finally:
        connection.close()
    return None


def sync_expectation(root, suite, value):
    value_dict = json.loads(value)
    json_object = json.dumps(value_dict, indent=4)

    path = suite.replace(".", "/")
    name_o = root + "/expectations/" + path + ".json"
    with open(name_o, "w") as outfile:
        outfile.write(json_object)


def replace_configuration(root):
    name_i = root + "/templates/config_variables.yml"
    with open(name_i, 'r') as file:
        config_variables = yaml.safe_load(file)

    config_variables['db_creds']['drivername'] = os.getenv(Constant.db_source_driver_name)
    config_variables['db_creds']['host'] = os.getenv(Constant.db_source_host)
    config_variables['db_creds']['port'] = int(os.getenv(Constant.db_source_port))
    config_variables['db_creds']['username'] = os.getenv(Constant.db_source_user)
    config_variables['db_creds']['password'] = os.getenv(Constant.db_source_password)
    config_variables['db_creds']['database'] = os.getenv(Constant.db_source_name)

    config_variables['ge_data_asset_name'] = os.getenv(Constant.ge_data_asset_name)
    config_variables['ge_expectation_suite'] = os.getenv(Constant.ge_expectation_suite)
    config_variables['gms_platform_alias'] = os.getenv(Constant.gms_platform_alias)
    config_variables['gms_exclude_dbname'] = bool(os.getenv(Constant.gms_exclude_dbname))
    config_variables['gms_server_url'] = os.getenv(Constant.gms_server_url)
    config_variables['gms_token'] = os.getenv(Constant.gms_token)
    config_variables['gms_platform_instance_map']['datahub'] = \
        os.getenv(Constant.gms_platform_instance_map_datahub)

    name_o = root + "/uncommitted/config_variables.yml"
    with open(name_o, "w") as outfile:
        yaml.dump(config_variables, outfile)


def sync():
    root = os.getenv(Constant.ge_root)
    suite = os.getenv(Constant.ge_expectation_suite)
    value = mysqlconnect(suite)
    if value is None:
        logging.warning("no suite[%s] was found", suite)
        return
    sync_expectation(root, suite, value)
    replace_configuration(root)


if __name__ == '__main__':
    sync()
