package com.linkedin.datahub.graphql.types.datasource;

import com.linkedin.common.urn.DatasourceUrn;
import com.linkedin.datahub.graphql.generated.*;

import java.net.URISyntaxException;

public class DatasourceUtils {

    private DatasourceUtils() { }

    public static DatasourceUrn getDatasourceUrn(String urnStr) {
        try {
            return DatasourceUrn.createFromString(urnStr);
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("Failed to retrieve datasource with urn %s, invalid urn", urnStr));
        }
    }

    public static IcebergSource convertIcebergSource(
            com.linkedin.datasource.sources.IcebergSource source, boolean isOwner) {
        IcebergSource icebergSource = new IcebergSource();
        icebergSource.setHiveMetastoreUris(source.getHiveMetastoreUris());
        return icebergSource;
    }

    public static KafkaMetadataSource convertKafkaMetadataSource(
            com.linkedin.datasource.sources.KafkaMetadataSource source, boolean isOwner) {
        KafkaMetadataSource kafkaSource = new KafkaMetadataSource();
        kafkaSource.setBootstrap(source.getBootstrap());
        kafkaSource.setSchemaRegistryUrl(source.getSchemaRegistryUrl());
        kafkaSource.setTopicPatternsAllow(source.getTopicPatternsAllow());
        kafkaSource.setTopicPatternsDeny(source.getTopicPatternsDeny());
        kafkaSource.setTopicPatternsIgnoreCase(source.isTopicPatternsIgnoreCase());
        return kafkaSource;
    }

    public static MysqlSource convertMysqlSource(
            com.linkedin.datasource.sources.MysqlSource source, boolean isOwner) {
        MysqlSource mysqlSource = new MysqlSource();
        if (isOwner) {
            mysqlSource.setUsername(source.getUsername());
            mysqlSource.setPassword(source.getPassword());
        }
        mysqlSource.setHostPort(source.getHostPort());
        mysqlSource.setDatabase(source.getDatabase());
        mysqlSource.setDriver(source.getDriver());
        mysqlSource.setMinSize(source.getMinSize());
        mysqlSource.setMaxSize(source.getMaxSize());
        mysqlSource.setIdleSize(source.getIdleSize());
        mysqlSource.setStatus(source.getStatus());
        mysqlSource.setJdbcParams(source.getJdbcParams());
        mysqlSource.setTablePatternAllow(source.getTablePatternAllow());
        mysqlSource.setTablePatternDeny(source.getTablePatternDeny());
        mysqlSource.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
        mysqlSource.setSchemaPatternAllow(source.getSchemaPatternAllow());
        mysqlSource.setSchemaPatternDeny(source.getSchemaPatternDeny());
        mysqlSource.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
        return mysqlSource;
    }

    public static PostgresSource convertPostgresSource(
            com.linkedin.datasource.sources.PostgresSource source, boolean isOwner) {
        PostgresSource postgresSource = new PostgresSource();
        if (isOwner) {
            postgresSource.setUsername(source.getUsername());
            postgresSource.setPassword(source.getPassword());
        }
        postgresSource.setHostPort(source.getHostPort());
        postgresSource.setDatabase(source.getDatabase());
        postgresSource.setDriver(source.getDriver());
        postgresSource.setMinSize(source.getMinSize());
        postgresSource.setMaxSize(source.getMaxSize());
        postgresSource.setIdleSize(source.getIdleSize());
        postgresSource.setStatus(source.getStatus());
        postgresSource.setJdbcParams(source.getJdbcParams());
        postgresSource.setTablePatternAllow(source.getTablePatternAllow());
        postgresSource.setTablePatternDeny(source.getTablePatternDeny());
        postgresSource.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
        postgresSource.setSchemaPatternAllow(source.getSchemaPatternAllow());
        postgresSource.setSchemaPatternDeny(source.getSchemaPatternDeny());
        postgresSource.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
        return postgresSource;
    }

    public static TiDBSource convertTiDBSource(
            com.linkedin.datasource.sources.TiDBSource source, boolean isOwner) {
        TiDBSource tidbSource = new TiDBSource();
        if (isOwner) {
            tidbSource.setUsername(source.getUsername());
            tidbSource.setPassword(source.getPassword());
        }
        tidbSource.setHostPort(source.getHostPort());
        tidbSource.setDatabase(source.getDatabase());
        tidbSource.setDriver(source.getDriver());
        tidbSource.setMinSize(source.getMinSize());
        tidbSource.setMaxSize(source.getMaxSize());
        tidbSource.setIdleSize(source.getIdleSize());
        tidbSource.setStatus(source.getStatus());
        tidbSource.setJdbcParams(source.getJdbcParams());
        tidbSource.setTablePatternAllow(source.getTablePatternAllow());
        tidbSource.setTablePatternDeny(source.getTablePatternDeny());
        tidbSource.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
        tidbSource.setSchemaPatternAllow(source.getSchemaPatternAllow());
        tidbSource.setSchemaPatternDeny(source.getSchemaPatternDeny());
        tidbSource.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
        return tidbSource;
    }

    public static HiveSource convertHiveSource(
            com.linkedin.datasource.sources.HiveSource source, boolean isOwner) {
        HiveSource hiveSource = new HiveSource();
        if (isOwner) {
            hiveSource.setUsername(source.getUsername());
            hiveSource.setPassword(source.getPassword());
        }
        hiveSource.setHostPort(source.getHostPort());
        hiveSource.setDatabase(source.getDatabase());
        hiveSource.setDriver(source.getDriver());
        hiveSource.setMinSize(source.getMinSize());
        hiveSource.setMaxSize(source.getMaxSize());
        hiveSource.setIdleSize(source.getIdleSize());
        hiveSource.setStatus(source.getStatus());
        hiveSource.setJdbcParams(source.getJdbcParams());
        hiveSource.setTablePatternAllow(source.getTablePatternAllow());
        hiveSource.setTablePatternDeny(source.getTablePatternDeny());
        hiveSource.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
        hiveSource.setSchemaPatternAllow(source.getSchemaPatternAllow());
        hiveSource.setSchemaPatternDeny(source.getSchemaPatternDeny());
        hiveSource.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
        return hiveSource;
    }

    public static OracleSource convertOracleSource(
            com.linkedin.datasource.sources.OracleSource source, boolean isOwner) {
        OracleSource oracleSource = new OracleSource();
        if (isOwner) {
            oracleSource.setUsername(source.getUsername());
            oracleSource.setPassword(source.getPassword());
        }
        oracleSource.setHostPort(source.getHostPort());
        oracleSource.setServiceName(source.getServiceName());
        oracleSource.setDriver(source.getDriver());
        oracleSource.setMinSize(source.getMinSize());
        oracleSource.setMaxSize(source.getMaxSize());
        oracleSource.setIdleSize(source.getIdleSize());
        oracleSource.setStatus(source.getStatus());
        oracleSource.setTnsName(source.getTnsName());
        oracleSource.setTablePatternAllow(source.getTablePatternAllow());
        oracleSource.setTablePatternDeny(source.getTablePatternDeny());
        oracleSource.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
        oracleSource.setSchemaPatternAllow(source.getSchemaPatternAllow());
        oracleSource.setSchemaPatternDeny(source.getSchemaPatternDeny());
        oracleSource.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
        return oracleSource;
    }

    public static PinotSource convertPinotSource(
            com.linkedin.datasource.sources.PinotSource source, boolean isOwner) {
        PinotSource pinotSource = new PinotSource();
        if (isOwner) {
            pinotSource.setUsername(source.getUsername());
            pinotSource.setPassword(source.getPassword());
        }
        pinotSource.setHostPort(source.getHostPort());
        pinotSource.setDriver(source.getDriver());
        pinotSource.setMinSize(source.getMinSize());
        pinotSource.setMaxSize(source.getMaxSize());
        pinotSource.setIdleSize(source.getIdleSize());
        pinotSource.setStatus(source.getStatus());
        pinotSource.setTablePatternAllow(source.getTablePatternAllow());
        pinotSource.setTablePatternDeny(source.getTablePatternDeny());
        pinotSource.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
        pinotSource.setSchemaPatternAllow(source.getSchemaPatternAllow());
        pinotSource.setSchemaPatternDeny(source.getSchemaPatternDeny());
        pinotSource.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
        return pinotSource;
    }

    public static PrestoSource convertPrestoSource(
            com.linkedin.datasource.sources.PrestoSource source, boolean isOwner) {
        PrestoSource prestoSource = new PrestoSource();
        if (isOwner) {
            prestoSource.setUsername(source.getUsername());
            prestoSource.setPassword(source.getPassword());
        }
        prestoSource.setHostPort(source.getHostPort());
        prestoSource.setCatalog(source.getCatalog());
        prestoSource.setSchema(source.getSchema());
        prestoSource.setDriver(source.getDriver());
        prestoSource.setMinSize(source.getMinSize());
        prestoSource.setMaxSize(source.getMaxSize());
        prestoSource.setIdleSize(source.getIdleSize());
        prestoSource.setStatus(source.getStatus());
        prestoSource.setJdbcParams(source.getJdbcParams());
        prestoSource.setTablePatternAllow(source.getTablePatternAllow());
        prestoSource.setTablePatternDeny(source.getTablePatternDeny());
        prestoSource.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
        prestoSource.setSchemaPatternAllow(source.getSchemaPatternAllow());
        prestoSource.setSchemaPatternDeny(source.getSchemaPatternDeny());
        prestoSource.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
        return prestoSource;
    }

    public static TrinoSource convertTrinoSource(
            com.linkedin.datasource.sources.TrinoSource source, boolean isOwner) {
        TrinoSource trinoSource = new TrinoSource();
        if (isOwner) {
            trinoSource.setUsername(source.getUsername());
            trinoSource.setPassword(source.getPassword());
        }
        trinoSource.setHostPort(source.getHostPort());
        trinoSource.setCatalog(source.getCatalog());
        trinoSource.setSchema(source.getSchema());
        trinoSource.setDriver(source.getDriver());
        trinoSource.setMinSize(source.getMinSize());
        trinoSource.setMaxSize(source.getMaxSize());
        trinoSource.setIdleSize(source.getIdleSize());
        trinoSource.setStatus(source.getStatus());
        trinoSource.setJdbcParams(source.getJdbcParams());
        trinoSource.setTablePatternAllow(source.getTablePatternAllow());
        trinoSource.setTablePatternDeny(source.getTablePatternDeny());
        trinoSource.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
        trinoSource.setSchemaPatternAllow(source.getSchemaPatternAllow());
        trinoSource.setSchemaPatternDeny(source.getSchemaPatternDeny());
        trinoSource.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
        return trinoSource;
    }

    public static SnowflakeSource convertSnowflakeSource(
            com.linkedin.datasource.sources.SnowflakeSource source, boolean isOwner) {
        SnowflakeSource snowflakeSource = new SnowflakeSource();
        if (isOwner) {
            snowflakeSource.setUsername(source.getUsername());
            snowflakeSource.setPassword(source.getPassword());
        }
        snowflakeSource.setHostPort(source.getHostPort());
        snowflakeSource.setDriver(source.getDriver());
        snowflakeSource.setMinSize(source.getMinSize());
        snowflakeSource.setMaxSize(source.getMaxSize());
        snowflakeSource.setIdleSize(source.getIdleSize());
        snowflakeSource.setStatus(source.getStatus());
        snowflakeSource.setConnectionParams(source.getConnectionParams());
        snowflakeSource.setTablePatternAllow(source.getTablePatternAllow());
        snowflakeSource.setTablePatternDeny(source.getTablePatternDeny());
        snowflakeSource.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
        snowflakeSource.setSchemaPatternAllow(source.getSchemaPatternAllow());
        snowflakeSource.setSchemaPatternDeny(source.getSchemaPatternDeny());
        snowflakeSource.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
        return snowflakeSource;
    }

}