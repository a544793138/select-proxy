package com.tjwoods.token;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.mitre.dsmiley.httpproxy.ProxyServlet;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomProxyServlet extends ProxyServlet {

    private final GiveTokenProperties giveTokenProperties;

    public static String currentToken;

    public CustomProxyServlet(GiveTokenProperties giveTokenProperties) {
        this.giveTokenProperties = giveTokenProperties;
    }

    @Override
    protected HttpClient createHttpClient() {

        final ClassLoader loader = CustomProxyServlet.class.getClassLoader();
        try {
            final SSLContext context = SSLContextBuilder.create()
                    .loadKeyMaterial(loader.getResource("client.pfx"), "123456".toCharArray(), "123456".toCharArray())
                    .loadTrustMaterial(loader.getResource("ca.jks"), "123456".toCharArray())
                    .build();

            final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", new SSLConnectionSocketFactory(context, (s, session) -> true))
                    .build());
            CloseableHttpClient client = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setUserTokenHandler((context1) -> null)
                    .build();
            return client;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected HttpResponse doExecute(HttpServletRequest servletRequest, HttpServletResponse servletResponse, HttpRequest proxyRequest) throws IOException {
        proxyRequest.setHeader(giveTokenProperties.getAuthHeader(), currentToken);
        return super.doExecute(servletRequest, servletResponse, proxyRequest);
    }
}
