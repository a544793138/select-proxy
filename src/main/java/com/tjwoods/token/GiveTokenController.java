package com.mastercard.cme.caas.web.test.token;

import com.mastercard.cme.caas.web.test.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

@Controller
public class GiveTokenController {

    private final GiveTokenProperties giveTokenProperties;

    @Autowired
    public GiveTokenController(GiveTokenProperties giveTokenProperties) {
        this.giveTokenProperties = giveTokenProperties;
    }

    @GetMapping("/")
    public String index(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, CookieUtil.MyTokenCookieName);
        CookieUtil.deleteCookie(request, response, CookieUtil.MyTargetCookieName);
        params.put("tokens", giveTokenProperties.getSamlTokens());
        return "index";
    }

    @PostMapping("/choose-user/{userId}")
    @ResponseBody
    public void chooseUser(@PathVariable("userId") String userId, @RequestBody TargetUrl targetUrl, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        final Optional<SamlTokenPojo> first = giveTokenProperties.getSamlTokens().stream().filter(samlTokenPojo -> userId.equals(samlTokenPojo.getId())).findFirst();
        if (first.isPresent()) {
            CookieUtil.setCookie(response, CookieUtil.MyTokenCookieName, first.get().getId(), null);
            CookieUtil.setCookie(response, CookieUtil.MyTargetCookieName, targetUrl.getTargetUrl(), null);
        } else {
            throw new ServletException("Invalid userId.");
        }
    }

    @GetMapping("/pkmslogout")
    public String logOut(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, CookieUtil.MyTokenCookieName);
        CookieUtil.deleteCookie(request, response, CookieUtil.MyTargetCookieName);
        params.put("tokens", giveTokenProperties.getSamlTokens());
        return "redirect:/";
    }

    static class TargetUrl {
        private String targetUrl;

        public String getTargetUrl() {
            return targetUrl;
        }

        public void setTargetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
        }
    }
}
