package com.wordleplus.backend;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {
    @GetMapping("/")
    public String index() {
        return "Bienvenue sur Wordle+. <a href=\"/oauth2/authorization/google\">Se connecter avec Google</a>";
    }

    @GetMapping("/continue")
    public Map<String, Object> afterLogin(@AuthenticationPrincipal OAuth2User user) {
        // renvoie quelques infos pour vérifier que tu es bien connecté
        return Map.of(
                "message", "Connecté !",
                "name", user.getAttribute("name"),
                "email", user.getAttribute("email"),
                "sub", user.getAttribute("sub")
        );
    }
}
