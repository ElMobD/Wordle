package com.wordle.backend.controller;

import com.wordle.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @GetMapping("/")
    public String home() {
        return "Bienvenue ! <a href='/oauth2/authorization/google'>Se connecter avec Google</a>";
    }

    @GetMapping("/auth/callback")
    public String authCallback(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "redirect:http://localhost:5173/login";
        }

        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");

        // Générer le JWT
        String token = jwtService.generateTokenFromOAuth2(email, name);

        // Créer l'objet user à passer au frontend
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("name", name);

        String userJson = "";
        try {
            userJson = URLEncoder.encode(
                "{\"email\":\"" + email + "\",\"name\":\"" + name + "\"}",
                StandardCharsets.UTF_8.toString()
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Rediriger vers le frontend avec le token
        return "redirect:http://localhost:5173/callback?token=" + token + "&user=" + userJson;
    }

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        return Map.of(
            "message", "Voici tout ce que Google a renvoyé",
            "attributes", principal.getAttributes()
        );
    }
}
