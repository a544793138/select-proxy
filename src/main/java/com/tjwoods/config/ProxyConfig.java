package com.mastercard.cme.caas.web.test.config;

import com.mastercard.cme.caas.web.test.token.CustomProxyServlet;
import com.mastercard.cme.caas.web.test.token.GiveTokenProperties;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({GiveTokenProperties.class})
public class ProxyConfig {

    private final ProxyProperties proxyProperties;
    private final GiveTokenProperties giveTokenProperties;

    @Autowired
    public ProxyConfig(GiveTokenProperties giveTokenProperties) {
        this.proxyProperties = new ProxyProperties();
        this.giveTokenProperties = giveTokenProperties;
    }

    @Bean
    public CustomProxyServlet customProxyServlet() {
        return new CustomProxyServlet(giveTokenProperties);
    }

    @Bean
    public ServletRegistrationBean proxyServlet(CustomProxyServlet customProxyServlet) {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(customProxyServlet, proxyProperties.getServletUrl());
        servletRegistrationBean.addInitParameter(ProxyServlet.P_PRESERVECOOKIES, "true");
        servletRegistrationBean.addInitParameter(ProxyServlet.P_PRESERVEHOST, "true");
        servletRegistrationBean.addInitParameter(ProxyServlet.P_LOG, String.valueOf(proxyProperties.isLogEnabled()));
        return servletRegistrationBean;
    }
}
