package com.datahub.cisco.wap;

import lombok.*;

@Builder
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CITokenInfo
{
    private String scope;
    private String orgId;
    private String bearerTokenUrl;
    private String accessTokenUrl;
    private String machineAccountName;
    private String machineAccountPass;
    private String clientId;
    private String clientSecret;
}
