package com.tjwoods.token;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "select-proxy.token")
public class GiveTokenProperties {

    private String authHeader;

    private Map<String, String> userIdWithTokenPath = new HashMap<>();

    public List<TokenPojo> getSamlTokens() {
        List<TokenPojo> result = new ArrayList<>();
        for (String userId : userIdWithTokenPath.keySet()) {
            final String tokenPath = userIdWithTokenPath.get(userId);

            final File tokenFile;
            try {
                tokenFile = ResourceUtils.getFile(tokenPath);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(String.format("Can't find the SAML TOKEN with path %s", tokenPath), e);
            }

            try (final FileInputStream fileInputStream = new FileInputStream(tokenFile)) {
                byte[] buffer = new byte[fileInputStream.available()];
                fileInputStream.read(buffer);
                final String token = new String(buffer);
                result.add(new TokenPojo(userId, token));
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot find token", e);
            }
        }
        return result;
    }

    public Map<String, String> getUserIdWithTokenPath() {
        return userIdWithTokenPath;
    }

    public void setUserIdWithTokenPath(Map<String, String> userIdWithTokenPath) {
        this.userIdWithTokenPath = userIdWithTokenPath;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }
}
