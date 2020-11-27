package com.tjwoods.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import java.io.IOException;
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
    public String index(Map<String, Object> params) {
        params.put("tokens", giveTokenProperties.getSamlTokens());
        return "index";
    }

    @GetMapping("/choose-user/{userId}")
    @ResponseBody
    public void chooseUser(@PathVariable("userId") String userId) throws ServletException, IOException {
        final Optional<TokenPojo> first = giveTokenProperties.getSamlTokens().stream().filter(tokenPojo -> userId.equals(tokenPojo.getId())).findFirst();
        if (first.isPresent()) {
            CustomProxyServlet.currentToken = first.get().getToken();
        } else {
            throw new ServletException("Invalid userId.");
        }
    }

    @GetMapping("/logout")
    public String logOut(Map<String, Object> params) {
        CustomProxyServlet.currentToken = null;
        params.put("tokens", giveTokenProperties.getSamlTokens());
        return "redirect:/";
    }
}
