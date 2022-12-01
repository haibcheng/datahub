package com.linkedin.metadata.resources.entity.datasource;

import com.datahub.authentication.AuthenticationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedin.common.urn.Urn;
import com.linkedin.metadata.client.JavaEntityClient;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.metadata.graph.GraphClient;
import com.linkedin.metadata.graph.GraphService;
import com.linkedin.metadata.graph.JavaGraphClient;
import com.linkedin.metadata.key.CorpGroupKey;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DatasourceSync {
    private EntityService entityService;
    private JavaEntityClient entityClient;
    private GraphClient graphClient;
    private GroupService groupService;
    private NativeUserService nativeUserService;
    private DatasourceService datasourceService;

    public static void main(String[] args) {
        new DatasourceSync().sync();
    }

    private DatasourceSync() { }

    public DatasourceSync(EntityService entityService, JavaEntityClient entityClient, GraphService graphService) {
        this.entityService = entityService;
        this.entityClient = entityClient;
        this.graphClient = new JavaGraphClient(graphService);
        this.groupService = new GroupService(entityClient, entityService, this.graphClient);
        this.nativeUserService = new NativeUserService(entityService, entityClient);
        this.datasourceService = new DatasourceService(entityClient, entityService);
    }

    public List<String> sync() {

        List<String> failures = new ArrayList<>();
        try {
            String[] regions = {"CANADA", "AMER", "GERMANY"};
            for (String region : regions) {
                JsonNode sources = getDataSources(region);
                for (int i1 = 0; i1 < sources.size(); ++i1) {
                    JsonNode eachNode = sources.get(i1);
                    handleDataSources(failures, eachNode, "GERMANY".equals(region) ? "EMEA" : region);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return failures;
    }

    private final String jdbcUrlPattern = "^#*jdbc:%s://(?<hostPort>[a-zA-Z0-9_\\-\\.:,]+)"
            + "/(?<database>[a-zA-Z0-9\\-_]+)\\??(?<jdbcParams>.+)?$";
    private final String prestoUrlPattern = "^#*jdbc:%s://(?<hostPort>[a-zA-Z0-9_\\-\\.:,]+)"
            + "/(?<catalog>[a-zA-Z0-9\\-_]+)"
            + "/(?<schema>[a-zA-Z0-9\\-_]+)\\??(?<jdbcParams>.+)?$";
    private final String pinotPattern = "^#*jdbc:%s://(?<hostPort>[a-zA-Z0-9_\\-\\.:,]+)$";
    private final String oraclePattern = "^#*jdbc:%s:thin:@(?<hostPort>[a-zA-Z0-9_\\-\\.:,]+)"
            + "/(?<serviceName>[a-zA-Z0-9\\-_\\.]+)$";

    private void handleDataSources(List<String> failures, JsonNode target, String region) throws Exception {

        String categoryName = target.get("categoryName").asText();
        JsonNode dataSources = target.get("dataSources");
        List<String> owners = new ArrayList<>();
        List<Source> targetSources = new ArrayList<>();

        for (Iterator<JsonNode> dsIter = dataSources.elements(); dsIter.hasNext();) {

            JsonNode eachSource = dsIter.next();
            String type = getValueStr(eachSource, "dataSourceType");
            String sourceName = getValueStr(eachSource, "dataSourceName");
            Source.SourceBuilder builder = Source.builder()
                    .dataSourceName(sourceName)
                    .alias(getValueStr(eachSource, "alias"))
                    .dataSourceType(type)
                    .testQuerySql(getValueStr(eachSource, "testQuerySql"));

            if (owners.isEmpty()) {
                JsonNode ownerNode = eachSource.get("owner");
                for (JsonNode jsonNode : ownerNode) {
                    String owner = jsonNode.asText().toLowerCase();
                    owner = owner.substring(0, owner.indexOf("@cisco.com"));
                    if (!owners.contains(owner)) {
                        owners.add(owner);
                    }
                }
            }

            JsonNode dataSourceConfigs = eachSource.get("dataSourceConfigs");
            boolean primary = false;
            boolean gsb = false;

            for (JsonNode eachConfig : dataSourceConfigs) {

                String cluster = getValueStr(eachConfig, "cluster");
                String url = getValueStr(eachConfig, "url");

                if ("PRIMARY".equalsIgnoreCase(cluster)) {
                    if (primary) {
                        log.warn("[" + type + "] multiple primary found, skip it!!! url -> " + url);
                        logFailure(failures, region, categoryName, sourceName, url, "multiple primary found");
                        continue;
                    }
                    primary = true;
                } else if ("GSB".equalsIgnoreCase(cluster)) {
                    if (gsb) {
                        log.warn("[" + type + "] multiple gsb found, skip it!!! url -> " + url);
                        logFailure(failures, region, categoryName, sourceName, url, "multiple gsb found");
                        continue;
                    }
                    gsb = true;
                } else {
                    log.warn("[" + type + "] invalid cluster found, skip it!!! url -> " + url);
                    logFailure(failures, region, categoryName, sourceName, url, "invalid cluster");
                    continue;
                }

                String urlPatternStr = jdbcUrlPattern;
                String jdbcType = type;
                if ("pinot".equalsIgnoreCase(type)) {
                    urlPatternStr = pinotPattern;
                } else if ("oracle".equalsIgnoreCase(type)) {
                    urlPatternStr = oraclePattern;
                } else if ("presto".equalsIgnoreCase(type)
                        || "trino".equalsIgnoreCase(type)) {
                    urlPatternStr = prestoUrlPattern;
                } else if ("tidb".equalsIgnoreCase(type)) {
                    jdbcType = "mysql";
                } else if ("postgres".equalsIgnoreCase(type)) {
                    jdbcType = "postgresql";
                }
                Pattern urlPattern = Pattern.compile(String.format(urlPatternStr, jdbcType));
                Matcher matcher = urlPattern.matcher(url);
                if (!matcher.matches()) {
                    log.error("[" + type + "] No invalid url -> " + url);
                    logFailure(failures, region, categoryName, sourceName, url, "invalid url");
                    continue;
                }

                String hostPort = matcher.group("hostPort");
                String database = null;
                String jdbcParams = null;
                String catalog = null;
                String schema = null;
                String serviceName = null;

                if ("oracle".equalsIgnoreCase(type)) {
                    serviceName = matcher.group("serviceName");
                } else if ("presto".equalsIgnoreCase(type)
                        || "trino".equalsIgnoreCase(type)) {
                    catalog = matcher.group("catalog");
                    schema = matcher.group("schema");
                } else if (!"pinot".equalsIgnoreCase(type)) {
                    database = matcher.group("database");
                    jdbcParams = matcher.group("jdbcParams");
                }

                builder.config(
                        SourceConfig.builder()
                                .cluster(cluster)
                                .url(url)
                                .username(getValueStr(eachConfig, "username"))
                                .password(getValueStr(eachConfig, "password"))
                                .driver(getValueStr(eachConfig, "driver"))
                                .type(getValueStr(eachConfig, "type"))
                                .maxSize(getIntStr(eachConfig, "maxSize"))
                                .minSize(getIntStr(eachConfig, "minSize"))
                                .idleSize(getIntStr(eachConfig, "idleSize"))
                                .status(getValueStr(eachConfig, "status"))
                                .hostPort(hostPort)
                                .database(database)
                                .jdbcParams(jdbcParams)
                                .catalog(catalog)
                                .schema(schema)
                                .serviceName(serviceName)
                                .dataCenter(resolveDataCenter(region, "PRIMARY".equalsIgnoreCase(cluster)))
                                .build()
                );
            }

            if(!builder.configs.isEmpty()) {
                targetSources.add(builder.build());
            }
        }

        if (targetSources.isEmpty()) {
            return;
        }

        categoryName = categoryName.toLowerCase();
        Urn groupUrn = Urn.createFromString("urn:li:corpGroup:" + categoryName);

        handleCategory(groupUrn, categoryName, owners);
        handleDataSource(failures, region, categoryName, targetSources);
    }

    private String resolveDataCenter(String region, boolean primary) {
        if ("AMER".equalsIgnoreCase(region)) {
            return primary ? "SJC02" : "DFW02";
        }
        if ("CANADA".equalsIgnoreCase(region)) {
            return primary ? "YUL01" : "YYZ02";
        }
        if ("EMEA".equalsIgnoreCase(region)) {
            return primary ? "FRA01" : "AMS01";
        }

        throw new RuntimeException("Cannot find datacenter for region[" + region + "]");
    }

    private void logFailure(List<String> failures,
                            String region, String group,  String name, String url, String reason) {
        String line = region + ":" + group + ":" + name + " -> " + url + " [" + reason + "]";
        failures.add(line);
    }

    private void logFailure(List<String> failures, String region, String group,  Source source, String reason) {
        for (SourceConfig config : source.getConfigs()) {
            logFailure(failures, region, group, source.getDataSourceName(), config.getUrl(), reason);
        }
    }

    private void handleDataSource(List<String> failures, String region, String group,  List<Source> targetSources) {
        if (this.datasourceService == null) {
            return;
        }
        for (Source source : targetSources) {
            log.info("Creating datasource[" + region + "," + group + ","
                    + source.getDataSourceType() + "," + source.getDataSourceName() + "]...");
            try {
                this.datasourceService.createDatasource(source, region, group);
            } catch (Exception ex) {
                logFailure(failures, region, region, source, ex.getMessage());
            }
        }
    }

    private void handleCategory(Urn groupUrn, String categoryName, List<String> owners) throws Exception {
        if (this.groupService == null) {
            return;
        }
        if (!this.groupService.groupExists(groupUrn)) {
            final CorpGroupKey key = new CorpGroupKey();
            key.setName(categoryName);
            this.groupService.createNativeGroup(key,
                    categoryName,
                    "",
                    AuthenticationContext.getAuthentication());
        }

        for (String owner : owners) {
            String ownerUrnStr = "urn:li:corpuser:" + owner;
            Urn ownerUrn = Urn.createFromString(ownerUrnStr);
            if (!this.entityService.exists(ownerUrn)) {
                log.info("Creating user[" + ownerUrn + "]...");
                this.nativeUserService.createNativeUser(
                        ownerUrnStr, "",
                        owner + "@cisco.com",
                        "", "",
                        AuthenticationContext.getAuthentication()
                );
            }
            this.groupService.addUserToNativeGroup(
                    ownerUrn, groupUrn, AuthenticationContext.getAuthentication());
        }

    }

    @Getter
    @Builder
    static class SourceConfig {
        private String cluster;
        private String url;
        private String dataCenter;
        private String hostPort;
        private String database;
        private String catalog;
        private String schema;
        private String serviceName;
        private String username;
        private String password;
        private String jdbcParams;
        private String driver;
        private String type;
        private int maxSize;
        private int minSize;
        private int idleSize;
        private String status;
    }

    @Builder @Getter
    static class Source {
        private String dataSourceName;
        private String alias;
        private String dataSourceType;
        private String testQuerySql;
        @Singular
        public final List<SourceConfig> configs;
    }

    private JsonNode getDataSources(String region) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(
                "http://10.29.42.205:8023/api/v1/datasource/datasources-for-datahub?region=" + region);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new Exception("Fail to get datasource -> " + response.getStatusLine().getReasonPhrase());
            }
            String content = EntityUtils.toString(response.getEntity(), "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(content);
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }

    private String getValueStr(JsonNode target, String fieldName) {
        return target.get(fieldName) == null ? null : target.get(fieldName).asText();
    }

    private int getIntStr(JsonNode target, String fieldName) {
        return target.get(fieldName) == null ? 0 : target.get(fieldName).asInt();
    }
}