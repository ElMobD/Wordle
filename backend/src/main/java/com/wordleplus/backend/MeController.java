package com.wordleplus.backend;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class MeController {
    @GetMapping("/api/me")
    public Map<String, Object> me(@AuthenticationPrincipal OAuth2User user) {
        return Map.of(
                "authenticated", user != null,
                "name", user != null ? user.getAttribute("name") : null,
                "email", user != null ? user.getAttribute("email") : null,
                "sub", user != null ? user.getAttribute("sub") : null
        );
    }
}
