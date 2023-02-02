package com.linkedin.datahub.graphql.types.datasource.mappers;

import com.linkedin.common.GlobalTags;
import com.linkedin.common.GlossaryTerms;
import com.linkedin.common.InstitutionalMemory;
import com.linkedin.common.Ownership;
import com.linkedin.common.Status;
import com.linkedin.common.urn.Urn;
import com.linkedin.data.DataMap;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.FabricType;
import com.linkedin.datahub.graphql.generated.*;
import com.linkedin.datahub.graphql.types.common.mappers.InstitutionalMemoryMapper;
import com.linkedin.datahub.graphql.types.common.mappers.OwnershipMapper;
import com.linkedin.datahub.graphql.types.common.mappers.StatusMapper;
import com.linkedin.datahub.graphql.types.common.mappers.StringMapMapper;
import com.linkedin.datahub.graphql.types.common.mappers.util.MappingHelper;
import com.linkedin.datahub.graphql.types.datasource.DatasourceUtils;
import com.linkedin.datahub.graphql.types.domain.DomainAssociationMapper;
import com.linkedin.datahub.graphql.types.glossary.mappers.GlossaryTermsMapper;
import com.linkedin.datahub.graphql.types.mappers.ModelMapper;
import com.linkedin.datahub.graphql.types.tag.mappers.GlobalTagsMapper;
import com.linkedin.dataset.EditableDatasetProperties;
import com.linkedin.datasource.DatasourceConnectionGSB;
import com.linkedin.datasource.DatasourceConnectionPrimary;
import com.linkedin.datasource.DatasourceDeprecation;
import com.linkedin.datasource.DatasourceInfo;
import com.linkedin.domain.Domains;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.EnvelopedAspectMap;
import com.linkedin.identity.NativeGroupMembership;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.stream.Collectors;

import static com.linkedin.metadata.Constants.*;

@Slf4j
public class DatasourceMapper implements ModelMapper<EntityResponse, Datasource>
{
    private final QueryContext context;
    private final NativeGroupMembership groupMembership;

    public static Datasource map(@Nonnull final EntityResponse datasource,
                                 QueryContext context, NativeGroupMembership groupMembership) {
        return new DatasourceMapper(context, groupMembership).apply(datasource);
    }

    public Datasource apply(@Nonnull final EntityResponse entityResponse) {

        Datasource result = new Datasource();
        Urn entityUrn = entityResponse.getUrn();
        result.setUrn(entityResponse.getUrn().toString());
        result.setType(EntityType.DATASOURCE);

        EnvelopedAspectMap aspectMap = entityResponse.getAspects();

        MappingHelper<Datasource> mappingHelper = new MappingHelper<>(aspectMap, result);
        mappingHelper.mapToResult(DATASOURCE_KEY_ASPECT_NAME, this::mapDatasourceKey);

        mappingHelper.mapToResult("datasourceProperties", this::mapDatasourceProperties);
        mappingHelper.mapToResult(DATASOURCE_INFO_ASPECT_NAME, this::mapDatasourceInfo);

        mappingHelper.mapToResult("editableDatasourceProperties",
                this::mapEditableDatasourceProperties);

        mappingHelper.mapToResult(INSTITUTIONAL_MEMORY_ASPECT_NAME, (datasource, dataMap) ->
                datasource.setInstitutionalMemory(
                        InstitutionalMemoryMapper.map(new InstitutionalMemory(dataMap))));
        mappingHelper.mapToResult(OWNERSHIP_ASPECT_NAME, (datasource, dataMap) ->
                datasource.setOwnership(OwnershipMapper.map(new Ownership(dataMap), entityUrn)));
        mappingHelper.mapToResult(STATUS_ASPECT_NAME, (datasource, dataMap) ->
                datasource.setStatus(StatusMapper.map(new Status(dataMap))));
        mappingHelper.mapToResult(GLOBAL_TAGS_ASPECT_NAME, (datasource, dataMap) ->
                this.mapGlobalTags(datasource, dataMap, entityUrn));
        mappingHelper.mapToResult(GLOSSARY_TERMS_ASPECT_NAME, (datasource, dataMap) ->
                datasource.setGlossaryTerms(
                        GlossaryTermsMapper.map(new GlossaryTerms(dataMap), entityUrn)));
        mappingHelper.mapToResult(DOMAINS_ASPECT_NAME, this::mapDomains);
        mappingHelper.mapToResult("datasourceDeprecation", (datasource, dataMap) ->
                datasource.setDeprecation(
                        DatasourceDeprecationMapper.map(new DatasourceDeprecation(dataMap))));

        ownedDatasource(result);
        mappingHelper.mapToResult("datasourceConnectionPrimary", this::mapConnectionPrimary);
        mappingHelper.mapToResult("datasourceConnectionGSB", this::mapConnectionGSB);

        return mappingHelper.getResult();
    }

    public DatasourceMapper(QueryContext context, NativeGroupMembership groupMembership) {
        this.context = context;
        this.groupMembership = groupMembership;
    }

