package com.wordle.backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class DictionaryService {

    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 8;

    private final Map<Integer, Set<String>> wordsByLength = new HashMap<>();

    @PostConstruct
    public void loadDictionary() {
        for (int i = MIN_LENGTH; i <= MAX_LENGTH; i++) {
            wordsByLength.put(i, new HashSet<>());
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource("dictionary.txt").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = normalize(line);
                if (isAlphabetic(word) && word.length() >= MIN_LENGTH && word.length() <= MAX_LENGTH) {
                    wordsByLength.get(word.length()).add(word);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de charger le dictionnaire", e);
        }
    }

    public boolean isValidWord(String word) {
        String normalized = normalize(word);
        if (!isAlphabetic(normalized)) {
            return false;
        }
        int length = normalized.length();
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            return false;
        }
        return wordsByLength.getOrDefault(length, Set.of()).contains(normalized);
    }

    public boolean isValidWord(String word, int minLength, int maxLength) {
        String normalized = normalize(word);
        if (!isAlphabetic(normalized)) {
            return false;
        }
        int length = normalized.length();
        if (length < minLength || length > maxLength) {
            return false;
        }
        return wordsByLength.getOrDefault(length, Set.of()).contains(normalized);
    }

    private String normalize(String word) {
        return word == null ? "" : word.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isAlphabetic(String word) {
        return word != null && word.matches("[a-zA-Z]+");
    }
}
