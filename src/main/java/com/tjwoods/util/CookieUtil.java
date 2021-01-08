package com.mastercard.cme.caas.web.test.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class CookieUtil {

    public static final String MyTokenCookieName = "MyTokenCookieName";

    public static final String MyTargetCookieName = "MyTargetCookieName";

    public static String getCookieValue(HttpServletRequest request, String name) {
        if (Objects.isNull(request.getCookies())) {
            return null;
        }
        final Optional<Cookie> first = Arrays.stream(request.getCookies())
                .filter(cookie -> name.equalsIgnoreCase(cookie.getName()))
                .findFirst();
        return first.map(Cookie::getValue).orElse(null);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        final String value = getCookieValue(request, name);
        if (StringUtils.isNotBlank(value)) {
            setCookie(response, name, null, 0);
        }
    }

    public static void setCookie(HttpServletResponse response, String name, String value, Integer maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        if (maxAge != null) {
            cookie.setMaxAge(maxAge);
        }
        response.addCookie(cookie);
    }
}
