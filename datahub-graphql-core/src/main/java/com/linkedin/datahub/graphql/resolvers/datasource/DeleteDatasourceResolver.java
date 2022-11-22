package com.linkedin.datahub.graphql.resolvers.datasource;

import com.google.common.collect.ImmutableList;
import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.authorization.AuthorizationUtils;
import com.linkedin.datahub.graphql.authorization.ConjunctivePrivilegeGroup;
import com.linkedin.datahub.graphql.authorization.DisjunctivePrivilegeGroup;
import com.linkedin.datahub.graphql.exception.AuthorizationException;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.metadata.authorization.PoliciesConfig;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class DeleteDatasourceResolver implements DataFetcher<CompletableFuture<String>>
{
    private final EntityClient datasourceClient;

    public DeleteDatasourceResolver(EntityClient datasourceClient) {
        this.datasourceClient = datasourceClient;
    }

    @Override
    public CompletableFuture<String> get(DataFetchingEnvironment environment) throws Exception {

        final QueryContext context = environment.getContext();
        final String datasourceUrn = environment.getArgument("urn");
        final Urn urn = Urn.createFromString(datasourceUrn);

        return CompletableFuture.supplyAsync(() -> {
            if (!isAuthorizedToDeleteDatasource(context, urn)) {
                throw new AuthorizationException(
                        "Unauthorized to perform this action. Please contact your DataHub administrator.");
            }
            try {
                datasourceClient.deleteEntity(urn, context.getAuthentication());
                return datasourceUrn;
            } catch (Exception ex) {
                throw new RuntimeException(
                        String.format("Failed to perform delete against datasourrce with urn %s", datasourceUrn), ex);
            }
        });
    }

    public static boolean isAuthorizedToDeleteDatasource(@Nonnull QueryContext context, Urn entityUrn) {
        final DisjunctivePrivilegeGroup orPrivilegeGroups = new DisjunctivePrivilegeGroup(ImmutableList.of(
                new ConjunctivePrivilegeGroup(ImmutableList.of(PoliciesConfig.DELETE_DATASOURCE_PRIVILEGE.getType()))
        ));

        return AuthorizationUtils.isAuthorized(context.getAuthorizer(),
                context.getActorUrn(), entityUrn.getEntityType(), entityUrn.toString(), orPrivilegeGroups);
    }
}