    private String ownerUrnStrOf(Owner owner) {
        if (owner.getOwner() instanceof CorpUser) {
            return ((CorpUser) owner.getOwner()).getUrn();
        }
        else {
            return ((CorpGroup) owner.getOwner()).getUrn();
        }
    }

    private void ownedDatasource(@Nonnull Datasource datasource) {
        try {
            List<String> ownerUrns = datasource.getOwnership().getOwners().stream()
                    .map(this::ownerUrnStrOf).collect(Collectors.toList());
            String actor = context.getAuthentication().getActor().toUrnStr();
            List<String> actorGroups = groupMembership.getNativeGroups().stream()
                    .map(Urn::toString).collect(Collectors.toList());
            boolean isOwner = ownerUrns.contains(actor) || ownerUrns.stream().anyMatch(actorGroups::contains);
            datasource.setOwned(isOwner);
        } catch (Exception ex) {
            datasource.setOwned(false);
        }
    }

    private void mapConnectionPrimary(@Nonnull Datasource datasource, @Nonnull DataMap dataMap) {
        DatasourceConnectionPrimary pri = new DatasourceConnectionPrimary(dataMap);
        DatasourceConnection dataConn = DatasourceConnectionPrimaryMapper.map(pri, datasource.getOwned());
        datasource.setPrimaryConn(dataConn);
    }

    private void mapConnectionGSB(@Nonnull Datasource datasource, @Nonnull DataMap dataMap) {
        DatasourceConnectionGSB gsb = new DatasourceConnectionGSB(dataMap);
        DatasourceConnection dataConn = DatasourceConnectionGSBMapper.map(gsb, datasource.getOwned());
        datasource.setGsbConn(dataConn);
    }

    private void mapDatasourceKey(@Nonnull Datasource datasource, @Nonnull DataMap dataMap) {
        com.linkedin.common.urn.DatasourceUrn urn = DatasourceUtils
                .getDatasourceUrn(datasource.getUrn());
        datasource.setName(urn.getDatasourceNameEntity());
        datasource.setRegion(urn.getRegionEntity());
        datasource.setOrigin(FabricType.PROD);
        DataPlatform partialPlatform = new DataPlatform();
        partialPlatform.setUrn(urn.getPlatformEntity().toString());
        datasource.setPlatform(partialPlatform);
    }

    private void mapDatasourceInfo(@Nonnull Datasource datasource, @Nonnull DataMap dataMap) {
        DatasourceInfo datasourceInfo = new DatasourceInfo(dataMap);
        CorpGroup group = new CorpGroup();
        group.setUrn(datasourceInfo.getGroup().toString());
        group.setName(datasourceInfo.getGroup().getGroupNameEntity());
        group.setType(EntityType.CORP_GROUP);
        datasource.setGroup(group);
        datasource.setRegion(datasourceInfo.getRegion());
        datasource.setAlias(datasourceInfo.getAlias());
        datasource.setTestQuerySql(datasourceInfo.getTestQuerySql());
    }

    private void mapDatasourceProperties(@Nonnull Datasource datasource,
                                         @Nonnull DataMap dataMap) {

        final com.linkedin.datasource.DatasourceProperties datasourceProperties =
                new com.linkedin.datasource.DatasourceProperties(dataMap);
        com.linkedin.datahub.graphql.generated.DatasourceProperties dsProperties =
                new com.linkedin.datahub.graphql.generated.DatasourceProperties();

        dsProperties.setDescription(datasourceProperties.getDescription());
        dsProperties.setCustomProperties(StringMapMapper.map(datasourceProperties.getCustomProperties()));
        dsProperties.setExternalUrl(datasourceProperties.getExternalUrl().toString());
        datasource.setProperties(dsProperties);
        if (datasourceProperties.getUri() != null) {
            datasource.setUri(datasourceProperties.getUri().toString());
        }
        if (datasourceProperties.getDescription() != null) {
            datasource.setDescription(datasourceProperties.getDescription());
        }
        if (datasourceProperties.getExternalUrl() != null) {
            datasource.setExternalUrl(datasourceProperties.getExternalUrl().toString());
        }
    }

    private void mapEditableDatasourceProperties(@Nonnull Datasource datasource,
                                                 @Nonnull DataMap dataMap) {
        final EditableDatasetProperties editableDatasetProperties = new EditableDatasetProperties(dataMap);
        final DatasourceEditableProperties editableProperties = new DatasourceEditableProperties();
        editableProperties.setDescription(editableDatasetProperties.getDescription());
        datasource.setEditableProperties(editableProperties);
    }

    private void mapGlobalTags(@Nonnull Datasource datasource,
                               @Nonnull DataMap dataMap, @Nonnull final Urn entityUrn) {
        com.linkedin.datahub.graphql.generated.GlobalTags globalTags =
                GlobalTagsMapper.map(new GlobalTags(dataMap), entityUrn);
        datasource.setGlobalTags(globalTags);
        datasource.setTags(globalTags);
    }

    private void mapDomains(@Nonnull Datasource datasource, @Nonnull DataMap dataMap) {
        final Domains domains = new Domains(dataMap);
        datasource.setDomain(DomainAssociationMapper.map(domains, datasource.getUrn()));
    }
}
