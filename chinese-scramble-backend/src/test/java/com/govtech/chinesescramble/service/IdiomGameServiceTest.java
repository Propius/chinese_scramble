package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.GameSession;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.entity.enums.SessionStatus;
import com.govtech.chinesescramble.entity.enums.UserRole;
import com.govtech.chinesescramble.exception.AllQuestionsCompletedException;
import com.govtech.chinesescramble.repository.IdiomScoreRepository;
import com.govtech.chinesescramble.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IdiomGameService
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class IdiomGameServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private IdiomScoreRepository idiomScoreRepository;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private ValidationService validationService;

    @Mock
    private ScoringService scoringService;

    @Mock
    private AchievementService achievementService;

    @Mock
    private LeaderboardService leaderboardService;

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private QuestionHistoryService questionHistoryService;

    @InjectMocks
    private IdiomGameService idiomGameService;

    private Player testPlayer;
    private Map<String, Object> idiomConfig;
    private GameSession testSession;

    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hash")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        ReflectionTestUtils.setField(testPlayer, "id", 1L);

        // Setup idiom configuration
        idiomConfig = new HashMap<>();
        List<Map<String, Object>> idioms = new ArrayList<>();

        Map<String, Object> idiom1 = new HashMap<>();
        idiom1.put("idiom", "一帆风顺");
        idiom1.put("difficulty", "EASY");
        idiom1.put("definition", "Smooth sailing");
        idiom1.put("pinyin", "yī fān fēng shùn");
        idioms.add(idiom1);

        Map<String, Object> idiom2 = new HashMap<>();
        idiom2.put("idiom", "画蛇添足");
        idiom2.put("difficulty", "MEDIUM");
        idiom2.put("definition", "Gild the lily");
        idiom2.put("pinyin", "huà shé tiān zú");
        idioms.add(idiom2);

        Map<String, Object> idiom3 = new HashMap<>();
        idiom3.put("idiom", "守株待兔");
        idiom3.put("difficulty", "HARD");
        idiom3.put("definition", "Wait for windfalls");
        idiom3.put("pinyin", "shǒu zhū dài tù");
        idioms.add(idiom3);

        idiomConfig.put("idioms", idioms);

        // Setup test session
        testSession = GameSession.builder()
            .player(testPlayer)
            .gameType(GameType.IDIOM)
            .difficulty(DifficultyLevel.EASY)
            .status(SessionStatus.ACTIVE)
            .sessionData("{\"idiom\":\"一帆风顺\",\"difficulty\":\"EASY\"}")
            .build();
        ReflectionTestUtils.setField(testSession, "id", 1L);
        testSession.setStartedAt(LocalDateTime.now());

        // Set no-repeat feature to false by default
        ReflectionTestUtils.setField(idiomGameService, "enableNoRepeatQuestions", false);
    }

    @Test
    void testStartGame_Success() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(configurationService.loadConfigurationAsMap("idioms.json")).thenReturn(idiomConfig);
        when(gameSessionService.sessionDataToJson(anyMap())).thenReturn("{}");

        // When
        var result = idiomGameService.startGame(1L, DifficultyLevel.EASY);

        // Then
        assertNotNull(result);
        assertEquals(4, result.characterCount());
        assertEquals(180, result.timeLimitSeconds());
        assertEquals(DifficultyLevel.EASY, result.difficulty());
        assertNotNull(result.scrambledCharacters());
        assertFalse(result.scrambledCharacters().isEmpty());
        verify(gameSessionService).createSession(eq(1L), eq(GameType.IDIOM), eq(DifficultyLevel.EASY), anyString());
    }

    @Test
    void testStartGame_PlayerNotFound() {
        // Given
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> idiomGameService.startGame(999L, DifficultyLevel.EASY));
    }

    @Test
    void testStartGame_NoIdiomsForDifficulty() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(configurationService.loadConfigurationAsMap("idioms.json")).thenReturn(idiomConfig);

        // When/Then - EXPERT difficulty has no idioms in test config
        assertThrows(IllegalStateException.class,
            () -> idiomGameService.startGame(1L, DifficultyLevel.EXPERT));
    }

    @Test
    void testStartGame_WithNoRepeatEnabled_AllQuestionsCompleted() {
        // Given
        ReflectionTestUtils.setField(idiomGameService, "enableNoRepeatQuestions", true);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(configurationService.loadConfigurationAsMap("idioms.json")).thenReturn(idiomConfig);

        // All EASY idioms already completed
        Set<String> completedIdioms = new HashSet<>(Arrays.asList("一帆风顺"));
        when(questionHistoryService.getExcludedQuestions(1L, "IDIOM")).thenReturn(completedIdioms);

        // When/Then
        assertThrows(AllQuestionsCompletedException.class,
            () -> idiomGameService.startGame(1L, DifficultyLevel.EASY));
    }

    @Test
    void testSubmitAnswer_CorrectAnswer() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(gameSessionService.getActiveSession(1L)).thenReturn(Optional.of(testSession));

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("idiom", "一帆风顺");
        sessionData.put("difficulty", "EASY");
        when(gameSessionService.parseSessionData(anyString())).thenReturn(sessionData);

        ValidationService.IdiomValidationResult validationResult =
            new ValidationService.IdiomValidationResult(true, 1.0);
        when(validationService.validateIdiom("一帆风顺", "一帆风顺")).thenReturn(validationResult);

        when(scoringService.calculateIdiomScore(eq(DifficultyLevel.EASY), eq(45), eq(1.0), eq(0)))
            .thenReturn(100);
        when(achievementService.checkAndUnlockAchievements(anyLong(), anyInt(), anyInt(), anyDouble(), anyInt()))
            .thenReturn(Collections.emptyList());

        // When
        var result = idiomGameService.submitAnswer(1L, "一帆风顺", 45, 0);

        // Then
        assertNotNull(result);
        assertTrue(result.isCorrect());
        assertEquals(100, result.score());
        assertEquals("一帆风顺", result.correctIdiom());
        verify(idiomScoreRepository).save(any());
        verify(gameSessionService).completeSession(anyLong(), eq(100));
    }

    @Test
    void testSubmitAnswer_NoActiveSession() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(gameSessionService.getActiveSession(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalStateException.class,
            () -> idiomGameService.submitAnswer(1L, "一帆风顺", 45, 0));
    }

    @Test
    void testGetHint_Level1() {
        // Given
        when(gameSessionService.getActiveSession(1L)).thenReturn(Optional.of(testSession));

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("idiom", "一帆风顺");
        when(gameSessionService.parseSessionData(anyString())).thenReturn(sessionData);

        // When
        var result = idiomGameService.getHint(1L, 1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.level());
        assertNotNull(result.content());
        assertTrue(result.content().contains("一")); // First character hint
    }

    @Test
    void testGetHint_InvalidLevel() {
        // When/Then - hint level validation happens before session check
        assertThrows(IllegalArgumentException.class,
            () -> idiomGameService.getHint(1L, 0));
        assertThrows(IllegalArgumentException.class,
            () -> idiomGameService.getHint(1L, 4));
    }

    @Test
    void testGetHint_NoActiveSession() {
        // Given
        when(gameSessionService.getActiveSession(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalStateException.class,
            () -> idiomGameService.getHint(1L, 1));
    }

    @Test
    void testRestartQuiz() {
        // When
        idiomGameService.restartQuiz(1L);

        // Then
        verify(questionHistoryService).clearHistory(1L, "IDIOM");
    }
}
