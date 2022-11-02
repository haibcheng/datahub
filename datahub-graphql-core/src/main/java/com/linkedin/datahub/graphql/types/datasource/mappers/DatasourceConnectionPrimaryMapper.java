package com.linkedin.datahub.graphql.types.datasource.mappers;

import com.linkedin.datahub.graphql.generated.*;
import com.linkedin.datahub.graphql.types.mappers.ModelMapper;

import javax.annotation.Nonnull;

public class DatasourceConnectionPrimaryMapper implements ModelMapper<com.linkedin.datasource.DatasourceConnectionPrimary, DatasourceConnection> {

    public static final DatasourceConnectionPrimaryMapper INSTANCE = new DatasourceConnectionPrimaryMapper();

    public static DatasourceConnection map(@Nonnull final com.linkedin.datasource.DatasourceConnectionPrimary connections) {
        return INSTANCE.apply(connections);
    }

    @Override
    public DatasourceConnection apply(@Nonnull final com.linkedin.datasource.DatasourceConnectionPrimary input) {
        final DatasourceConnection result = new DatasourceConnection();
        result.setDataCenter(input.getDataCenter());
        if (input.getConnection().isIcebergSource()) {
            IcebergSource icebergSource = new IcebergSource();
            icebergSource.setHiveMetastoreUris(input.getConnection().getIcebergSource().getHiveMetastoreUris());
            result.setConnection(icebergSource);
        } else if (input.getConnection().isKafkaMetadataSource()) {
            KafkaMetadataSource conn = new KafkaMetadataSource();
            com.linkedin.datasource.sources.KafkaMetadataSource source = input.getConnection().getKafkaMetadataSource();
            conn.setBootstrap(source.getBootstrap());
            conn.setSchemaRegistryUrl(source.getSchemaRegistryUrl());
            conn.setTopicPatternsAllow(source.getTopicPatternsAllow());
            conn.setTopicPatternsDeny(source.getTopicPatternsDeny());
            conn.setTopicPatternsIgnoreCase(source.isTopicPatternsIgnoreCase());
            result.setConnection(conn);
        } else if (input.getConnection().isMysqlSource()) {
            MysqlSource conn = new MysqlSource();
            com.linkedin.datasource.sources.MysqlSource source = input.getConnection().getMysqlSource();
            conn.setUsername(source.getUsername());
//            conn.setPassword(source.getPassword());
            conn.setHostPort(source.getHostPort());
            conn.setDatabase(source.getDatabase());
            conn.setDriver(source.getDriver());
            conn.setMinSize(source.getMinSize());
            conn.setMaxSize(source.getMaxSize());
            conn.setIdleSize(source.getIdleSize());
            conn.setStatus(source.getStatus());
            conn.setJdbcParams(source.getJdbcParams());
            conn.setTablePatternAllow(source.getTablePatternAllow());
            conn.setTablePatternDeny(source.getTablePatternDeny());
            conn.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
            conn.setSchemaPatternAllow(source.getSchemaPatternAllow());
            conn.setSchemaPatternDeny(source.getSchemaPatternDeny());
            conn.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
            result.setConnection(conn);
        } else if (input.getConnection().isPostgresSource()) {
            PostgresSource conn = new PostgresSource();
            com.linkedin.datasource.sources.PostgresSource source = input.getConnection().getPostgresSource();
            conn.setUsername(source.getUsername());
//            conn.setPassword(source.getPassword());
            conn.setHostPort(source.getHostPort());
            conn.setDatabase(source.getDatabase());
            conn.setDriver(source.getDriver());
            conn.setMinSize(source.getMinSize());
            conn.setMaxSize(source.getMaxSize());
            conn.setIdleSize(source.getIdleSize());
            conn.setStatus(source.getStatus());
            conn.setJdbcParams(source.getJdbcParams());
            conn.setTablePatternAllow(source.getTablePatternAllow());
            conn.setTablePatternDeny(source.getTablePatternDeny());
            conn.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
            conn.setSchemaPatternAllow(source.getSchemaPatternAllow());
            conn.setSchemaPatternDeny(source.getSchemaPatternDeny());
            conn.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
            result.setConnection(conn);
        } else if (input.getConnection().isTiDBSource()) {
            TiDBSource conn = new TiDBSource();
            com.linkedin.datasource.sources.TiDBSource source = input.getConnection().getTiDBSource();
            conn.setUsername(source.getUsername());
//            conn.setPassword(source.getPassword());
            conn.setHostPort(source.getHostPort());
            conn.setDatabase(source.getDatabase());
            conn.setDriver(source.getDriver());
            conn.setMinSize(source.getMinSize());
            conn.setMaxSize(source.getMaxSize());
            conn.setIdleSize(source.getIdleSize());
            conn.setStatus(source.getStatus());
            conn.setJdbcParams(source.getJdbcParams());
            conn.setTablePatternAllow(source.getTablePatternAllow());
            conn.setTablePatternDeny(source.getTablePatternDeny());
            conn.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
            conn.setSchemaPatternAllow(source.getSchemaPatternAllow());
            conn.setSchemaPatternDeny(source.getSchemaPatternDeny());
            conn.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
            result.setConnection(conn);
        } else if (input.getConnection().isHiveSource()) {
            HiveSource conn = new HiveSource();
            com.linkedin.datasource.sources.HiveSource source = input.getConnection().getHiveSource();
            conn.setUsername(source.getUsername());
//            conn.setPassword(source.getPassword());
            conn.setHostPort(source.getHostPort());
            conn.setDatabase(source.getDatabase());
            conn.setDriver(source.getDriver());
            conn.setMinSize(source.getMinSize());
            conn.setMaxSize(source.getMaxSize());
            conn.setIdleSize(source.getIdleSize());
            conn.setStatus(source.getStatus());
            conn.setJdbcParams(source.getJdbcParams());
            conn.setTablePatternAllow(source.getTablePatternAllow());
            conn.setTablePatternDeny(source.getTablePatternDeny());
            conn.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
            conn.setSchemaPatternAllow(source.getSchemaPatternAllow());
            conn.setSchemaPatternDeny(source.getSchemaPatternDeny());
            conn.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
            result.setConnection(conn);
        } else if (input.getConnection().isOracleSource()) {
            OracleSource conn = new OracleSource();
            com.linkedin.datasource.sources.OracleSource source = input.getConnection().getOracleSource();
            conn.setUsername(source.getUsername());
//            conn.setPassword(source.getPassword());
            conn.setHostPort(source.getHostPort());
            conn.setServiceName(source.getServiceName());
            conn.setDriver(source.getDriver());
            conn.setMinSize(source.getMinSize());
            conn.setMaxSize(source.getMaxSize());
            conn.setIdleSize(source.getIdleSize());
            conn.setStatus(source.getStatus());
            conn.setTnsName(source.getTnsName());
            conn.setTablePatternAllow(source.getTablePatternAllow());
            conn.setTablePatternDeny(source.getTablePatternDeny());
            conn.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
            conn.setSchemaPatternAllow(source.getSchemaPatternAllow());
            conn.setSchemaPatternDeny(source.getSchemaPatternDeny());
            conn.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
            result.setConnection(conn);
        }  else if (input.getConnection().isPinotSource()) {
            PinotSource conn = new PinotSource();
            com.linkedin.datasource.sources.PinotSource source = input.getConnection().getPinotSource();
            conn.setUsername(source.getUsername());
//            conn.setPassword(source.getPassword());
            conn.setHostPort(source.getHostPort());
            conn.setDriver(source.getDriver());
            conn.setMinSize(source.getMinSize());
            conn.setMaxSize(source.getMaxSize());
            conn.setIdleSize(source.getIdleSize());
            conn.setStatus(source.getStatus());
            conn.setTablePatternAllow(source.getTablePatternAllow());
            conn.setTablePatternDeny(source.getTablePatternDeny());
            conn.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
            conn.setSchemaPatternAllow(source.getSchemaPatternAllow());
            conn.setSchemaPatternDeny(source.getSchemaPatternDeny());
            conn.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
            result.setConnection(conn);
        } else if (input.getConnection().isPrestoSource()) {
            PrestoSource conn = new PrestoSource();
            com.linkedin.datasource.sources.PrestoSource source = input.getConnection().getPrestoSource();
            conn.setUsername(source.getUsername());
//            conn.setPassword(source.getPassword());
            conn.setHostPort(source.getHostPort());
            conn.setCatalog(source.getCatalog());
            conn.setSchema(source.getSchema());
            conn.setDriver(source.getDriver());
            conn.setMinSize(source.getMinSize());
            conn.setMaxSize(source.getMaxSize());
            conn.setIdleSize(source.getIdleSize());
            conn.setStatus(source.getStatus());
            conn.setTablePatternAllow(source.getTablePatternAllow());
            conn.setTablePatternDeny(source.getTablePatternDeny());
            conn.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
            conn.setSchemaPatternAllow(source.getSchemaPatternAllow());
            conn.setSchemaPatternDeny(source.getSchemaPatternDeny());
            conn.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
            result.setConnection(conn);
        } else if (input.getConnection().isTrinoSource()) {
            TrinoSource conn = new TrinoSource();
            com.linkedin.datasource.sources.TrinoSource source = input.getConnection().getTrinoSource();
            conn.setUsername(source.getUsername());
//            conn.setPassword(source.getPassword());
            conn.setHostPort(source.getHostPort());
            conn.setCatalog(source.getCatalog());
            conn.setSchema(source.getSchema());
            conn.setDriver(source.getDriver());
            conn.setMinSize(source.getMinSize());
            conn.setMaxSize(source.getMaxSize());
            conn.setIdleSize(source.getIdleSize());
            conn.setStatus(source.getStatus());
            conn.setTablePatternAllow(source.getTablePatternAllow());
            conn.setTablePatternDeny(source.getTablePatternDeny());
            conn.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
            conn.setSchemaPatternAllow(source.getSchemaPatternAllow());
            conn.setSchemaPatternDeny(source.getSchemaPatternDeny());
            conn.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
            result.setConnection(conn);
        } else if (input.getConnection().isSnowflakeSource()) {
            SnowflakeSource conn = new SnowflakeSource();
            com.linkedin.datasource.sources.SnowflakeSource source = input.getConnection().getSnowflakeSource();
            conn.setUsername(source.getUsername());
            conn.setHostPort(source.getHostPort());
            conn.setDriver(source.getDriver());
            conn.setMinSize(source.getMinSize());
            conn.setMaxSize(source.getMaxSize());
            conn.setIdleSize(source.getIdleSize());
            conn.setStatus(source.getStatus());
            conn.setConnectionParams(source.getConnectionParams());
            conn.setTablePatternAllow(source.getTablePatternAllow());
            conn.setTablePatternDeny(source.getTablePatternDeny());
            conn.setTablePatternIgnoreCase(source.isTablePatternIgnoreCase());
            conn.setSchemaPatternAllow(source.getSchemaPatternAllow());
            conn.setSchemaPatternDeny(source.getSchemaPatternDeny());
            conn.setSchemaPatternIgnoreCase(source.isSchemaPatternIgnoreCase());
        }

        return result;
    }
}
