package com.linkedin.metadata.resources.entity.datasource;

import com.datahub.authentication.AuthenticationContext;
import com.linkedin.common.Owner;
import com.linkedin.common.OwnershipType;
import com.linkedin.common.Ownership;
import com.linkedin.common.BrowsePaths;
import com.linkedin.common.OwnerArray;
import com.linkedin.common.OwnershipSource;
import com.linkedin.common.OwnershipSourceType;
import com.linkedin.common.urn.CorpGroupUrn;
import com.linkedin.common.urn.DataPlatformUrn;
import com.linkedin.common.urn.DatasourceUrn;
import com.linkedin.data.template.StringArray;
import com.linkedin.datasource.DatasourceConnectionGSB;
import com.linkedin.datasource.DatasourceConnectionPrimary;
import com.linkedin.datasource.DatasourceInfo;
import com.linkedin.datasource.sources.PostgresSource;
import com.linkedin.datasource.sources.OracleSource;
import com.linkedin.datasource.sources.HiveSource;
import com.linkedin.datasource.sources.TiDBSource;
import com.linkedin.datasource.sources.MysqlSource;
import com.linkedin.datasource.sources.SnowflakeSource;
import com.linkedin.datasource.sources.TrinoSource;
import com.linkedin.datasource.sources.PrestoSource;
import com.linkedin.datasource.sources.PinotSource;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.events.metadata.ChangeType;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.metadata.utils.GenericRecordUtils;
import com.linkedin.mxe.MetadataChangeProposal;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.linkedin.metadata.Constants.BROWSE_PATHS_ASPECT_NAME;
import static com.linkedin.metadata.Constants.OWNERSHIP_ASPECT_NAME;

@Slf4j
public class DatasourceService {
    public static final String KAFKA_SOURCE_NAME = "kafka";
    public static final String ORACLE_SOURCE_NAME = "oracle";
    public static final String MYSQL_SOURCE_NAME = "mysql";
    public static final String ICEBERG_SOURCE_NAME = "iceberg";
    public static final String POSTGRES_SOURCE_NAME = "postgres";
    public static final String HIVE_SOURCE_NAME = "hive";
    public static final String PINOT_SOURCE_NAME = "pinot";
    public static final String PRESTO_SOURCE_NAME = "presto";
    public static final String TIDB_SOURCE_NAME = "tiDB";
    public static final String TRINO_SOURCE_NAME = "trino";
    public static final String SNOWFLAKE_SOURCE_NAME = "snowflake";

    private final EntityClient datasourceClient;
    private final EntityService entityService;

    public DatasourceService(EntityClient datasourceClient, EntityService entityService) {
        this.datasourceClient = datasourceClient;
        this.entityService = entityService;
    }

