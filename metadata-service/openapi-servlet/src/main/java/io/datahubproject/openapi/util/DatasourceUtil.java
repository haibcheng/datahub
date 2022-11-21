package io.datahubproject.openapi.util;

import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.util.Collection;

public class DatasourceUtil
{
    public static void checkUrns(String... urns) {
        for(String urn : urns) {
            urn = URLDecoder.decode(urn);
            urn = StringUtils.trim(urn).toLowerCase();
            if(urn.startsWith("urn:li:datasource:")) {
                throw new RuntimeException(String.format("Unauthorized to perform with urn: %s", urn));
            }
        }
    }

    public static void checkUrns(Collection<String> urns) {
        checkUrns(urns.toArray(new String[0]));
    }
}
