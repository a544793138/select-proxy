package com.mastercard.cme.caas.web.test.config;

import com.mastercard.cme.caas.web.test.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String requestURI = request.getRequestURI();
        final String myTokenId = CookieUtil.getCookieValue(request, CookieUtil.MyTokenCookieName);
        if (StringUtils.isNotBlank(myTokenId) && !requestURI.startsWith("/proxy")) {
            request.getRequestDispatcher("/proxy" + requestURI).forward(request, response);
        }
        return true;
    }
}
