package com.wordle.backend.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class WordService {

    private final List<String> dailyWords;
    private final List<String> randomWords;
    private final Random random;

    public WordService() {
        this.dailyWords = loadWords("dailywords.txt");
        this.randomWords = loadWords("randomwords.txt");
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

    public boolean isValidWord(String word) {
        if (word == null) {
            return false;
        }
        String lower = word.trim().toLowerCase();
        return dailyWords.contains(lower) || randomWords.contains(lower);
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
