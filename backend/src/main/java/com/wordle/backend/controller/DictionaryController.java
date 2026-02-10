package com.wordle.backend.controller;

import com.wordle.backend.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dictionary")
public class DictionaryController {

    @Autowired
    private DictionaryService dictionaryService;

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateWord(
            @RequestParam("word") String word,
            @RequestParam(value = "min", required = false) Integer min,
            @RequestParam(value = "max", required = false) Integer max
    ) {
        boolean valid;
        if (min != null && max != null) {
            valid = dictionaryService.isValidWord(word, min, max);
        } else {
            valid = dictionaryService.isValidWord(word);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("word", word);
        response.put("valid", valid);
        return ResponseEntity.ok(response);
    }
}
