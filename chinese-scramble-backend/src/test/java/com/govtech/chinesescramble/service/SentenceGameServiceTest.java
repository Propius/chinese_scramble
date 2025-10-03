package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.Achievement;
import com.govtech.chinesescramble.entity.GameSession;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.SentenceScore;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.entity.enums.SessionStatus;
import com.govtech.chinesescramble.exception.AllQuestionsCompletedException;
import com.govtech.chinesescramble.repository.PlayerRepository;
import com.govtech.chinesescramble.repository.SentenceScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for SentenceGameService
 * Focus: Coverage improvement for low-coverage methods
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SentenceGameServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private SentenceScoreRepository sentenceScoreRepository;

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
    private SentenceGameService sentenceGameService;

    private Player testPlayer;
    private Map<String, Object> mockConfig;
    private List<Map<String, Object>> mockSentences;

    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hash")
            .build();
        ReflectionTestUtils.setField(testPlayer, "id", 1L);

        // Mock sentences configuration
        mockSentences = List.of(
            createMockSentence("我喜欢学习中文", "EASY", "I like learning Chinese",
                List.of("我", "喜欢", "学习", "中文")),
            createMockSentence("今天天气很好", "EASY", "The weather is very good today",
                List.of("今天", "天气", "很", "好")),
            createMockSentence("我们一起去图书馆学习", "MEDIUM", "Let's go to the library together",
                List.of("我们", "一起", "去", "图书馆", "学习"))
        );

        mockConfig = Map.of("sentences", mockSentences);
    }

    private Map<String, Object> createMockSentence(String target, String difficulty,
                                                    String meaning, List<String> words) {
        Map<String, String> hints = new HashMap<>();
        hints.put("level1", "Translation hint");
        hints.put("level2", "First word hint");
        hints.put("level3", "First half hint");

        Map<String, Object> sentence = new HashMap<>();
        sentence.put("targetSentence", target);
        sentence.put("difficulty", difficulty);
        sentence.put("meaning", meaning);
        sentence.put("pinyin", "wǒ xǐhuan xuéxí zhōngwén");
        sentence.put("words", words);
        sentence.put("hints", hints);
        sentence.put("grammarPoints", List.of("Subject-Verb-Object structure", "动词+宾语"));

        return sentence;
    }

    // ========================================================================
    // startGame() Tests - Target: 78% → 95%+
    // ========================================================================

    @Test
    void testStartGame_Success_NoRepeatDisabled() {
        // Arrange
        ReflectionTestUtils.setField(sentenceGameService, "enableNoRepeatQuestions", false);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(configurationService.loadConfigurationAsMap("sentences.json")).thenReturn(mockConfig);
        when(gameSessionService.sessionDataToJson(any())).thenReturn("{\"session\":\"data\"}");

        // Act
        SentenceGameService.SentenceGameState result = sentenceGameService.startGame(1L, DifficultyLevel.EASY);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.scrambledWords()).hasSize(4);
        assertThat(result.wordCount()).isEqualTo(4);
        assertThat(result.timeLimitSeconds()).isEqualTo(DifficultyLevel.EASY.getTimeLimitSeconds());
        assertThat(result.difficulty()).isEqualTo(DifficultyLevel.EASY);
        assertThat(result.meaning()).isNotBlank();
        assertThat(result.pinyin()).isNotBlank();

        // Verify no history tracking when feature disabled
        verify(questionHistoryService, never()).getExcludedQuestions(anyLong(), anyString());
        verify(questionHistoryService, never()).addQuestion(anyLong(), anyString(), anyString());
        verify(gameSessionService).createSession(eq(1L), eq(GameType.SENTENCE), eq(DifficultyLevel.EASY), anyString());
    }

    @Test
    void testStartGame_Success_NoRepeatEnabled() {
        // Arrange
        ReflectionTestUtils.setField(sentenceGameService, "enableNoRepeatQuestions", true);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(configurationService.loadConfigurationAsMap("sentences.json")).thenReturn(mockConfig);
        when(questionHistoryService.getExcludedQuestions(1L, "SENTENCE")).thenReturn(Set.of());
        when(gameSessionService.sessionDataToJson(any())).thenReturn("{\"session\":\"data\"}");

        // Act
        SentenceGameService.SentenceGameState result = sentenceGameService.startGame(1L, DifficultyLevel.EASY);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.scrambledWords()).hasSize(4);

        // Verify history tracking when feature enabled
        verify(questionHistoryService).getExcludedQuestions(1L, "SENTENCE");
        verify(questionHistoryService).addQuestion(eq(1L), eq("SENTENCE"), anyString());
        verify(gameSessionService).createSession(eq(1L), eq(GameType.SENTENCE), eq(DifficultyLevel.EASY), anyString());
    }

    @Test
    void testStartGame_AllQuestionsCompleted() {
        // Arrange
        ReflectionTestUtils.setField(sentenceGameService, "enableNoRepeatQuestions", true);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(configurationService.loadConfigurationAsMap("sentences.json")).thenReturn(mockConfig);

        // All easy sentences already completed
        Set<String> excludedSentences = Set.of("我喜欢学习中文", "今天天气很好");
        when(questionHistoryService.getExcludedQuestions(1L, "SENTENCE")).thenReturn(excludedSentences);

        // Act & Assert
        AllQuestionsCompletedException exception = assertThrows(
            AllQuestionsCompletedException.class,
            () -> sentenceGameService.startGame(1L, DifficultyLevel.EASY)
        );

        assertThat(exception.getMessage()).contains("恭喜");
        assertThat(exception.getMessage()).contains(DifficultyLevel.EASY.getLabel());

        verify(questionHistoryService).getExcludedQuestions(1L, "SENTENCE");
        verify(questionHistoryService, never()).addQuestion(anyLong(), anyString(), anyString());
        verify(gameSessionService, never()).createSession(anyLong(), any(), any(), anyString());
    }

    @Test
    void testStartGame_PlayerNotFound() {
        // Arrange
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sentenceGameService.startGame(999L, DifficultyLevel.EASY)
        );

        assertThat(exception.getMessage()).contains("Player not found");
    }

    @Test
    void testStartGame_NoDifficultySentences() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(configurationService.loadConfigurationAsMap("sentences.json")).thenReturn(mockConfig);

        // Act & Assert - EXPERT difficulty has no sentences in mock data
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> sentenceGameService.startGame(1L, DifficultyLevel.EXPERT)
        );

        assertThat(exception.getMessage()).contains("No sentences found for difficulty");
    }

    // ========================================================================
    // getHint() Tests - Target: 89% → 95%+
    // ========================================================================

    @Test
    void testGetHint_Success_Level1() {
        // Arrange
        GameSession mockSession = createMockGameSession();
        when(gameSessionService.getActiveSession(1L)).thenReturn(Optional.of(mockSession));
        when(gameSessionService.getHintCount(mockSession.getId())).thenReturn(0);
        when(configurationService.loadConfigurationAsMap("sentences.json")).thenReturn(mockConfig);
        when(scoringService.getHintPenalty(1)).thenReturn(10);

        // Act
        SentenceGameService.SentenceHint result = sentenceGameService.getHint(1L, 1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.level()).isEqualTo(1);
        assertThat(result.content()).contains("💡");
        assertThat(result.penalty()).isEqualTo(10);

        verify(gameSessionService).addHintUsage(eq(mockSession.getId()), eq(1), eq(10), anyString());
    }

    @Test
    void testGetHint_Success_Level2() {
        // Arrange
        GameSession mockSession = createMockGameSession();
        when(gameSessionService.getActiveSession(1L)).thenReturn(Optional.of(mockSession));
        when(gameSessionService.getHintCount(mockSession.getId())).thenReturn(1);
        when(configurationService.loadConfigurationAsMap("sentences.json")).thenReturn(mockConfig);
        when(scoringService.getHintPenalty(2)).thenReturn(20);

        // Act
        SentenceGameService.SentenceHint result = sentenceGameService.getHint(1L, 2);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.level()).isEqualTo(2);
        assertThat(result.content()).contains("🔤");
        assertThat(result.penalty()).isEqualTo(20);

        verify(gameSessionService).addHintUsage(eq(mockSession.getId()), eq(2), eq(20), anyString());
    }

    @Test
    void testGetHint_Success_Level3() {
        // Arrange
        GameSession mockSession = createMockGameSession();
        when(gameSessionService.getActiveSession(1L)).thenReturn(Optional.of(mockSession));
        when(gameSessionService.getHintCount(mockSession.getId())).thenReturn(2);
        when(configurationService.loadConfigurationAsMap("sentences.json")).thenReturn(mockConfig);
        when(scoringService.getHintPenalty(3)).thenReturn(30);

        // Act
        SentenceGameService.SentenceHint result = sentenceGameService.getHint(1L, 3);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.level()).isEqualTo(3);
        assertThat(result.content()).contains("📝");
        assertThat(result.penalty()).isEqualTo(30);

        verify(gameSessionService).addHintUsage(eq(mockSession.getId()), eq(3), eq(30), anyString());
    }

    @Test
    void testGetHint_InvalidLevel_TooLow() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sentenceGameService.getHint(1L, 0)
        );

        assertThat(exception.getMessage()).contains("Invalid hint level");
    }

    @Test
    void testGetHint_InvalidLevel_TooHigh() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sentenceGameService.getHint(1L, 4)
        );

        assertThat(exception.getMessage()).contains("Invalid hint level");
    }

    @Test
    void testGetHint_NoActiveSession() {
        // Arrange
        when(gameSessionService.getActiveSession(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> sentenceGameService.getHint(1L, 1)
        );

        assertThat(exception.getMessage()).contains("No active game session found");
    }

    @Test
    void testGetHint_MaxHintsExceeded() {
        // Arrange - Don't mock parseSessionData since it won't be called
        GameSession mockSession = createMockGameSession(false);
        when(gameSessionService.getActiveSession(1L)).thenReturn(Optional.of(mockSession));
        when(gameSessionService.getHintCount(mockSession.getId())).thenReturn(3);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> sentenceGameService.getHint(1L, 1)
        );

        assertThat(exception.getMessage()).contains("Maximum hints (3) already used");
        verify(gameSessionService, never()).addHintUsage(anyLong(), anyInt(), anyInt(), anyString());
    }

    // ========================================================================
    // getPersonalBest() Tests - Target: 0% → 100%
    // ========================================================================

    @Test
    void testGetPersonalBest_Found() {
        // Arrange
        SentenceScore bestScore = SentenceScore.builder()
            .player(testPlayer)
            .targetSentence("我喜欢学习中文")
            .playerSentence("我喜欢学习中文")
            .score(500)
            .difficulty(DifficultyLevel.EASY)
            .timeTaken(25)
            .accuracyRate(1.0)
            .grammarScore(100)
            .similarityScore(1.0)
            .completed(true)
            .build();
        ReflectionTestUtils.setField(bestScore, "id", 1L);

        when(sentenceScoreRepository.findPersonalBest(1L, DifficultyLevel.EASY))
            .thenReturn(Optional.of(bestScore));

        // Act
        Optional<SentenceScore> result = sentenceGameService.getPersonalBest(1L, DifficultyLevel.EASY);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getScore()).isEqualTo(500);
        assertThat(result.get().getDifficulty()).isEqualTo(DifficultyLevel.EASY);

        verify(sentenceScoreRepository).findPersonalBest(1L, DifficultyLevel.EASY);
    }

    @Test
    void testGetPersonalBest_NotFound() {
        // Arrange
        when(sentenceScoreRepository.findPersonalBest(1L, DifficultyLevel.HARD))
            .thenReturn(Optional.empty());

        // Act
        Optional<SentenceScore> result = sentenceGameService.getPersonalBest(1L, DifficultyLevel.HARD);

        // Assert
        assertThat(result).isEmpty();

        verify(sentenceScoreRepository).findPersonalBest(1L, DifficultyLevel.HARD);
    }

    // ========================================================================
    // restartQuiz() Tests - Already 100% but add for completeness
    // ========================================================================

    @Test
    void testRestartQuiz_Success() {
        // Arrange
        doNothing().when(questionHistoryService).clearHistory(1L, "SENTENCE");

        // Act
        sentenceGameService.restartQuiz(1L);

        // Assert
        verify(questionHistoryService).clearHistory(1L, "SENTENCE");
    }

    // ========================================================================
    // getPlayerHistory() Tests - Already 100% but add for completeness
    // ========================================================================

    @Test
    void testGetPlayerHistory_ReturnsScores() {
        // Arrange
        SentenceScore score1 = SentenceScore.builder()
            .player(testPlayer)
            .targetSentence("test1")
            .playerSentence("test1")
            .score(100)
            .difficulty(DifficultyLevel.EASY)
            .timeTaken(30)
            .accuracyRate(0.9)
            .grammarScore(90)
            .similarityScore(0.9)
            .completed(true)
            .build();
        ReflectionTestUtils.setField(score1, "id", 1L);

        SentenceScore score2 = SentenceScore.builder()
            .player(testPlayer)
            .targetSentence("test2")
            .playerSentence("test2")
            .score(200)
            .difficulty(DifficultyLevel.MEDIUM)
            .timeTaken(25)
            .accuracyRate(0.95)
            .grammarScore(95)
            .similarityScore(0.95)
            .completed(true)
            .build();
        ReflectionTestUtils.setField(score2, "id", 2L);

        List<SentenceScore> mockScores = List.of(score1, score2);

        when(sentenceScoreRepository.findRecentScores(1L)).thenReturn(mockScores);

        // Act
        List<SentenceScore> result = sentenceGameService.getPlayerHistory(1L, 10);

        // Assert
        assertThat(result).hasSize(2);
        verify(sentenceScoreRepository).findRecentScores(1L);
    }

    // ========================================================================
    // submitAnswer() Tests - Already 97% coverage, add edge cases
    // ========================================================================

    @Test
    void testSubmitAnswer_ValidAnswer() {
        // Arrange
        GameSession mockSession = createMockGameSession();
        ValidationService.ValidationResult validationResult = new ValidationService.ValidationResult(
            true, 95, 0.98, List.of()
        );

        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(gameSessionService.getActiveSession(1L)).thenReturn(Optional.of(mockSession));
        when(validationService.validateSentence(anyString(), anyString(), anyList()))
            .thenReturn(validationResult);
        when(scoringService.calculateSentenceScore(any(), anyInt(), anyDouble(), anyInt(), anyInt()))
            .thenReturn(500);
        when(validationService.errorsToJson(anyList())).thenReturn("[]");
        when(achievementService.checkAndUnlockAchievements(anyLong(), anyInt(), anyInt(), anyDouble(), anyInt()))
            .thenReturn(List.of());
        when(configurationService.loadConfigurationAsMap("sentences.json")).thenReturn(mockConfig);

        // Act
        SentenceGameService.SentenceGameResult result = sentenceGameService.submitAnswer(
            1L, "我喜欢学习中文", 25, 0
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.score()).isEqualTo(500);
        assertThat(result.grammarScore()).isEqualTo(95);
        assertThat(result.similarityScore()).isEqualTo(0.98);

        verify(sentenceScoreRepository).save(any(SentenceScore.class));
        verify(gameSessionService).completeSession(eq(mockSession.getId()), eq(500));
        verify(leaderboardService).updateLeaderboardAfterGame(
            eq(1L), eq(GameType.SENTENCE), eq(DifficultyLevel.EASY), eq(500), anyDouble()
        );
    }

    @Test
    void testSubmitAnswer_InvalidAnswer() {
        // Arrange
        GameSession mockSession = createMockGameSession();
        ValidationService.ValidationResult validationResult = new ValidationService.ValidationResult(
            false, 50, 0.60, List.of(
                new ValidationService.ValidationError("GRAMMAR", "Word order incorrect", 10)
            )
        );

        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(gameSessionService.getActiveSession(1L)).thenReturn(Optional.of(mockSession));
        when(validationService.validateSentence(anyString(), anyString(), anyList()))
            .thenReturn(validationResult);
        when(validationService.errorsToJson(anyList())).thenReturn("[{\"type\":\"GRAMMAR\"}]");
        when(achievementService.checkAndUnlockAchievements(anyLong(), anyInt(), anyInt(), anyDouble(), anyInt()))
            .thenReturn(List.of());
        when(configurationService.loadConfigurationAsMap("sentences.json")).thenReturn(mockConfig);

        // Act
        SentenceGameService.SentenceGameResult result = sentenceGameService.submitAnswer(
            1L, "中文学习喜欢我", 45, 2
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.score()).isZero();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0)).contains("Word order incorrect");

        verify(sentenceScoreRepository).save(any(SentenceScore.class));
        verify(gameSessionService).completeSession(eq(mockSession.getId()), eq(0));
        verify(leaderboardService, never()).updateLeaderboardAfterGame(anyLong(), any(), any(), anyInt(), anyDouble());
    }

    @Test
    void testSubmitAnswer_NoActiveSession() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(gameSessionService.getActiveSession(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> sentenceGameService.submitAnswer(1L, "我喜欢学习中文", 30, 0)
        );

        assertThat(exception.getMessage()).contains("No active game session found");
    }

    // ========================================================================
    // SentenceGameResult Record Tests
    // ========================================================================

    @Test
    void testSentenceGameResult_IsPerfectScore_True() {
        // Arrange
        SentenceGameService.SentenceGameResult result = new SentenceGameService.SentenceGameResult(
            true, "我喜欢学习中文", "我喜欢学习中文", 500, 1.0, 100, 1.0, 25, 0,
            List.of(), List.of(), "I like learning Chinese", "Subject-Verb-Object", "wǒ xǐhuan"
        );

        // Act & Assert
        assertThat(result.isPerfectScore()).isTrue();
    }

    @Test
    void testSentenceGameResult_IsPerfectScore_False_WithHints() {
        // Arrange
        SentenceGameService.SentenceGameResult result = new SentenceGameService.SentenceGameResult(
            true, "我喜欢学习中文", "我喜欢学习中文", 450, 1.0, 100, 1.0, 25, 1,
            List.of(), List.of(), "I like learning Chinese", "Subject-Verb-Object", "wǒ xǐhuan"
        );

        // Act & Assert
        assertThat(result.isPerfectScore()).isFalse();
    }

    @Test
    void testSentenceGameResult_IsPerfectScore_False_LowGrammar() {
        // Arrange
        SentenceGameService.SentenceGameResult result = new SentenceGameService.SentenceGameResult(
            true, "我喜欢学习中文", "我喜欢学习中文", 400, 0.95, 90, 1.0, 25, 0,
            List.of(), List.of(), "I like learning Chinese", "Subject-Verb-Object", "wǒ xǐhuan"
        );

        // Act & Assert
        assertThat(result.isPerfectScore()).isFalse();
    }

    @Test
    void testSentenceGameResult_IsPerfectScore_False_Invalid() {
        // Arrange
        SentenceGameService.SentenceGameResult result = new SentenceGameService.SentenceGameResult(
            false, "我喜欢学习中文", "中文学习喜欢我", 0, 0.50, 100, 0.60, 45, 0,
            List.of("Word order incorrect"), List.of(), "I like learning Chinese", "Subject-Verb-Object", "wǒ xǐhuan"
        );

        // Act & Assert
        assertThat(result.isPerfectScore()).isFalse();
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private GameSession createMockGameSession() {
        return createMockGameSession(true);
    }

    private GameSession createMockGameSession(boolean mockParseSessionData) {
        Map<String, Object> sessionData = Map.of(
            "targetSentence", "我喜欢学习中文",
            "scrambledWords", List.of("中文", "学习", "我", "喜欢"),
            "allowedWords", List.of("我", "喜欢", "学习", "中文"),
            "difficulty", "EASY"
        );

        String sessionDataJson = "{\"targetSentence\":\"我喜欢学习中文\",\"difficulty\":\"EASY\"}";

        if (mockParseSessionData) {
            when(gameSessionService.parseSessionData(anyString())).thenReturn(sessionData);
        }

        GameSession session = GameSession.builder()
            .player(testPlayer)
            .gameType(GameType.SENTENCE)
            .difficulty(DifficultyLevel.EASY)
            .status(SessionStatus.ACTIVE)
            .sessionData(sessionDataJson)
            .startedAt(LocalDateTime.now())
            .build();
        ReflectionTestUtils.setField(session, "id", 1L);

        return session;
    }
}
