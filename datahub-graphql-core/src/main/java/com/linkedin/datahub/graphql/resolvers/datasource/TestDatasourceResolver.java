package com.linkedin.datahub.graphql.resolvers.datasource;

import com.linkedin.datahub.graphql.generated.DatasourceSourceInput;
import com.linkedin.datahub.graphql.generated.DatasourceTestInput;
import com.linkedin.datasource.sources.*;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static com.linkedin.datahub.graphql.resolvers.ResolverUtils.bindArgument;
import static com.linkedin.datahub.graphql.resolvers.datasource.DatasourceConstants.*;

@Slf4j
public class TestDatasourceResolver implements DataFetcher<CompletableFuture<Boolean>>
{
    @Override
    public CompletableFuture<Boolean> get(DataFetchingEnvironment environment) throws Exception {

        Map<String, Object> inputMap = environment.getArgument("input");

        final DatasourceTestInput input = bindArgument(inputMap, DatasourceTestInput.class);
        DatasourceSourceInput sourceInput = input.getConnection();

        String type;
        Properties props = new Properties();
        if (sourceInput.getPostgres() != null) {
            type = POSTGRES_SOURCE_NAME;
            putProperties(props,
                    sourceInput.getPostgres().getUsername(),
                    sourceInput.getPostgres().getPassword(),
                    new PostgresSource().getDriver(), null);
        } else if (sourceInput.getOracle() != null) {
            type = ORACLE_SOURCE_NAME;
            putProperties(props,
                    sourceInput.getOracle().getUsername(),
                    sourceInput.getOracle().getPassword(),
                    new OracleSource().getDriver(), null);
        } else if (sourceInput.getMysql() != null) {
            type = MYSQL_SOURCE_NAME;
            putProperties(props,
                    sourceInput.getMysql().getUsername(),
                    sourceInput.getMysql().getPassword(),
                    new MysqlSource().getDriver(), null);
        } else if (sourceInput.getHive() != null) {
            type = HIVE_SOURCE_NAME;
            putProperties(props,
                    sourceInput.getHive().getUsername(),
                    sourceInput.getHive().getPassword(),
                    new HiveSource().getDriver(), null);
        } else if (sourceInput.getPinot() != null) {
            type = PINOT_SOURCE_NAME;
            pinotProps(props,
                    sourceInput.getPinot().getUsername(),
                    sourceInput.getPinot().getPassword());
        } else if (sourceInput.getPresto() != null) {
            type = PRESTO_SOURCE_NAME;
            putProperties(props,
                    sourceInput.getPresto().getUsername(),
                    sourceInput.getPresto().getPassword(),
                    null, sourceInput.getPresto().getJdbcParams());
            sourceInput.getPresto().setJdbcParams("");
        } else if (sourceInput.getTrino() != null) {
            type = TRINO_SOURCE_NAME;
            putProperties(props,
                    sourceInput.getTrino().getUsername(),
                    sourceInput.getTrino().getPassword(),
                    null, sourceInput.getTrino().getJdbcParams());
            sourceInput.getTrino().setJdbcParams("");
        } else if (sourceInput.getTiDB() != null) {
            type = TIDB_SOURCE_NAME;
            putProperties(props,
                    sourceInput.getTiDB().getUsername(),
                    sourceInput.getTiDB().getPassword(),
                    new TiDBSource().getDriver(), null);
        } else if (sourceInput.getSnowflake() != null) {
            type = SNOWFLAKE_SOURCE_NAME;
            putProperties(props,
                    sourceInput.getSnowflake().getUsername(),
                    sourceInput.getSnowflake().getPassword(),
                    new SnowflakeSource().getDriver(), null);
        } else if (sourceInput.getIceberg() != null) {
            type = ICEBERG_SOURCE_NAME;
        } else if (sourceInput.getKafka() != null) {
            type = KAFKA_SOURCE_NAME;
        } else {
            throw new IllegalArgumentException("Unknown source type");
        }

        if (supportType(type)) {

            final String jdbcUrl = parseJdbcUrl(type, sourceInput);

            return CompletableFuture.supplyAsync(() -> {
                try (Connection conn = DriverManager.getConnection(jdbcUrl, props);
                     Statement stat = conn.createStatement()) {
                    stat.executeQuery(input.getTestQuerySql());
                    return true;
                } catch (Exception ex) {
                    log.error("Failed to test datasource.", ex);
                    return false;
                }
            });

        }

        throw new IllegalArgumentException("Not support type:" + type);
    }

