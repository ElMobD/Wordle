package com.wordle.backend.service;

import com.wordle.backend.model.Game;
import com.wordle.backend.model.User;
import com.wordle.backend.repository.GameRepository;
import com.wordle.backend.repository.GuessRepository;
import com.wordle.backend.repository.SessionRepository;
import com.wordle.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GuessRepository guessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WordService wordService;

    @Mock
    private ScoreService scoreService;

    @Mock
    private DailyStatsService dailyStatsService;

    @Mock
    private com.wordle.backend.repository.SessionGamePlayerRepository sessionGamePlayerRepository;

    @InjectMocks
    private GameService gameService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(15L);
    }

    @Test
    void shouldReturnExistingDailyGameWhenAnswerMatchesTodayWord() {
        Game existingGame = new Game(user, Game.GameType.DAILY, "erses");

        when(userRepository.findById(15L)).thenReturn(Optional.of(user));
        when(wordService.getDailyWord()).thenReturn("erses");
        when(gameRepository.findByUserIdAndGameTypeOrderByCreatedAtDesc(15L, Game.GameType.DAILY))
                .thenReturn(List.of(existingGame));

        Game result = gameService.createGameSolo(15L, Game.GameType.DAILY);

        assertSame(existingGame, result);
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void shouldCreateNewDailyGameWhenLatestGameAnswerDiffersFromTodayWord() {
        Game yesterdayGame = new Game(user, Game.GameType.DAILY, "erses");

        when(userRepository.findById(15L)).thenReturn(Optional.of(user));
        when(wordService.getDailyWord()).thenReturn("abaca");
        when(gameRepository.findByUserIdAndGameTypeOrderByCreatedAtDesc(15L, Game.GameType.DAILY))
                .thenReturn(List.of(yesterdayGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.createGameSolo(15L, Game.GameType.DAILY);

        assertEquals(Game.GameType.DAILY, result.getGameType());
        assertEquals("abaca", result.getAnswer());
        assertSame(user, result.getUser());
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void shouldCreateRandomGameForRandomMode() {
        when(userRepository.findById(15L)).thenReturn(Optional.of(user));
        when(wordService.getRandomWord()).thenReturn("salut");
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.createGameSolo(15L, Game.GameType.RANDOM);

        assertEquals(Game.GameType.RANDOM, result.getGameType());
        assertEquals("salut", result.getAnswer());
        verify(gameRepository, never())
                .findByUserIdAndGameTypeOrderByCreatedAtDesc(eq(15L), eq(Game.GameType.DAILY));
    }
}
