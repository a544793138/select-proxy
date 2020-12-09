package com.tjwoods.token;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIUtils;
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CustomProxyServlet extends ProxyServlet {

    private final GiveTokenProperties giveTokenProperties;

    // 均以 sessionId 作为 key
    private static final ConcurrentMap<String, String> currentTokens = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, String> targetUrls = new ConcurrentHashMap<>();

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
        proxyRequest.setHeader(giveTokenProperties.getAuthHeader(), currentTokens.get(servletRequest.getSession().getId()));
        return super.doExecute(servletRequest, servletResponse, proxyRequest);
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        initTarget(servletRequest.getSession().getId());
        super.service(servletRequest, servletResponse);
    }

    private void initTarget(String id) throws ServletException {
        targetUri = targetUrls.get(id);
        if (targetUri == null) {
            throw new ServletException(P_TARGET_URI + " is required.");
        }
        try {
            targetUriObj = new URI(targetUri);
        } catch (Exception e) {
            throw new ServletException("Trying to process targetUri init parameter: " + e, e);
        }
        targetHost = URIUtils.extractHost(targetUriObj);
    }

    @Override
    protected void initTarget() {
        // 必须重写 initTarget，用于废弃这个方法，改为使用使用 initTarget(String id) 获取正确的 targetUrl
    }

    public static ConcurrentMap<String, String> getCurrentTokens() {
        return currentTokens;
    }

    public static ConcurrentMap<String, String> getTargetUrls() {
        return targetUrls;
    }

    public static void putCurrentTokens(String sessionId, String token) {
        currentTokens.put(sessionId, token);
    }

    public static void putTargetUrls(String sessionId, String targetUrl) {
        targetUrls.put(sessionId, targetUrl);
    }

    public static void removeCurrentTokens(String sessionId) {
        currentTokens.remove(sessionId);
    }

    public static void removeTargetUrls(String sessionId) {
        targetUrls.remove(sessionId);
    }
}
