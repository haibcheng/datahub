package com.linkedin.datahub.graphql.resolvers.datasource;

import java.util.Map;

public interface DatasourceConstants
{
    String KAFKA_SOURCE_NAME = "kafka";
    String ORACLE_SOURCE_NAME = "oracle";
    String MYSQL_SOURCE_NAME = "mysql";
    String ICEBERG_SOURCE_NAME = "iceberg";
    String POSTGRES_SOURCE_NAME = "postgres";
    String HIVE_SOURCE_NAME = "hive";
    String PINOT_SOURCE_NAME = "pinot";
    String PRESTO_SOURCE_NAME = "presto";
    String TIDB_SOURCE_NAME = "tiDB";
    String TRINO_SOURCE_NAME = "trino";
    String SNOWFLAKE_SOURCE_NAME = "snowflake";

    Map<String, String> JDBC_TYPES = Map.of(
            POSTGRES_SOURCE_NAME, "postgresql",
            ORACLE_SOURCE_NAME, "oracle:thin",
            TIDB_SOURCE_NAME, "mysql",
            MYSQL_SOURCE_NAME, "mysql",
            HIVE_SOURCE_NAME, "hive2",
            PRESTO_SOURCE_NAME, "presto",
            PINOT_SOURCE_NAME, "pinot",
            TRINO_SOURCE_NAME, "trino",
            SNOWFLAKE_SOURCE_NAME, "snowflake"
        );

}