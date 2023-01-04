package com.datahub.cisco.wap;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.util.Base64Utils;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class CITokenServiceImpl implements CITokenService
{
    private final Map<CITokenInfo, TokenHolder> tokenHolders = new HashMap<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public String getAccessToken(CITokenInfo ciTokenInfo) {

        readWriteLock.readLock().lock();
        try {
            String token = getCacheToken(ciTokenInfo);
            if(token != null) {
                return token;
            }
        } finally {
            readWriteLock.readLock().unlock();
        }

        readWriteLock.writeLock().lock();
        try {
            String token = getCacheToken(ciTokenInfo);
            if(token != null) {
                return token;
            }
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                TokenHolder tokenHolder = generateToken(httpclient, ciTokenInfo);
                tokenHolders.put(ciTokenInfo, tokenHolder);
                return tokenHolder.token;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private String getCacheToken(CITokenInfo ciTokenInfo) {
        TokenHolder tokenHolder = tokenHolders.get(ciTokenInfo);
        return (tokenHolder != null && !tokenHolder.timeoutChecker.isTimeout()) ? tokenHolder.token : null;
    }

    private TokenHolder generateToken(CloseableHttpClient httpclient, CITokenInfo ciTokenInfo) throws Exception {

        String bearerToken = this.getBearerToken(httpclient, ciTokenInfo);
        Validate.notNull(bearerToken, "the 'bearerToken' is null");

        String CI_AUTH = "Basic " + new String(Base64Utils.encode(
                (ciTokenInfo.getClientId().concat(":").concat(ciTokenInfo.getClientSecret())).getBytes()));

        HttpPost httpPost = new HttpPost(ciTokenInfo.getAccessTokenUrl());
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.addHeader("Authorization", CI_AUTH);

        List<NameValuePair> form = new ArrayList<>();
        form.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:saml2-bearer"));
        form.add(new BasicNameValuePair("scope", ciTokenInfo.getScope()));
        form.add(new BasicNameValuePair("assertion", bearerToken));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, UTF_8);
        httpPost.setEntity(entity);

        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new Exception("Fail to get access token -> " + response.getStatusLine().getReasonPhrase());
            }
            String content = EntityUtils.toString(response.getEntity(), UTF_8);
            JSONObject tokenJson = new JSONObject(content);
            String token = tokenJson.getString("access_token");
            Validate.notNull(token, "the 'token' is null");
            int expiresIn = tokenJson.getInt("expires_in") - 120;
            return TokenHolder.builder().token(token).timeoutChecker(new TimeoutChecker(expiresIn)).build();
        }
    }

    private String getBearerToken(CloseableHttpClient httpclient, CITokenInfo ciTokenInfo) throws Exception {
        String bearerTokenUrl = ciTokenInfo.getBearerTokenUrl().replace("{orgId}", ciTokenInfo.getOrgId());
        JSONObject postParams = new JSONObject();
        postParams.put("name", ciTokenInfo.getMachineAccountName());
        postParams.put("password", ciTokenInfo.getMachineAccountPass());

        HttpPost httpPost = new HttpPost(bearerTokenUrl);
        httpPost.addHeader("Content-Type", "application/json");
        StringEntity se = new StringEntity(postParams.toString(), UTF_8);
        httpPost.setEntity(se);

        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new Exception("Fail to get bearer token -> " + response.getStatusLine().getReasonPhrase());
            }
            String content = EntityUtils.toString(response.getEntity(), UTF_8);
            JSONObject tokenJson = new JSONObject(content);
            return tokenJson.getString("BearerToken");
        }
    }

    @Builder
    static class TokenHolder {
        private String token;
        private TimeoutChecker timeoutChecker;
    }

    static class TimeoutChecker
    {
        private Date startTime;
        private int timeoutSeconds;

        public boolean isTimeout() {
            return new Date().getTime() - startTime.getTime() >= this.timeoutSeconds * 1000L;
        }

        public TimeoutChecker(int timeoutSeconds) {
            setTimeoutSeconds(timeoutSeconds);
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            this.startTime = new Date();
        }
    }
}