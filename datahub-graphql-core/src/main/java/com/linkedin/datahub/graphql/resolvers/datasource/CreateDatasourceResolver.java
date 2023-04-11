package com.linkedin.datahub.graphql.resolvers.datasource;

import com.google.common.collect.ImmutableList;
import com.linkedin.common.*;
import com.linkedin.common.urn.CorpGroupUrn;
import com.linkedin.common.urn.DataPlatformUrn;
import com.linkedin.common.urn.DatasourceUrn;
import com.linkedin.common.urn.Urn;
import com.linkedin.common.urn.*;
import com.linkedin.data.template.StringArray;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.authorization.AuthorizationUtils;
import com.linkedin.datahub.graphql.authorization.ConjunctivePrivilegeGroup;
import com.linkedin.datahub.graphql.authorization.DisjunctivePrivilegeGroup;
import com.linkedin.datahub.graphql.exception.AuthorizationException;
import com.linkedin.datahub.graphql.exception.DataHubGraphQLErrorCode;
import com.linkedin.datahub.graphql.exception.DataHubGraphQLException;
import com.linkedin.datahub.graphql.generated.CorpGroup;
import com.linkedin.datahub.graphql.generated.DatasourceCreateInput;
import com.linkedin.datahub.graphql.generated.DatasourceSourceInput;
import com.linkedin.datahub.graphql.types.corpgroup.mappers.CorpGroupMapper;
import com.linkedin.datasource.DatasourceConnectionGSB;
import com.linkedin.datasource.DatasourceConnectionPrimary;
import com.linkedin.datasource.DatasourceInfo;
import com.linkedin.datasource.sources.*;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.events.metadata.ChangeType;
import com.linkedin.metadata.authorization.PoliciesConfig;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.metadata.utils.GenericRecordUtils;
import com.linkedin.mxe.MetadataChangeProposal;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.linkedin.datahub.graphql.resolvers.ResolverUtils.bindArgument;
import static com.linkedin.datahub.graphql.resolvers.datasource.DatasourceConstants.*;
import static com.linkedin.metadata.Constants.*;

public class CreateDatasourceResolver implements DataFetcher<CompletableFuture<String>>
{
    private final EntityClient datasourceClient;
    private final EntityService entityService;

    public CreateDatasourceResolver(EntityClient datasourceClient, EntityService entityService) {
        this.datasourceClient = datasourceClient;
        this.entityService = entityService;
    }

