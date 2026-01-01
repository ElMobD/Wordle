package com.wordle.backend.config;

import com.wordle.backend.service.JwtService;
import com.wordle.backend.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        
        // Récupérer les infos de Google
        String googleId = principal.getAttribute("sub");  // L'ID Google unique
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        String picture = principal.getAttribute("picture");
        
        // Sauvegarder ou mettre à jour l'utilisateur en base de données
        userService.findOrCreateUser(googleId, email, name, picture);
        
        // Générer le JWT
        String token = jwtService.generateTokenFromOAuth2(email, name);
        
        // Créer le JSON user
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
        response.sendRedirect("http://localhost:5173/callback?token=" + token + "&user=" + userJson);
    }
}
