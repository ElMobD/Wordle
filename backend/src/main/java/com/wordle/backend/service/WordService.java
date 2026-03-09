package com.wordle.backend.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Service
public class WordService {

    private final List<String> dailyWords;
    private final List<String> randomWords;
    private final Map<Integer, List<String>> randomWordsByLength;
    private final Set<String> allValidWords;
    private final Random random;

    public WordService() {
        this.dailyWords = loadWords("dailywords.txt");
        this.randomWords = loadWords("randomwords.txt");
        this.randomWordsByLength = indexByLength(this.randomWords);
        this.allValidWords = buildAllValidWords(this.dailyWords, this.randomWords);
        this.random = new Random();
    }

    public String getDailyWord() {
        if (dailyWords.isEmpty()) {
            return "adieu";
        }
        long dayIndex = LocalDate.now().toEpochDay();
        int index = (int) (Math.floorMod(dayIndex, dailyWords.size()));
        return dailyWords.get(index);
    }

    public String getRandomWord() {
        if (randomWords.isEmpty()) {
            return "adieu";
        }
        return randomWords.get(random.nextInt(randomWords.size()));
    }

    public String getRandomWord(int length) {
        List<String> wordsOfLength = randomWordsByLength.get(length);
        if (wordsOfLength == null || wordsOfLength.isEmpty()) {
            // Si aucun mot de la longueur spécifiée, retourner un mot par défaut
            return "adieu";
        }
        return wordsOfLength.get(random.nextInt(wordsOfLength.size()));
    }

    public boolean isValidWord(String word) {
        if (word == null) {
            return false;
        }
        String lower = word.trim().toLowerCase();
        return allValidWords.contains(lower);
    }

    private Map<Integer, List<String>> indexByLength(List<String> words) {
        Map<Integer, List<String>> byLength = new HashMap<>();
        for (String word : words) {
            byLength.computeIfAbsent(word.length(), ignored -> new ArrayList<>()).add(word);
        }
        return byLength;
    }

    private Set<String> buildAllValidWords(List<String> daily, List<String> random) {
        Set<String> allWords = new HashSet<>(daily.size() + random.size());
        allWords.addAll(daily);
        allWords.addAll(random);
        return allWords;
    }

    private List<String> loadWords(String resourceName) {
        List<String> words = new ArrayList<>();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                return words;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim().toLowerCase();
                    if (!word.isEmpty()) {
                        words.add(word);
                    }
                }
            }
        } catch (Exception ignored) {
            return words;
        }
        return words;
    }
}
