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

    Map<String, String> JDBC_DRIVERS = Map.of(
            POSTGRES_SOURCE_NAME, "org.postgresql.Driver",
            ORACLE_SOURCE_NAME, "oracle.jdbc.OracleDriver",
            TIDB_SOURCE_NAME, "com.mysql.jdbc.Driver",
            MYSQL_SOURCE_NAME, "com.mysql.jdbc.Driver",
            HIVE_SOURCE_NAME, "org.apache.hive.jdbc.HiveDriver",
            PRESTO_SOURCE_NAME, "com.facebook.presto.jdbc.PrestoDriver",
            PINOT_SOURCE_NAME, "org.apache.pinot.client.PinotDriver",
            TRINO_SOURCE_NAME, "io.trino.jdbc.TrinoDriver",
            SNOWFLAKE_SOURCE_NAME, "com.snowflake.client.jdbc.SnowflakeDriver"
    );

}