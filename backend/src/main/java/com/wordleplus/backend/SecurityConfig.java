package com.wordleplus.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Matcher pour toutes les routes API
        var apiMatcher = new RegexRequestMatcher("^/api/.*", null);

        http
                .cors(c -> {}) // CORS activé (bean plus bas)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error", "/api/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Si pas connecté ET on tape une route /api/** → 401 (pas de redirection)
                .exceptionHandling(e -> e
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                apiMatcher
                        )
                )
                .oauth2Login(o -> o
                        // après login Google réussi → renvoie vers le front
                        .successHandler((req, res, auth) -> res.sendRedirect("http://localhost:5173/"))
                )
                .logout(l -> l.logoutSuccessUrl("http://localhost:5173/login").permitAll());

        return http.build();
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(java.util.List.of("http://localhost:5173"));
        config.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setAllowCredentials(true); // indispensable pour envoyer les cookies
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
