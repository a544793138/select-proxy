package com.mastercard.cme.caas.web.test.token;

import com.mastercard.cme.caas.web.test.util.CookieUtil;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Optional;

public class CustomProxyServlet extends ProxyServlet {

    private final GiveTokenProperties giveTokenProperties;

    public CustomProxyServlet(GiveTokenProperties giveTokenProperties) {
        this.giveTokenProperties = giveTokenProperties;
    }

    @Override
    protected HttpClient createHttpClient() {

        final ClassLoader loader = CustomProxyServlet.class.getClassLoader();
        try {
            final SSLContext context = SSLContextBuilder.create()
                    .loadKeyMaterial(loader.getResource("application-1.pfx"), "123456".toCharArray(), "123456".toCharArray())
                    .loadTrustMaterial(loader.getResource("caas-ca.jks"), "123456".toCharArray())
                    .build();

            final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", new SSLConnectionSocketFactory(context, (s, session) -> true))
                    .build());

            final RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setRedirectsEnabled(doHandleRedirects)
                    .setCookieSpec(CookieSpecs.IGNORE_COOKIES) // we handle them in the servlet instead
                    .setConnectTimeout(connectTimeout)
                    .setSocketTimeout(readTimeout)
                    .setConnectionRequestTimeout(connectionRequestTimeout)
                    .build();

            CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(defaultRequestConfig)
                    .setMaxConnTotal(maxConnections)
                    .setConnectionManager(connectionManager)
                    .setUserTokenHandler(context1 -> null)
                    .build();
            return client;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected HttpResponse doExecute(HttpServletRequest servletRequest, HttpServletResponse servletResponse, HttpRequest proxyRequest) throws IOException {
        final String myTokenId = CookieUtil.getCookieValue(servletRequest, CookieUtil.MyTokenCookieName);
        final Optional<SamlTokenPojo> first = giveTokenProperties.getSamlTokens().stream()
                .filter(samlTokenPojo -> samlTokenPojo.getId().equals(myTokenId))
                .findFirst();
        if (!first.isPresent()) {
            throw new IllegalArgumentException(String.format("Can't find token with id %s", myTokenId));
        }
        proxyRequest.setHeader(giveTokenProperties.getAuthHeader(), first.get().getToken());
        return super.doExecute(servletRequest, servletResponse, proxyRequest);
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        final String myTarget = CookieUtil.getCookieValue(servletRequest, CookieUtil.MyTargetCookieName);
        initTarget(myTarget);
        super.service(servletRequest, servletResponse);
    }

    private void initTarget(String myTarget) throws ServletException {
        targetUri = myTarget;
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

    @Override
    protected void copyProxyCookie(HttpServletRequest servletRequest, HttpServletResponse servletResponse, String headerValue) {
        //build path for resulting cookie
        String path = servletRequest.getContextPath(); // path starts with / or is empty string
        path += servletRequest.getServletPath(); // servlet path starts with / or is empty string
        if(path.isEmpty()){
            path = "/";
        }

        for (HttpCookie cookie : HttpCookie.parse(headerValue)) {
            //set cookie name prefixed w/ a proxy value so it won't collide w/ other cookies
            String proxyCookieName = doPreserveCookies ? cookie.getName() : getCookieNamePrefix(cookie.getName()) + cookie.getName();
            Cookie servletCookie = new Cookie(proxyCookieName, cookie.getValue());
            servletCookie.setComment(cookie.getComment());
            servletCookie.setMaxAge((int) cookie.getMaxAge());
            servletCookie.setPath("/"); //set to the path of the proxy servlet
            // don't set cookie domain
            servletResponse.addCookie(servletCookie);
        }
    }
}
