package com.tjwoods.config;

import com.tjwoods.token.CustomProxyServlet;
import com.tjwoods.token.GiveTokenProperties;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ProxyProperties.class, GiveTokenProperties.class})
public class ProxyConfig {

    private final ProxyProperties proxyProperties;
    private final GiveTokenProperties giveTokenProperties;

    @Autowired
    public ProxyConfig(ProxyProperties proxyProperties, GiveTokenProperties giveTokenProperties) {
        this.proxyProperties = proxyProperties;
        this.giveTokenProperties = giveTokenProperties;
    }

    @Bean
    public CustomProxyServlet customProxyServlet() {
        return new CustomProxyServlet(giveTokenProperties);
    }

    @Bean
    public ServletRegistrationBean proxyServlet(CustomProxyServlet customProxyServlet) {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(customProxyServlet, proxyProperties.getServletUrl());
        servletRegistrationBean.addInitParameter("targetUri", proxyProperties.getTargetUrl());
        // 用于保持 cookie 不变
        servletRegistrationBean.addInitParameter(ProxyServlet.P_PRESERVECOOKIES, "true");
        servletRegistrationBean.addInitParameter(ProxyServlet.P_LOG, String.valueOf(proxyProperties.isLogEnabled()));
        return servletRegistrationBean;
    }
}
