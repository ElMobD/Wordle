package com.wordle.backend;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {
  
  @GetMapping("/test")
  public Map<String, Object> test(@AuthenticationPrincipal OAuth2User user) {
    return Map.of(
      "message", "Tu es connecté!",
      "name", user.getAttribute("name"),
      "email", user.getAttribute("email"),
      "picture", user.getAttribute("picture"),
      "allAttributes", user.getAttributes()
    );
  }
}