    private ConnectInfo parseConnection(DatasourceSync.Source connInput,
                                            DatasourceConnectionPrimary primaryConn,
                                            DatasourceConnectionGSB gsbConn) {
        boolean primary = false;
        boolean gsb = false;
        if ("postgresql".equalsIgnoreCase(connInput.getDataSourceType())
                || "postgres".equalsIgnoreCase(connInput.getDataSourceType())) {
            for (DatasourceSync.SourceConfig config : connInput.getConfigs()) {
                PostgresSource postgres = new PostgresSource();
                postgres.setDriver(config.getDriver());
                postgres.setIdleSize(config.getIdleSize());
                postgres.setMaxSize(config.getMaxSize());
                postgres.setMinSize(config.getMinSize());
                postgres.setStatus(config.getStatus());
                postgres.setUsername(config.getUsername());
                postgres.setPassword(config.getPassword());
                if (config.getJdbcParams() != null) {
                    postgres.setJdbcParams(config.getJdbcParams());
                }
                postgres.setHostPort(config.getHostPort());
                postgres.setDatabase(config.getDatabase());
                if (!primary && "PRIMARY".equalsIgnoreCase(config.getCluster())) {
                    primaryConn.setDataCenter(config.getDataCenter());
                    primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(postgres));
                    primary = true;
                } else if (!gsb && "GSB".equalsIgnoreCase(config.getCluster())) {
                    gsbConn.setDataCenter(config.getDataCenter());
                    gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(postgres));
                    gsb = true;
                } else {
                    throw new RuntimeException("Invalid cluster type -> " + config.getCluster());
                }
            }
            return ConnectInfo.builder()
                    .urn(new DataPlatformUrn(POSTGRES_SOURCE_NAME)).primary(primary).gsb(gsb).build();
        }

        if ("oracle".equalsIgnoreCase(connInput.getDataSourceType())) {
            for (DatasourceSync.SourceConfig config : connInput.getConfigs()) {
                OracleSource oracle = new OracleSource();
                oracle.setDriver(config.getDriver());
                oracle.setIdleSize(config.getIdleSize());
                oracle.setMaxSize(config.getMaxSize());
                oracle.setMinSize(config.getMinSize());
                oracle.setStatus(config.getStatus());
                oracle.setUsername(config.getUsername());
                oracle.setPassword(config.getPassword());
                oracle.setServiceName(config.getServiceName());
                oracle.setHostPort(config.getHostPort());
                if (!primary && "PRIMARY".equalsIgnoreCase(config.getCluster())) {
                    primaryConn.setDataCenter(config.getDataCenter());
                    primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(oracle));
                    primary = true;
                } else if (!gsb && "GSB".equalsIgnoreCase(config.getCluster())) {
                    gsbConn.setDataCenter(config.getDataCenter());
                    gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(oracle));
                    gsb = true;
                } else {
                    throw new RuntimeException("Invalid cluster type -> " + config.getCluster());
                }
            }
            return ConnectInfo.builder()
                    .urn(new DataPlatformUrn(ORACLE_SOURCE_NAME)).primary(primary).gsb(gsb).build();
        }
        if ("mysql".equalsIgnoreCase(connInput.getDataSourceType())) {
            for (DatasourceSync.SourceConfig config : connInput.getConfigs()) {
                MysqlSource mysql = new MysqlSource();
                mysql.setDriver(config.getDriver());
                mysql.setIdleSize(config.getIdleSize());
                mysql.setMaxSize(config.getMaxSize());
                mysql.setMinSize(config.getMinSize());
                mysql.setStatus(config.getStatus());
                mysql.setUsername(config.getUsername());
                mysql.setPassword(config.getPassword());
                mysql.setDatabase(config.getDatabase());
                mysql.setHostPort(config.getHostPort());
                if (config.getJdbcParams() != null) {
                    mysql.setJdbcParams(config.getJdbcParams());
                }
                if (!primary && "PRIMARY".equalsIgnoreCase(config.getCluster())) {
                    primaryConn.setDataCenter(config.getDataCenter());
                    primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(mysql));
                    primary = true;
                } else if (!gsb && "GSB".equalsIgnoreCase(config.getCluster())) {
                    gsbConn.setDataCenter(config.getDataCenter());
                    gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(mysql));
                    gsb = true;
                } else {
                    throw new RuntimeException("Invalid cluster type -> " + config.getCluster());
                }
            }
            return ConnectInfo.builder()
                    .urn(new DataPlatformUrn(MYSQL_SOURCE_NAME)).primary(primary).gsb(gsb).build();
        }
        if ("hive".equalsIgnoreCase(connInput.getDataSourceType())) {
            for (DatasourceSync.SourceConfig config : connInput.getConfigs()) {
                HiveSource hive = new HiveSource();
                hive.setDriver(config.getDriver());
                hive.setIdleSize(config.getIdleSize());
                hive.setMaxSize(config.getMaxSize());
                hive.setMinSize(config.getMinSize());
                hive.setStatus(config.getStatus());
                hive.setUsername(config.getUsername());
                hive.setPassword(config.getPassword());
                hive.setDatabase(config.getDatabase());
                hive.setHostPort(config.getHostPort());
                if (config.getJdbcParams() != null) {
                    hive.setJdbcParams(config.getJdbcParams());
                }
                if (!primary && "PRIMARY".equalsIgnoreCase(config.getCluster())) {
                    primaryConn.setDataCenter(config.getDataCenter());
                    primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(hive));
                    primary = true;
                } else if (!gsb && "GSB".equalsIgnoreCase(config.getCluster())) {
                    gsbConn.setDataCenter(config.getDataCenter());
                    gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(hive));
                    gsb = true;
                } else {
                    throw new RuntimeException("Invalid cluster type -> " + config.getCluster());
                }
            }
            return ConnectInfo.builder()
                    .urn(new DataPlatformUrn(HIVE_SOURCE_NAME)).primary(primary).gsb(gsb).build();
        }
        if ("pinot".equalsIgnoreCase(connInput.getDataSourceType())) {
            for (DatasourceSync.SourceConfig config : connInput.getConfigs()) {
                PinotSource pinot = new PinotSource();
                pinot.setDriver(config.getDriver());
                pinot.setIdleSize(config.getIdleSize());
                pinot.setMaxSize(config.getMaxSize());
                pinot.setMinSize(config.getMinSize());
                pinot.setStatus(config.getStatus());
                pinot.setUsername(config.getUsername());
                pinot.setPassword(config.getPassword());
                pinot.setHostPort(config.getHostPort());
                if (!primary && "PRIMARY".equalsIgnoreCase(config.getCluster())) {
                    primaryConn.setDataCenter(config.getDataCenter());
                    primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(pinot));
                    primary = true;
                } else if (!gsb && "GSB".equalsIgnoreCase(config.getCluster())) {
                    gsbConn.setDataCenter(config.getDataCenter());
                    gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(pinot));
                    gsb = true;
                } else {
                    throw new RuntimeException("Invalid cluster type -> " + config.getCluster());
                }
            }
            return ConnectInfo.builder()
                    .urn(new DataPlatformUrn(PINOT_SOURCE_NAME)).primary(primary).gsb(gsb).build();
        }
        if ("presto".equalsIgnoreCase(connInput.getDataSourceType())) {
            for (DatasourceSync.SourceConfig config : connInput.getConfigs()) {
                PrestoSource presto = new PrestoSource();
                presto.setDriver(config.getDriver());
                presto.setIdleSize(config.getIdleSize());
                presto.setMaxSize(config.getMaxSize());
                presto.setMinSize(config.getMinSize());
                presto.setStatus(config.getStatus());
                presto.setUsername(config.getUsername());
                presto.setPassword(config.getPassword());
                presto.setCatalog(config.getCatalog());
                presto.setSchema(config.getSchema());
                presto.setHostPort(config.getHostPort());
                if (config.getJdbcParams() != null) {
                    presto.setJdbcParams(config.getJdbcParams());
                }
                if (!primary && "PRIMARY".equalsIgnoreCase(config.getCluster())) {
                    primaryConn.setDataCenter(config.getDataCenter());
                    primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(presto));
                    primary = true;
                } else if (!gsb && "GSB".equalsIgnoreCase(config.getCluster())) {
                    gsbConn.setDataCenter(config.getDataCenter());
                    gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(presto));
                    gsb = true;
                } else {
                    throw new RuntimeException("Invalid cluster type -> " + config.getCluster());
                }
            }
            return ConnectInfo.builder()
                    .urn(new DataPlatformUrn(PRESTO_SOURCE_NAME)).primary(primary).gsb(gsb).build();
        }
        if ("tidb".equalsIgnoreCase(connInput.getDataSourceType())) {
            for (DatasourceSync.SourceConfig config : connInput.getConfigs()) {
                TiDBSource tidb = new TiDBSource();
                tidb.setDriver(config.getDriver());
                tidb.setIdleSize(config.getIdleSize());
                tidb.setMaxSize(config.getMaxSize());
                tidb.setMinSize(config.getMinSize());
                tidb.setStatus(config.getStatus());
                tidb.setUsername(config.getUsername());
                tidb.setPassword(config.getPassword());
                tidb.setDatabase(config.getDatabase());
                tidb.setHostPort(config.getHostPort());
                if (config.getJdbcParams() != null) {
                    tidb.setJdbcParams(config.getJdbcParams());
                }
                if (!primary && "PRIMARY".equalsIgnoreCase(config.getCluster())) {
                    primaryConn.setDataCenter(config.getDataCenter());
                    primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(tidb));
                    primary = true;
                } else if (!gsb && "GSB".equalsIgnoreCase(config.getCluster())) {
                    gsbConn.setDataCenter(config.getDataCenter());
                    gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(tidb));
                    gsb = true;
                } else {
                    throw new RuntimeException("Invalid cluster type -> " + config.getCluster());
                }
            }
            return ConnectInfo.builder()
                    .urn(new DataPlatformUrn(TIDB_SOURCE_NAME)).primary(primary).gsb(gsb).build();
        }
        if ("trino".equalsIgnoreCase(connInput.getDataSourceType())) {
            for (DatasourceSync.SourceConfig config : connInput.getConfigs()) {
                TrinoSource trino = new TrinoSource();
                trino.setDriver(config.getDriver());
                trino.setIdleSize(config.getIdleSize());
                trino.setMaxSize(config.getMaxSize());
                trino.setMinSize(config.getMinSize());
                trino.setStatus(config.getStatus());
                trino.setUsername(config.getUsername());
                trino.setPassword(config.getPassword());
                trino.setCatalog(config.getCatalog());
                trino.setSchema(config.getSchema());
                trino.setHostPort(config.getHostPort());
                if (config.getJdbcParams() != null) {
                    trino.setJdbcParams(config.getJdbcParams());
                }
                if (!primary && "PRIMARY".equalsIgnoreCase(config.getCluster())) {
                    primaryConn.setDataCenter(config.getDataCenter());
                    primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(trino));
                    primary = true;
                } else if (!gsb && "GSB".equalsIgnoreCase(config.getCluster())) {
                    gsbConn.setDataCenter(config.getDataCenter());
                    gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(trino));
                    gsb = true;
                } else {
                    throw new RuntimeException("Invalid cluster type -> " + config.getCluster());
                }
            }
            return ConnectInfo.builder()
                    .urn(new DataPlatformUrn(TRINO_SOURCE_NAME)).primary(primary).gsb(gsb).build();
        }
        if ("snowflake".equalsIgnoreCase(connInput.getDataSourceType())) {
            for (DatasourceSync.SourceConfig config : connInput.getConfigs()) {
                SnowflakeSource snowflake = new SnowflakeSource();
                snowflake.setDriver(config.getDriver());
                snowflake.setIdleSize(config.getIdleSize());
                snowflake.setMaxSize(config.getMaxSize());
                snowflake.setMinSize(config.getMinSize());
                snowflake.setStatus(config.getStatus());
                snowflake.setUsername(config.getUsername());
                snowflake.setPassword(config.getPassword());
                snowflake.setConnectionParams(config.getJdbcParams());
                snowflake.setHostPort(config.getHostPort());
                if (!primary && "PRIMARY".equalsIgnoreCase(config.getCluster())) {
                    primaryConn.setDataCenter(config.getDataCenter());
                    primaryConn.setConnection(DatasourceConnectionPrimary.Connection.create(snowflake));
                    primary = true;
                } else if (!gsb && "GSB".equalsIgnoreCase(config.getCluster())) {
                    gsbConn.setDataCenter(config.getDataCenter());
                    gsbConn.setConnection(DatasourceConnectionGSB.Connection.create(snowflake));
                    gsb = true;
                } else {
                    throw new RuntimeException("Invalid cluster type -> " + config.getCluster());
                }
            }
            return ConnectInfo.builder()
                    .urn(new DataPlatformUrn(SNOWFLAKE_SOURCE_NAME)).primary(primary).gsb(gsb).build();
        } else {
            throw new IllegalArgumentException("Unknown source type");
        }
    }

    public void createDatasource(DatasourceSync.Source source,
                                 String sourceRegion, String groupName, boolean forceUpdated) throws Exception {

        String sourceName = source.getDataSourceName();

        final DatasourceInfo datasourceInfo = new DatasourceInfo();
        datasourceInfo.setRegion(sourceRegion);

        CorpGroupUrn corpGroupUrn = CorpGroupUrn.createFromString("urn:li:corpGroup:" + groupName);
        datasourceInfo.setGroup(corpGroupUrn);
        datasourceInfo.setName(sourceName);

        if (source.getAlias() != null) {
            datasourceInfo.setAlias(source.getAlias());
        }
        if (source.getTestQuerySql() != null) {
            datasourceInfo.setTestQuerySql(source.getTestQuerySql());
        }

        DatasourceConnectionPrimary primaryConn = new DatasourceConnectionPrimary();
        DatasourceConnectionGSB gsbConn = new DatasourceConnectionGSB();

        ConnectInfo connectInfo = parseConnection(source, primaryConn, gsbConn);
        final DatasourceUrn sourceUrn = new DatasourceUrn(connectInfo.getUrn(), sourceName.toLowerCase(), sourceRegion);

        if (!forceUpdated && entityService.exists(sourceUrn)) {
            log.info("Datasource[" + sourceUrn + "] exists!!!");
            return;
        }

        final MetadataChangeProposal sourceInfoProposal = new MetadataChangeProposal();
        sourceInfoProposal.setEntityUrn(sourceUrn);
        sourceInfoProposal.setAspectName("datasourceInfo");
        sourceInfoProposal.setEntityType("datasource");
        sourceInfoProposal.setAspect(GenericRecordUtils.serializeAspect(datasourceInfo));
        sourceInfoProposal.setChangeType(ChangeType.UPSERT);

        final MetadataChangeProposal primaryConnProposal = new MetadataChangeProposal();
        if (connectInfo.isPrimary()) {
            primaryConnProposal.setEntityUrn(sourceUrn);
            primaryConnProposal.setAspectName("datasourceConnectionPrimary");
            primaryConnProposal.setEntityType("datasource");
            primaryConnProposal.setAspect(GenericRecordUtils.serializeAspect(primaryConn));
            primaryConnProposal.setChangeType(ChangeType.UPSERT);
        }

        final MetadataChangeProposal gsbConnProposal = new MetadataChangeProposal();
        if (connectInfo.isGsb()) {
            gsbConnProposal.setEntityUrn(sourceUrn);
            gsbConnProposal.setAspectName("datasourceConnectionGSB");
            gsbConnProposal.setEntityType("datasource");
            gsbConnProposal.setAspect(GenericRecordUtils.serializeAspect(gsbConn));
            gsbConnProposal.setChangeType(ChangeType.UPSERT);
        }

        final MetadataChangeProposal ownershipProposal = new MetadataChangeProposal();
        final MetadataChangeProposal browsePathsProposal = new MetadataChangeProposal();

        Ownership ownership = createOwnership(datasourceInfo);
        ownershipProposal.setEntityUrn(sourceUrn);
        ownershipProposal.setAspectName(OWNERSHIP_ASPECT_NAME);
        ownershipProposal.setEntityType("datasource");
        ownershipProposal.setAspect(GenericRecordUtils.serializeAspect(ownership));
        ownershipProposal.setChangeType(ChangeType.UPSERT);

        BrowsePaths browsePaths = createBrowsePaths(
                connectInfo.getUrn().getPlatformNameEntity(), groupName, datasourceInfo);
        browsePathsProposal.setEntityUrn(sourceUrn);
        browsePathsProposal.setAspectName(BROWSE_PATHS_ASPECT_NAME);
        browsePathsProposal.setEntityType("datasource");
        browsePathsProposal.setAspect(GenericRecordUtils.serializeAspect(browsePaths));
        browsePathsProposal.setChangeType(ChangeType.UPSERT);

        if (connectInfo.isPrimary()) {
            datasourceClient.ingestProposal(primaryConnProposal, AuthenticationContext.getAuthentication());
        }
        if (connectInfo.isGsb()) {
            datasourceClient.ingestProposal(gsbConnProposal, AuthenticationContext.getAuthentication());
        }
        datasourceClient.ingestProposal(ownershipProposal, AuthenticationContext.getAuthentication());
        datasourceClient.ingestProposal(browsePathsProposal, AuthenticationContext.getAuthentication());
        datasourceClient.ingestProposal(sourceInfoProposal, AuthenticationContext.getAuthentication());
    }

    private Ownership createOwnership(DatasourceInfo datasourceInfo) {
        Ownership ownership = new Ownership();
        Owner owner2 = new Owner();
        owner2.setOwner(datasourceInfo.getGroup());
        owner2.setType(OwnershipType.BUSINESS_OWNER);
        owner2.setSource(new OwnershipSource().setType(OwnershipSourceType.MANUAL));
        OwnerArray owners = new OwnerArray();
        owners.add(owner2);
        ownership.setOwners(owners);
        return ownership;
    }

    private BrowsePaths createBrowsePaths(String platform, String groupName, DatasourceInfo datasourceInfo) {
        BrowsePaths browsePaths = new BrowsePaths();
        StringArray paths = new StringArray();
        paths.add("/" + datasourceInfo.getRegion().toLowerCase() + "/" + groupName.toLowerCase() + "/" + platform.toLowerCase());
        browsePaths.setPaths(paths);
        return browsePaths;
    }

    @Builder
    @Getter
    static class ConnectInfo {
        private DataPlatformUrn urn;
        private boolean primary;
        private boolean gsb;
    }
}