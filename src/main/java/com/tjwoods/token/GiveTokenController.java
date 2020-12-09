package com.tjwoods.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
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
    public String index(Map<String, Object> params, HttpServletRequest request) {
        CustomProxyServlet.removeCurrentTokens(request.getSession().getId());
        CustomProxyServlet.removeTargetUrls(request.getSession().getId());
        params.put("tokens", giveTokenProperties.getSamlTokens());
        return "index";
    }

    @PostMapping("/choose-user/{userId}")
    @ResponseBody
    public void chooseUser(@PathVariable("userId") String userId, @RequestBody TargetUrl targetUrl, HttpServletRequest request) throws ServletException {
        final Optional<TokenPojo> first = giveTokenProperties.getSamlTokens().stream().filter(samlTokenPojo -> userId.equals(samlTokenPojo.getId())).findFirst();
        if (first.isPresent()) {
            CustomProxyServlet.putCurrentTokens(request.getSession().getId(), first.get().getToken());
            CustomProxyServlet.putTargetUrls(request.getSession().getId(), targetUrl.getTargetUrl());
        } else {
            throw new ServletException("Invalid userId.");
        }
    }

    @GetMapping("/logout")
    public String logOut(Map<String, Object> params, HttpServletRequest request) {
        CustomProxyServlet.removeCurrentTokens(request.getSession().getId());
        CustomProxyServlet.removeTargetUrls(request.getSession().getId());
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
