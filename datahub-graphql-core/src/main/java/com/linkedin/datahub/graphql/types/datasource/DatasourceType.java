package com.linkedin.datahub.graphql.types.datasource;

import com.datahub.authentication.group.GroupService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.linkedin.common.urn.CorpuserUrn;
import com.linkedin.common.urn.DatasourceUrn;
import com.linkedin.common.urn.Urn;
import com.linkedin.common.urn.UrnUtils;
import com.linkedin.data.template.StringArray;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.authorization.AuthorizationUtils;
import com.linkedin.datahub.graphql.authorization.ConjunctivePrivilegeGroup;
import com.linkedin.datahub.graphql.authorization.DisjunctivePrivilegeGroup;
import com.linkedin.datahub.graphql.exception.AuthorizationException;
import com.linkedin.datahub.graphql.generated.*;
import com.linkedin.datahub.graphql.resolvers.ResolverUtils;
import com.linkedin.datahub.graphql.types.BrowsableEntityType;
import com.linkedin.datahub.graphql.types.MutableType;
import com.linkedin.datahub.graphql.types.SearchableEntityType;
import com.linkedin.datahub.graphql.types.datasource.mappers.DatasourceMapper;
import com.linkedin.datahub.graphql.types.datasource.mappers.DatasourceUpdateInputSnapshotMapper;
import com.linkedin.datahub.graphql.types.mappers.AutoCompleteResultsMapper;
import com.linkedin.datahub.graphql.types.mappers.BrowsePathsMapper;
import com.linkedin.datahub.graphql.types.mappers.BrowseResultMapper;
import com.linkedin.datahub.graphql.types.mappers.UrnSearchResultsMapper;
import com.linkedin.entity.Entity;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.identity.NativeGroupMembership;
import com.linkedin.metadata.authorization.PoliciesConfig;
import com.linkedin.metadata.browse.BrowseResult;
import com.linkedin.metadata.query.AutoCompleteResult;
import com.linkedin.metadata.search.SearchResult;
import com.linkedin.metadata.snapshot.DatasourceSnapshot;
import com.linkedin.metadata.snapshot.Snapshot;
import com.linkedin.r2.RemoteInvocationException;
import graphql.execution.DataFetcherResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.linkedin.datahub.graphql.Constants.BROWSE_PATH_DELIMITER;
import static com.linkedin.metadata.Constants.*;

