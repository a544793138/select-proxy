package com.tjwoods.config;

import com.tjwoods.token.CustomProxyServlet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String requestURI = request.getRequestURI();
        if (StringUtils.isNotBlank(CustomProxyServlet.currentToken) && !requestURI.startsWith("/proxy")) {
            request.getRequestDispatcher("/proxy" + requestURI).forward(request, response);
            return false;
        }
        return true;
    }
}
