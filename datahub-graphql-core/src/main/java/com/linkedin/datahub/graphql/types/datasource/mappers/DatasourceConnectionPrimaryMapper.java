package com.linkedin.datahub.graphql.types.datasource.mappers;

import com.linkedin.datahub.graphql.generated.DatasourceConnection;
import com.linkedin.datahub.graphql.types.mappers.ModelMapper;

import javax.annotation.Nonnull;

import static com.linkedin.datahub.graphql.types.datasource.DatasourceUtils.*;

public class DatasourceConnectionPrimaryMapper implements
        ModelMapper<com.linkedin.datasource.DatasourceConnectionPrimary, DatasourceConnection>
{
    private final boolean isOwner;

    public static DatasourceConnection map(
            @Nonnull final com.linkedin.datasource.DatasourceConnectionPrimary connections,
            boolean isOwner) {
        return new DatasourceConnectionPrimaryMapper(isOwner).apply(connections);
    }

    @Override
    public DatasourceConnection apply(
            @Nonnull final com.linkedin.datasource.DatasourceConnectionPrimary input) {
        final DatasourceConnection result = new DatasourceConnection();
        result.setDataCenter(input.getDataCenter());
        if (input.getConnection().isIcebergSource()) {
            result.setConnection(convertIcebergSource(
                    input.getConnection().getIcebergSource(), isOwner));
        } else if (input.getConnection().isKafkaMetadataSource()) {
            result.setConnection(convertKafkaMetadataSource(
                    input.getConnection().getKafkaMetadataSource(), isOwner));
        } else if (input.getConnection().isMysqlSource()) {
            result.setConnection(convertMysqlSource(
                    input.getConnection().getMysqlSource(), isOwner));
        } else if (input.getConnection().isPostgresSource()) {
            result.setConnection(convertPostgresSource(
                    input.getConnection().getPostgresSource(), isOwner));
        } else if (input.getConnection().isTiDBSource()) {
            result.setConnection(convertTiDBSource(
                    input.getConnection().getTiDBSource(), isOwner));
        } else if (input.getConnection().isHiveSource()) {
            result.setConnection(convertHiveSource(
                    input.getConnection().getHiveSource(), isOwner));
        } else if (input.getConnection().isOracleSource()) {
            result.setConnection(convertOracleSource(
                    input.getConnection().getOracleSource(), isOwner));
        }  else if (input.getConnection().isPinotSource()) {
            result.setConnection(convertPinotSource(
                    input.getConnection().getPinotSource(), isOwner));
        } else if (input.getConnection().isPrestoSource()) {
            result.setConnection(convertPrestoSource(
                    input.getConnection().getPrestoSource(), isOwner));
        } else if (input.getConnection().isTrinoSource()) {
            result.setConnection(convertTrinoSource(
                    input.getConnection().getTrinoSource(), isOwner));
        } else if (input.getConnection().isSnowflakeSource()) {
            result.setConnection(convertSnowflakeSource(
                    input.getConnection().getSnowflakeSource(), isOwner));
        }

        return result;
    }

    public DatasourceConnectionPrimaryMapper(boolean isOwner) {
        this.isOwner = isOwner;
    }

}