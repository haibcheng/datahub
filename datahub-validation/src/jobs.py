import logging
import json
import yaml
import os
import pymysql
from constant import Constant
from dotenv import load_dotenv
from pathlib import Path


class OnceJob:

    def __init__(self):
        self.ge_root = os.getenv(Constant.ge_root)
        self.db_store_host = os.getenv(Constant.db_store_host)
        self.db_store_port = int(os.getenv(Constant.db_store_port))
        self.db_store_user = os.getenv(Constant.db_store_user)
        self.db_store_password = os.getenv(Constant.db_store_password)
        self.db_store_name = os.getenv(Constant.db_store_name)

    def mysql_connect(self, suite):

        sql = "select db_type,project,cluster,db_name,schema_name,table_name,value,params " \
              "from ge_expectations where suite = %s and active = 1"
        connection = pymysql.connect(
            host=self.db_store_host,
            port=self.db_store_port,
            user=self.db_store_user,
            password=self.db_store_password,
            db=self.db_store_name,
            cursorclass=pymysql.cursors.DictCursor
        )
        try:
            cursor = connection.cursor()
            cursor.execute(sql, suite)
            for row in cursor:
                return row
        finally:
            connection.close()
        return None

    def sync(self):
        suite = os.getenv(Constant.ge_expectation_suite)
        row = self.mysql_connect(suite)
        if row is None:
            logging.warning("no suite[%s] was found", suite)
            return
        self.sync_expectation(suite, row)
        self.sync_configuration(suite, row)

    def sync_expectation(self, suite, row):
        value_dict = json.loads(row["value"])
        json_object = json.dumps(value_dict, indent=4)

        path = self.ge_root + "/expectations"
        paths = suite.split(".")
        for idx, p in enumerate(paths):
            if idx == len(paths) - 1:
                break
            path += "/" + p
            if not os.path.exists(path):
                os.makedirs(path)

        name_o = path + "/" + paths[len(paths) - 1] + ".json"
        with open(name_o, "w") as outfile:
            outfile.write(json_object)

    def sync_configuration(self, suite: str, row: dict):
        name_i = self.ge_root + "/templates/config_variables.yml"
        with open(name_i, 'r') as file:
            config_variables = yaml.safe_load(file)

        params_dict = None
        if row is not None and row["params"] is not None:
            params_dict = json.loads(row["params"])

        path = suite[0:suite.rindex(".")] + ".env"
        dotenv_path = Path(self.ge_root + '/templates/env/' + path)
        load_dotenv(dotenv_path=dotenv_path)

        config_variables['ge_expectation_suite'] = suite
        if row is None:
            config_variables['ge_data_asset_name'] = \
                self.value_of(params_dict, Constant.ge_data_asset_name)
        elif row["schema_name"] is not None:
            config_variables['ge_data_asset_name'] = \
                row["schema_name"] + "." + row["table_name"]
        else:
            config_variables['ge_data_asset_name'] = row["table_name"]

        config_variables['db_creds']['drivername'] = \
            self.value_of(params_dict, Constant.db_source_driver_name)
        config_variables['db_creds']['host'] = \
            self.value_of(params_dict, Constant.db_source_host)
        config_variables['db_creds']['port'] = \
            int(self.value_of(params_dict, Constant.db_source_port))
        config_variables['db_creds']['username'] = \
            self.value_of(params_dict, Constant.db_source_user)
        config_variables['db_creds']['password'] = \
            self.value_of(params_dict, Constant.db_source_password)
        config_variables['db_creds']['database'] = \
            self.value_of(params_dict, Constant.db_source_name)

        config_variables['store_db_creds']['drivername'] = \
            self.value_of(params_dict, Constant.db_target_driver_name)
        config_variables['store_db_creds']['host'] = \
            self.value_of(params_dict, Constant.db_target_host)
        config_variables['store_db_creds']['port'] = \
            int(self.value_of(params_dict, Constant.db_target_port))
        config_variables['store_db_creds']['username'] = \
            self.value_of(params_dict, Constant.db_target_user)
        config_variables['store_db_creds']['password'] = \
            self.value_of(params_dict, Constant.db_target_password)
        config_variables['store_db_creds']['database'] = \
            self.value_of(params_dict, Constant.db_target_name)

        config_variables['gms_platform_alias'] = \
            self.value_of(params_dict, Constant.gms_platform_alias)
        config_variables['gms_exclude_dbname'] = \
            'true' == self.value_of(params_dict, Constant.gms_exclude_dbname).lower()
        config_variables['gms_server_url'] = \
            self.value_of(params_dict, Constant.gms_server_url)
        config_variables['gms_token'] = \
            self.value_of(params_dict, Constant.gms_token)
        config_variables['gms_platform_instance_map']['datahub'] = \
            self.value_of(params_dict, Constant.gms_platform_instance_map_datahub)

        name_o = self.ge_root + "/uncommitted/config_variables.yml"
        with open(name_o, "w") as outfile:
            yaml.dump(config_variables, outfile)

    @staticmethod
    def value_of(params: dict, key: str):
        if params is None or key.lower() not in params:
            return os.getenv(key)
        else:
            return str(params[key.lower()])
