package com.wordle.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public String home() {
        return "<h>Voici où se trouve l'api backend Spring boot</h>";
    }

    @GetMapping("/api/health")
    public String health() {
        return "C'est ok, l'api fonctionne!";
    }

    // Catch-all 404, mais on ignore explicitement les endpoints WebSocket
    @RequestMapping(path = "/**", headers = {"!Upgrade"})
    public ResponseEntity<Map<String, Object>> notFound() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Endpoint not found");
        response.put("status", 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
    // Optionnel : ignorer explicitement /socket et /ws pour les requêtes non-REST
    // (Spring ne mappe pas les WebSocket sur les controllers REST, mais on évite tout conflit)