public class DatasourceType implements SearchableEntityType<Datasource, String>,
        BrowsableEntityType<Datasource, String>,
        MutableType<DatasourceUpdateInput, Datasource> {

    private static final Set<String> ASPECTS_TO_RESOLVE = ImmutableSet.of(
            DATASOURCE_KEY_ASPECT_NAME,
            "datasourceProperties",
            "editableDatasourceProperties",
            "datasourceDeprecation",
            INSTITUTIONAL_MEMORY_ASPECT_NAME,
            OWNERSHIP_ASPECT_NAME,
            STATUS_ASPECT_NAME,
            DOMAINS_ASPECT_NAME,
            DATASOURCE_INFO_ASPECT_NAME,
            GLOBAL_TAGS_ASPECT_NAME,
            GLOSSARY_TERMS_ASPECT_NAME,
            "datasourceConnectionPrimary",
            "datasourceConnectionGSB"
    );

    private static final Set<String> FACET_FIELDS = ImmutableSet.of("origin", "platform");
    private static final String ENTITY_NAME = "datasource";

    private final EntityClient _datasourcesClient;
    private final GroupService _groupService;

    public DatasourceType(final EntityClient datasourcesClient, final GroupService groupService) {
        _datasourcesClient = datasourcesClient;
        _groupService = groupService;
    }

    @Override
    public Class<Datasource> objectClass() {
        return Datasource.class;
    }

    @Override
    public Class<DatasourceUpdateInput> inputClass() {
        return DatasourceUpdateInput.class;
    }

    @Override
    public EntityType type() {
        return EntityType.DATASOURCE;
    }

    @Override
    public Function<com.linkedin.datahub.graphql.generated.Entity, String> getKeyProvider() {
        return com.linkedin.datahub.graphql.generated.Entity::getUrn;
    }

    @Override
    public List<DataFetcherResult<Datasource>> batchLoad(@Nonnull final List<String> urnStrs,
        @Nonnull final QueryContext context) {

        try {
            NativeGroupMembership groupMembership = nativeGroupMembershipFor(context);

            final List<Urn> urns = urnStrs.stream()
                    .map(UrnUtils::getUrn)
                    .collect(Collectors.toList());

            final Map<Urn, EntityResponse> datasetMap =
                    _datasourcesClient.batchGetV2(
                            DATASOURCE_ENTITY_NAME,
                            new HashSet<>(urns),
                            ASPECTS_TO_RESOLVE,
                            context.getAuthentication());

            final List<EntityResponse> gmsResults = new ArrayList<>();
            for (Urn urn : urns) {
                gmsResults.add(datasetMap.getOrDefault(urn, null));
            }
            return gmsResults.stream()
                    .map(gmsDataset -> gmsDataset == null ? null : DataFetcherResult.<Datasource>newResult()
                            .data(DatasourceMapper.map(gmsDataset, context, groupMembership))
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to batch load Datasources", e);
        }

    }

    @Override
    public SearchResults search(@Nonnull String query,
                                @Nullable List<FacetFilterInput> filters,
                                int start,
                                int count,
                                @Nonnull final QueryContext context) throws Exception {
        final Map<String, String> facetFilters = ResolverUtils.buildFacetFilters(filters, FACET_FIELDS);
        final SearchResult searchResult = _datasourcesClient.search(ENTITY_NAME, query, facetFilters, start, count, context.getAuthentication());
        return UrnSearchResultsMapper.map(searchResult);
    }

    @Override
    public AutoCompleteResults autoComplete(@Nonnull String query,
                                            @Nullable String field,
                                            @Nullable List<FacetFilterInput> filters,
                                            int limit,
                                            @Nonnull final QueryContext context) throws Exception {
        final Map<String, String> facetFilters = ResolverUtils.buildFacetFilters(filters, FACET_FIELDS);
        final AutoCompleteResult result = _datasourcesClient.autoComplete(ENTITY_NAME, query, facetFilters, limit, context.getAuthentication());
        return AutoCompleteResultsMapper.map(result);
    }

    @Override
    public BrowseResults browse(@Nonnull List<String> path,
                                @Nullable List<FacetFilterInput> filters,
                                int start,
                                int count,
                                @Nonnull final QueryContext context) throws Exception {
        final Map<String, String> facetFilters = ResolverUtils.buildFacetFilters(filters, FACET_FIELDS);
        final String pathStr = path.size() > 0 ? BROWSE_PATH_DELIMITER + String.join(BROWSE_PATH_DELIMITER, path) : "";
        final BrowseResult result = _datasourcesClient.browse(
                ENTITY_NAME,
                pathStr,
                facetFilters,
                start,
                count,
                context.getAuthentication());
        return BrowseResultMapper.map(result);
    }

    @Override
    public List<BrowsePath> browsePaths(@Nonnull String urn, @Nonnull final QueryContext context) throws Exception {
        final StringArray result = _datasourcesClient.getBrowsePaths(DatasourceUtils.getDatasourceUrn(urn), context.getAuthentication());
        return BrowsePathsMapper.map(result);
    }

    @Override
    public Datasource update(@Nonnull String urn, @Nonnull DatasourceUpdateInput input, @Nonnull QueryContext context) throws Exception {
        if (isAuthorized(urn, input, context)) {
            final CorpuserUrn actor = CorpuserUrn.createFromString(context.getAuthentication().getActor().toUrnStr());
            final DatasourceSnapshot datasourceSnapshot = DatasourceUpdateInputSnapshotMapper.map(input, actor);
            datasourceSnapshot.setUrn(DatasourceUrn.createFromString(urn));
            final Snapshot snapshot = Snapshot.create(datasourceSnapshot);

            try {
                Entity entity = new Entity();
                entity.setValue(snapshot);
                _datasourcesClient.update(entity, context.getAuthentication());
            } catch (RemoteInvocationException e) {
                throw new RuntimeException(String.format("Failed to write entity with urn %s", urn), e);
            }

            return load(urn, context).getData();
        }
        throw new AuthorizationException("Unauthorized to perform this action. Please contact your DataHub administrator.");
    }

    private boolean isAuthorized(@Nonnull String urn, @Nonnull DatasourceUpdateInput update, @Nonnull QueryContext context) {
        // Decide whether the current principal should be allowed to update the Dataset.
        final DisjunctivePrivilegeGroup orPrivilegeGroups = getAuthorizedPrivileges(update);
        return AuthorizationUtils.isAuthorized(
                context.getAuthorizer(),
                context.getAuthentication().getActor().toUrnStr(),
                PoliciesConfig.DATASOURCE_PRIVILEGES.getResourceType(),
                urn,
                orPrivilegeGroups);
    }

    private DisjunctivePrivilegeGroup getAuthorizedPrivileges(final DatasourceUpdateInput updateInput) {

        final ConjunctivePrivilegeGroup allPrivilegesGroup = new ConjunctivePrivilegeGroup(ImmutableList.of(
                PoliciesConfig.EDIT_DATASOURCE_PRIVILEGE.getType()
        ));

        List<String> specificPrivileges = new ArrayList<>();
        if (updateInput.getInstitutionalMemory() != null) {
            specificPrivileges.add(PoliciesConfig.EDIT_ENTITY_DOC_LINKS_PRIVILEGE.getType());
        }
        if (updateInput.getOwnership() != null) {
            specificPrivileges.add(PoliciesConfig.EDIT_ENTITY_OWNERS_PRIVILEGE.getType());
        }
        if (updateInput.getDeprecation() != null) {
            specificPrivileges.add(PoliciesConfig.EDIT_ENTITY_STATUS_PRIVILEGE.getType());
        }
        if (updateInput.getEditableProperties() != null) {
            specificPrivileges.add(PoliciesConfig.EDIT_ENTITY_DOCS_PRIVILEGE.getType());
        }
        if (updateInput.getGlobalTags() != null) {
            specificPrivileges.add(PoliciesConfig.EDIT_ENTITY_TAGS_PRIVILEGE.getType());
        }

        final ConjunctivePrivilegeGroup specificPrivilegeGroup = new ConjunctivePrivilegeGroup(specificPrivileges);

        // If you either have all entity privileges, or have the specific privileges required, you are authorized.
        return new DisjunctivePrivilegeGroup(ImmutableList.of(
                allPrivilegesGroup,
                specificPrivilegeGroup
        ));
    }

    private NativeGroupMembership nativeGroupMembershipFor(@Nonnull QueryContext context) throws Exception {
        final CorpuserUrn actor = CorpuserUrn.createFromString(context.getAuthentication().getActor().toUrnStr());
        return _groupService.getExistingNativeGroupMembership(actor, context.getAuthentication());
    }

}