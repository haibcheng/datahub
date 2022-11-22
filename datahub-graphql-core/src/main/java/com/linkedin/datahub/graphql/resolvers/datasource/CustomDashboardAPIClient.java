package com.linkedin.datahub.graphql.resolvers.datasource;


import com.linkedin.util.Configuration;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class CustomDashboardAPIClient {

    private CustomDashboardAPIClient() {
    }

    private static final String PREFIX = "/api/v2/";

    public static boolean testConnection(String body, String bearerToken) {
        String url = Configuration.getEnvironmentVariable("CUSTOM_DASHBOARD_API_URL")
                + PREFIX + "datasource/test-connection";
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Authorization", bearerToken);
        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));


        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build();
             CloseableHttpResponse resp = httpClient.execute(post)
        ) {
            if (resp.getStatusLine().getStatusCode() == 200) {
                String resStr = EntityUtils.toString(resp.getEntity());
                return Boolean.parseBoolean(resStr);
            } else {
                throw new IllegalStateException("create datasource failed." + resp.getStatusLine().getReasonPhrase());
            }
        }  catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

}
