package com.linkedin.datahub.graphql.types.datasource.mappers;

import com.linkedin.datahub.graphql.generated.*;
import com.linkedin.datahub.graphql.types.mappers.ModelMapper;

import javax.annotation.Nonnull;

import static com.linkedin.datahub.graphql.types.datasource.DatasourceUtils.*;

public class DatasourceConnectionPrimaryMapper implements
        ModelMapper<com.linkedin.datasource.DatasourceConnectionPrimary, DatasourceConnection>
{
    public static final DatasourceConnectionPrimaryMapper INSTANCE = new DatasourceConnectionPrimaryMapper();

    public static DatasourceConnection map(
            @Nonnull final com.linkedin.datasource.DatasourceConnectionPrimary connections) {
        return INSTANCE.apply(connections);
    }

    @Override
    public DatasourceConnection apply(
            @Nonnull final com.linkedin.datasource.DatasourceConnectionPrimary input) {
        final DatasourceConnection result = new DatasourceConnection();
        result.setDataCenter(input.getDataCenter());
        if (input.getConnection().isIcebergSource()) {
            result.setConnection(convertIcebergSource(input.getConnection().getIcebergSource()));
        } else if (input.getConnection().isKafkaMetadataSource()) {
            result.setConnection(convertKafkaMetadataSource(input.getConnection().getKafkaMetadataSource()));
        } else if (input.getConnection().isMysqlSource()) {
            result.setConnection(convertMysqlSource(input.getConnection().getMysqlSource()));
        } else if (input.getConnection().isPostgresSource()) {
            result.setConnection(convertPostgresSource(input.getConnection().getPostgresSource()));
        } else if (input.getConnection().isTiDBSource()) {
            result.setConnection(convertTiDBSource(input.getConnection().getTiDBSource()));
        } else if (input.getConnection().isHiveSource()) {
            result.setConnection(convertHiveSource(input.getConnection().getHiveSource()));
        } else if (input.getConnection().isOracleSource()) {
            result.setConnection(convertOracleSource(input.getConnection().getOracleSource()));
        }  else if (input.getConnection().isPinotSource()) {
            result.setConnection(convertPinotSource(input.getConnection().getPinotSource()));
        } else if (input.getConnection().isPrestoSource()) {
            result.setConnection(convertPrestoSource(input.getConnection().getPrestoSource()));
        } else if (input.getConnection().isTrinoSource()) {
            result.setConnection(convertTrinoSource(input.getConnection().getTrinoSource()));
        } else if (input.getConnection().isSnowflakeSource()) {
            result.setConnection(convertSnowflakeSource(input.getConnection().getSnowflakeSource()));
        }

        return result;
    }

}