    private DataPlatformUrn parseConnection(Map<String, Object> connInput,
                                            DatasourceConnectionPrimary primaryConn,
                                            DatasourceConnectionGSB gsbConn) {
        DataPlatformUrn platformUrn;
        if (connInput.containsKey(POSTGRES_SOURCE_NAME)) {
            PostgresSource postgres = bindArgument(
                    removeNullValues((Map<String, Object>)connInput.get(POSTGRES_SOURCE_NAME)),
                    PostgresSource.class);
            postgres.setDriver(postgres.getDriver());
            if(gsbConn != null) {
                gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(postgres));
            } else {
                primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(postgres));
            }
            platformUrn = new DataPlatformUrn(POSTGRES_SOURCE_NAME);
        } else if (connInput.containsKey(ORACLE_SOURCE_NAME)) {
            Map<String, Object> params = removeNullValues((Map<String, Object>)connInput.get(ORACLE_SOURCE_NAME));
            params.putIfAbsent("hostPort", "");
            params.putIfAbsent("serviceName", "");
            params.putIfAbsent("tnsName", "");
            OracleSource oracle = bindArgument(params, OracleSource.class);
            oracle.setDriver(oracle.getDriver());
            if(gsbConn != null) {
                gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(oracle));
            } else {
                primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(oracle));
            }
            platformUrn = new DataPlatformUrn(ORACLE_SOURCE_NAME);
        } else if (connInput.containsKey(ICEBERG_SOURCE_NAME)) {
            IcebergSource iceberg = bindArgument(
                    removeNullValues((Map<String, Object>)connInput.get(ICEBERG_SOURCE_NAME)),
                    IcebergSource.class);
            if(gsbConn != null) {
                gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(iceberg));
            } else {
                primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(iceberg));
            }
            platformUrn = new DataPlatformUrn(ICEBERG_SOURCE_NAME);
        } else if (connInput.containsKey(KAFKA_SOURCE_NAME)) {
            KafkaMetadataSource kafka = bindArgument(
                    removeNullValues((Map<String, Object>)connInput.get(KAFKA_SOURCE_NAME)),
                    KafkaMetadataSource.class);
            if(gsbConn != null) {
                gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(kafka));
            } else {
                primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(kafka));
            }
            platformUrn = new DataPlatformUrn(KAFKA_SOURCE_NAME);
        } else if (connInput.containsKey(MYSQL_SOURCE_NAME)) {
            MysqlSource mysql = bindArgument(
                    removeNullValues((Map<String, Object>)connInput.get(MYSQL_SOURCE_NAME)),
                    MysqlSource.class);
            mysql.setDriver(mysql.getDriver());
            if(gsbConn != null) {
                gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(mysql));
            } else {
                primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(mysql));
            }
            platformUrn = new DataPlatformUrn(MYSQL_SOURCE_NAME);
        } else if (connInput.containsKey(HIVE_SOURCE_NAME)) {
            HiveSource hive = bindArgument(
                    removeNullValues((Map<String, Object>)connInput.get(HIVE_SOURCE_NAME)),
                    HiveSource.class);
            hive.setDriver(hive.getDriver());
            if(gsbConn != null) {
                gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(hive));
            } else {
                primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(hive));
            }
            platformUrn = new DataPlatformUrn(HIVE_SOURCE_NAME);
        } else if (connInput.containsKey(PINOT_SOURCE_NAME)) {
            PinotSource pinot = bindArgument(
                    removeNullValues((Map<String, Object>)connInput.get(PINOT_SOURCE_NAME)),
                    PinotSource.class);
            pinot.setDriver(pinot.getDriver());
            if(gsbConn != null) {
                gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(pinot));
            } else {
                primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(pinot));
            }
            platformUrn = new DataPlatformUrn(PINOT_SOURCE_NAME);
        } else if (connInput.containsKey(PRESTO_SOURCE_NAME)) {
            PrestoSource presto = bindArgument(
                    removeNullValues((Map<String, Object>)connInput.get(PRESTO_SOURCE_NAME)),
                    PrestoSource.class);
            presto.setDriver(presto.getDriver());
            if(gsbConn != null) {
                gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(presto));
            } else {
                primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(presto));
            }
            platformUrn = new DataPlatformUrn(PRESTO_SOURCE_NAME);
        } else if (connInput.containsKey(TIDB_SOURCE_NAME)) {
            TiDBSource tidb = bindArgument(
                    removeNullValues((Map<String, Object>)connInput.get(TIDB_SOURCE_NAME)),
                    TiDBSource.class);
            tidb.setDriver(tidb.getDriver());
            if(gsbConn != null) {
                gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(tidb));
            } else {
                primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(tidb));
            }
            platformUrn = new DataPlatformUrn(TIDB_SOURCE_NAME);
        } else if (connInput.containsKey(TRINO_SOURCE_NAME)) {
            TrinoSource trino = bindArgument(
                    removeNullValues((Map<String, Object>)connInput.get(TRINO_SOURCE_NAME)),
                    TrinoSource.class);
            trino.setDriver(trino.getDriver());
            if(gsbConn != null) {
                gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(trino));
            } else {
                primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(trino));
            }
            platformUrn = new DataPlatformUrn(TRINO_SOURCE_NAME);
        } else if (connInput.containsKey(SNOWFLAKE_SOURCE_NAME)) {
            SnowflakeSource snowflake = bindArgument(
                    removeNullValues((Map<String, Object>)connInput.get(SNOWFLAKE_SOURCE_NAME)),
                    SnowflakeSource.class);
            snowflake.setDriver(snowflake.getDriver());
            if(gsbConn != null) {
                gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(snowflake));
            } else {
                primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(snowflake));
            }
            platformUrn = new DataPlatformUrn(SNOWFLAKE_SOURCE_NAME);
        } else {
            throw new IllegalArgumentException("Unknown source type");
        }
        return platformUrn;
    }

    @Override
    public CompletableFuture<String> get(DataFetchingEnvironment environment) throws Exception {

        final QueryContext context = environment.getContext();

        Map<String, Object> inputMap = environment.getArgument("input");
        final DatasourceCreateInput input = bindArgument(inputMap, DatasourceCreateInput.class);

        String sourceName = input.getName().trim();

        final DatasourceInfo datasourceInfo = new DatasourceInfo();

        String sourceRegion = input.getRegion().trim();
        datasourceInfo.setRegion(sourceRegion);
        datasourceInfo.setName(sourceName);

        AuditStamp lastModified = new AuditStamp();
        lastModified.setTime(new Date().getTime());
        lastModified.setActor(UrnUtils.getUrn(context.getActorUrn()));
        datasourceInfo.setLastModified(lastModified);

        if (input.getGroup() != null) {
            CorpGroupUrn corpGroupUrn = CorpGroupUrn.createFromString(input.getGroup());
            datasourceInfo.setGroup(corpGroupUrn);
        }
        if(input.getGroup() == null || input.getGroup().equalsIgnoreCase("urn:li:corpGroup:none")) {
            throw new DataHubGraphQLException("No group was found", DataHubGraphQLErrorCode.BAD_REQUEST);
        }

        if (input.getAlias() != null) {
            datasourceInfo.setAlias(input.getAlias());
        }
        if (input.getTestQuerySql() != null) {
            datasourceInfo.setTestQuerySql(input.getTestQuerySql());
        }

        final DatasourceConnectionPrimary primaryConn = new DatasourceConnectionPrimary();
        DatasourceSourceInput priInput = input.getPrimaryConn();
        if (priInput != null && priInput.getDataCenter() != null) {
            primaryConn.setDataCenter(priInput.getDataCenter());
        }

        DataPlatformUrn primaryPlatformUrn = parseConnection(
                (Map<String, Object>)inputMap.get("primaryConn"), primaryConn, null);

        final DatasourceUrn sourceUrn = new DatasourceUrn(primaryPlatformUrn, sourceName.toLowerCase(), sourceRegion);
        if (input.getCreate() && entityService.exists(sourceUrn)) {
            throw new DataHubGraphQLException("Duplicate data source", DataHubGraphQLErrorCode.BAD_REQUEST);
        }

        DatasourceConnectionGSB gsbConn = null;
        DataPlatformUrn gsbPlatformUrn = null;
        if (input.getGsbConn() != null) {
            gsbConn = new DatasourceConnectionGSB();
            DatasourceSourceInput gsbInput = input.getGsbConn();
            if (gsbInput.getDataCenter() != null) {
                gsbConn.setDataCenter(gsbInput.getDataCenter());
            }
            gsbPlatformUrn = parseConnection(
                    (Map<String, Object>)inputMap.get("gsbConn"), null, gsbConn);
        }

        if (gsbPlatformUrn != null && !primaryPlatformUrn.equals(gsbPlatformUrn)) {
            throw new DataHubGraphQLException("GSB platform was different from primary platform",
                    DataHubGraphQLErrorCode.BAD_REQUEST);
        }

        final MetadataChangeProposal sourceInfoProposal = new MetadataChangeProposal();
        sourceInfoProposal.setEntityUrn(sourceUrn);
        sourceInfoProposal.setAspectName("datasourceInfo");
        sourceInfoProposal.setEntityType("datasource");
        sourceInfoProposal.setAspect(GenericRecordUtils.serializeAspect(datasourceInfo));
        sourceInfoProposal.setChangeType(ChangeType.UPSERT);

        final MetadataChangeProposal primaryConnProposal = new MetadataChangeProposal();
        primaryConnProposal.setEntityUrn(sourceUrn);
        primaryConnProposal.setAspectName("datasourceConnectionPrimary");
        primaryConnProposal.setEntityType("datasource");
        primaryConnProposal.setAspect(GenericRecordUtils.serializeAspect(primaryConn));
        primaryConnProposal.setChangeType(ChangeType.UPSERT);

        final MetadataChangeProposal gsbConnProposal = new MetadataChangeProposal();
        if (gsbConn != null) {
            gsbConnProposal.setEntityUrn(sourceUrn);
            gsbConnProposal.setAspectName("datasourceConnectionGSB");
            gsbConnProposal.setEntityType("datasource");
            gsbConnProposal.setAspect(GenericRecordUtils.serializeAspect(gsbConn));
            gsbConnProposal.setChangeType(ChangeType.UPSERT);
        }

        final boolean hasGSB = gsbConn != null;

        final MetadataChangeProposal ownershipProposal = new MetadataChangeProposal();
        final MetadataChangeProposal browsePathsProposal = new MetadataChangeProposal();

        if(input.getCreate()) {
            Ownership ownership = createOwnership(context, datasourceInfo);
            ownershipProposal.setEntityUrn(sourceUrn);
            ownershipProposal.setAspectName(OWNERSHIP_ASPECT_NAME);
            ownershipProposal.setEntityType("datasource");
            ownershipProposal.setAspect(GenericRecordUtils.serializeAspect(ownership));
            ownershipProposal.setChangeType(ChangeType.UPSERT);

            BrowsePaths browsePaths = createBrowsePaths(
                    primaryPlatformUrn.getPlatformNameEntity(), datasourceInfo);
            browsePathsProposal.setEntityUrn(sourceUrn);
            browsePathsProposal.setAspectName(BROWSE_PATHS_ASPECT_NAME);
            browsePathsProposal.setEntityType("datasource");
            browsePathsProposal.setAspect(GenericRecordUtils.serializeAspect(browsePaths));
            browsePathsProposal.setChangeType(ChangeType.UPSERT);
        }

        return CompletableFuture.supplyAsync(() -> {
            validateDatasource(sourceUrn, context);
            try {
                datasourceClient.ingestProposal(primaryConnProposal, context.getAuthentication());
                if (hasGSB) {
                    datasourceClient.ingestProposal(gsbConnProposal, context.getAuthentication());
                }
                if(input.getCreate()) {
                    datasourceClient.ingestProposal(ownershipProposal, context.getAuthentication());
                    datasourceClient.ingestProposal(browsePathsProposal, context.getAuthentication());
                }
                return datasourceClient.ingestProposal(sourceInfoProposal, context.getAuthentication());
            } catch (Exception ex) {
                throw new RuntimeException("Failed to add datasource.", ex);
            }
        });
    }

    private void validateDatasource(DatasourceUrn maybeDatasourceUrn, QueryContext context) {
        final Urn resourceUrn = UrnUtils.getUrn(maybeDatasourceUrn.toString());
        if (!entityService.exists(resourceUrn)) {
            return;
        }
        if (!isAuthorizedToUpdateDatasource(context, resourceUrn)) {
            throw new AuthorizationException(
                    "Unauthorized to perform this action. Please contact your DataHub administrator.");
        }
    }

    public static boolean isAuthorizedToUpdateDatasource(@Nonnull QueryContext context, Urn entityUrn) {
        final DisjunctivePrivilegeGroup orPrivilegeGroups = new DisjunctivePrivilegeGroup(ImmutableList.of(
                new ConjunctivePrivilegeGroup(ImmutableList.of(PoliciesConfig.EDIT_DATASOURCE_PRIVILEGE.getType()))
        ));

        return AuthorizationUtils.isAuthorized(context.getAuthorizer(),
                context.getActorUrn(), entityUrn.getEntityType(), entityUrn.toString(), orPrivilegeGroups);
    }

    private Ownership createOwnership(QueryContext context, DatasourceInfo datasourceInfo) {
        Ownership ownership = new Ownership();
        Owner owner = new Owner();
        owner.setOwner(UrnUtils.getUrn(context.getActorUrn()));
        owner.setType(OwnershipType.BUSINESS_OWNER);
        owner.setSource(new OwnershipSource().setType(OwnershipSourceType.MANUAL));
        Owner owner2 = new Owner();
        owner2.setOwner(datasourceInfo.getGroup());
        owner2.setType(OwnershipType.BUSINESS_OWNER);
        owner2.setSource(new OwnershipSource().setType(OwnershipSourceType.MANUAL));
        OwnerArray owners = new OwnerArray();
        owners.add(owner);
        owners.add(owner2);
        ownership.setOwners(owners);
        return ownership;
    }

    private BrowsePaths createBrowsePaths(String platform, DatasourceInfo datasourceInfo) throws Exception {
        Set<String> aspects = new HashSet<>();
        aspects.add(CORP_GROUP_INFO_ASPECT_NAME);
        EntityResponse entityResponse = entityService.getEntityV2(
                CORP_GROUP_ENTITY_NAME, datasourceInfo.getGroup(), aspects);

        assert entityResponse != null;
        CorpGroup corpGroup = CorpGroupMapper.map(entityResponse);
        BrowsePaths browsePaths = new BrowsePaths();
        StringArray paths = new StringArray();
        paths.add("/" + datasourceInfo.getRegion().toLowerCase() +
                "/" + corpGroup.getProperties().getDisplayName().toLowerCase() + "/" + platform.toLowerCase());
        browsePaths.setPaths(paths);
        return browsePaths;
    }

    private Map<String, Object> removeNullValues(Map<String, Object> values) {
        String[] keys = values.keySet().toArray(new String[0]);
        for(String key : keys) {
            if(values.get(key) == null) {
                values.remove(key);
            }
        }
        return values;
    }

}