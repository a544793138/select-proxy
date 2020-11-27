package com.tjwoods.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "select-proxy.proxy")
public class ProxyProperties {

    private static final String servletUrl = "/proxy/*";

    private String targetUrl;

    private static final boolean logEnabled = true;

    public String getServletUrl() {
        return servletUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

}