    private static String parseJdbcUrl(String type, DatasourceSourceInput sourceInput) {

        if (PINOT_SOURCE_NAME.equals(type)) {
            return "jdbc:" + JDBC_TYPES.get(type) + "://" + sourceInput.getPinot().getHostPort();
        } else if (TRINO_SOURCE_NAME.equals(type)) {
            return urlFor(type,
                    sourceInput.getTrino().getHostPort(),
                    sourceInput.getTrino().getCatalog(),
                    sourceInput.getTrino().getSchema(),
                    sourceInput.getTrino().getJdbcParams()
            );
        } else if(PRESTO_SOURCE_NAME.equals(type)) {
            return urlFor(type,
                    sourceInput.getPresto().getHostPort(),
                    sourceInput.getPresto().getCatalog(),
                    sourceInput.getPresto().getSchema(),
                    sourceInput.getPresto().getJdbcParams()
            );
        } else if (ORACLE_SOURCE_NAME.equals(type)) {
            String jdbcUrl = "jdbc:" + JDBC_TYPES.get(type) + ":@";
            String hostPort = sourceInput.getOracle().getHostPort();
            String serviceName = sourceInput.getOracle().getServiceName();
            if (StringUtils.isNotEmpty(hostPort) && StringUtils.isNotEmpty(serviceName)) {
                jdbcUrl += "//" + hostPort + "/" + serviceName;
            } else {
                jdbcUrl += sourceInput.getOracle().getTnsName();
            }
            return jdbcUrl;
        } else if (HIVE_SOURCE_NAME.equals(type)) {
            return urlFor(type,
                    sourceInput.getHive().getHostPort(),
                    sourceInput.getHive().getDatabase(),
                    null,
                    sourceInput.getHive().getJdbcParams()
            );
        } else if (POSTGRES_SOURCE_NAME.equals(type)) {
            return urlFor(type,
                    sourceInput.getPostgres().getHostPort(),
                    sourceInput.getPostgres().getDatabase(),
                    null,
                    sourceInput.getPostgres().getJdbcParams()
            );
        } else if (MYSQL_SOURCE_NAME.equals(type)) {
            return urlFor(type,
                    sourceInput.getMysql().getHostPort(),
                    sourceInput.getMysql().getDatabase(),
                    null,
                    sourceInput.getMysql().getJdbcParams()
            );
        } else if (TIDB_SOURCE_NAME.equals(type)) {
            return urlFor(type,
                    sourceInput.getTiDB().getHostPort(),
                    sourceInput.getTiDB().getDatabase(),
                    null,
                    sourceInput.getTiDB().getJdbcParams()
            );
        } else if (SNOWFLAKE_SOURCE_NAME.equals(type)) {
            String jdbcUrl = "jdbc:" + JDBC_TYPES.get(type) + "://" +
                    sourceInput.getSnowflake().getHostPort() + "/";
            String connParams = sourceInput.getSnowflake().getConnectionParams();
            if (StringUtils.isNotEmpty(connParams)) {
                jdbcUrl += connParams.startsWith("?") ? connParams : ("?" + connParams);
            }
            return jdbcUrl;
        }

        throw new IllegalArgumentException("Not support the type:" + type);
    }

    private static void putProperties(
            Properties props, String user, String password, String driver, String jdbcParams) {
        props.put("user", user);
        props.put("password", password);

        if(StringUtils.isNotEmpty(driver)) {
            props.put("driver", driver);
        }
        if(StringUtils.isNotEmpty(jdbcParams)) {
            String[] params = StringUtils.split(jdbcParams, "&");
            for(String each : params) {
                String[] keyValue = StringUtils.split(each, "=");
                props.put(keyValue[0], keyValue[1]);
            }
        }

    }

    private static String urlFor(String type, String hostPort, String catalog, String schema, String jdbcParams) {
        String jdbcUrl = "jdbc:" + JDBC_TYPES.get(type) + "://" + hostPort;
        if (StringUtils.isNotEmpty(catalog)) {
            jdbcUrl += "/" + catalog;
            if (StringUtils.isNotEmpty(schema)) {
                jdbcUrl += "/" + schema;
            }
        }
        if (StringUtils.isNotEmpty(jdbcParams)) {
            jdbcParams = jdbcParams.startsWith("?") ? jdbcParams : ("?" + jdbcParams);
            jdbcUrl += jdbcParams;
        }
        return jdbcUrl;
    }

    private static void pinotProps(Properties props, String user, String password) {
        String plainCredentials = user + ":" + password;
        String base64Credentials = new String(Base64.getEncoder().encode(plainCredentials.getBytes()));
        String authorizationHeader = "Basic " + base64Credentials;
        props.setProperty("headers.Authorization", authorizationHeader);
        props.put("driver", new PinotSource().getDriver());
    }

    private static boolean supportType(String type) {
        return HIVE_SOURCE_NAME.equals(type)
                || MYSQL_SOURCE_NAME.equals(type)
                || ORACLE_SOURCE_NAME.equals(type)
                || PINOT_SOURCE_NAME.equals(type)
                || POSTGRES_SOURCE_NAME.equals(type)
                || PRESTO_SOURCE_NAME.equals(type)
                || SNOWFLAKE_SOURCE_NAME.equals(type)
                || TIDB_SOURCE_NAME.equals(type)
                || TRINO_SOURCE_NAME.equals(type);
    }

}