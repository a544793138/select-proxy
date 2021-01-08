package com.mastercard.cme.caas.web.test.config;

public class ProxyProperties {

    private static final String servletUrl = "/proxy/*";

    private static final boolean logEnabled = true;

    public String getServletUrl() {
        return servletUrl;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

}
