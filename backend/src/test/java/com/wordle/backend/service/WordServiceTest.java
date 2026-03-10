package com.wordle.backend.service;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordServiceTest {

    @Test
    void shouldReturnSameDailyWordWithinSameDay() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-03-10T10:30:00Z"), ZoneOffset.UTC);
        WordService service = new WordService(fixedClock);

        String firstCall = service.getDailyWord();
        String secondCall = service.getDailyWord();

        assertEquals(firstCall, secondCall);
    }

    @Test
    void shouldChangeDailyWordAtMidnight() {
        Clock beforeMidnight = Clock.fixed(Instant.parse("2026-03-10T23:59:59Z"), ZoneOffset.UTC);
        Clock afterMidnight = Clock.fixed(Instant.parse("2026-03-11T00:00:00Z"), ZoneOffset.UTC);

        String wordBefore = new WordService(beforeMidnight).getDailyWord();
        String wordAfter = new WordService(afterMidnight).getDailyWord();

        assertNotEquals(wordBefore, wordAfter);
    }

    @Test
    void shouldPickDailyWordFromDailyWordsFile() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-03-10T10:30:00Z"), ZoneOffset.UTC);
        WordService service = new WordService(fixedClock);

        String selectedWord = service.getDailyWord();
        Set<String> dailyWords = readWords("dailywords.txt");

        assertTrue(dailyWords.contains(selectedWord));
    }

    private Set<String> readWords(String resourceName) {
        Set<String> words = new HashSet<>();

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
