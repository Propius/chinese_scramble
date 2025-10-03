package com.govtech.chinesescramble.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govtech.chinesescramble.dto.request.AnswerSubmissionRequest;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.SentenceScore;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.UserRole;
import com.govtech.chinesescramble.service.PlayerService;
import com.govtech.chinesescramble.service.SentenceGameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for SentenceGameController using @WebMvcTest
 * Focuses on controller layer logic, error handling, and edge cases
 *
 * @author Claude Code Assistant
 * @version 1.0.0
 */
@WebMvcTest(controllers = SentenceGameController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
    })
class SentenceGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SentenceGameService sentenceGameService;

    @MockBean
    private PlayerService playerService;

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private Player createPlayer(Long id, String username) {
        Player player = Player.builder()
            .username(username)
            .email(username + "@test.com")
            .passwordHash("hashedPassword")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        ReflectionTestUtils.setField(player, "id", id);
        return player;
    }

    private SentenceGameService.SentenceGameState createMockGameState() {
        return new SentenceGameService.SentenceGameState(
            List.of("我", "喜欢", "学习", "中文"),
            4,
            180,
            DifficultyLevel.EASY,
            "I like learning Chinese",
            "wǒ xǐ huān xué xí zhōng wén"
        );
    }

    private SentenceGameService.SentenceGameResult createMockGameResult(int score, int timeTaken, int hintsUsed) {
        return new SentenceGameService.SentenceGameResult(
            true,
            "我喜欢学习中文",
            "我喜欢学习中文",
            score,
            100.0,
            100,
            1.0,
            timeTaken,
            hintsUsed,
            List.of(),
            List.of(),
            "I like learning Chinese",
            "Subject + verb + object structure",
            "wǒ xǐ huān xué xí zhōng wén"
        );
    }

    private SentenceGameService.SentenceHint createMockHint(int level) {
        return new SentenceGameService.SentenceHint(
            level,
            "💡 句子含义：I like learning Chinese",
            10
        );
    }

    private SentenceScore createMockScore() {
        Player player = createPlayer(1L, "testuser");
        SentenceScore score = SentenceScore.builder()
            .player(player)
            .targetSentence("我喜欢学习中文")
            .playerSentence("我喜欢学习中文")
            .score(100)
            .difficulty(DifficultyLevel.EASY)
            .timeTaken(45)
            .hintsUsed(0)
            .completed(true)
            .accuracyRate(1.0)
            .grammarScore(100)
            .similarityScore(1.0)
            .build();
        ReflectionTestUtils.setField(score, "id", 1L);
        return score;
    }

    // ========================================================================
    // Tests: startGame
    // ========================================================================

    @Test
    void startGame_WithNumericPlayerId_Success() throws Exception {
        // Given
        SentenceGameService.SentenceGameState mockGameState = createMockGameState();

        when(sentenceGameService.startGame(1L, DifficultyLevel.EASY))
            .thenReturn(mockGameState);

        // When & Then
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "EASY")
                .param("playerId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.wordCount").value(4))
            .andExpect(jsonPath("$.scrambledWords").isArray())
            .andExpect(jsonPath("$.difficulty").value("EASY"))
            .andExpect(jsonPath("$.meaning").value("I like learning Chinese"));

        verify(sentenceGameService).startGame(1L, DifficultyLevel.EASY);
    }

    @Test
    void startGame_WithUsername_ResolvesToPlayerId() throws Exception {
        // Given
        Player mockPlayer = createPlayer(5L, "testuser");
        SentenceGameService.SentenceGameState mockGameState = createMockGameState();

        when(playerService.getPlayerByUsername("testuser"))
            .thenReturn(Optional.of(mockPlayer));
        when(sentenceGameService.startGame(5L, DifficultyLevel.MEDIUM))
            .thenReturn(mockGameState);

        // When & Then
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "MEDIUM")
                .param("playerId", "testuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.wordCount").value(4));

        verify(playerService).getPlayerByUsername("testuser");
        verify(sentenceGameService).startGame(5L, DifficultyLevel.MEDIUM);
    }

    @Test
    void startGame_AutoCreatePlayer_Success() throws Exception {
        // Given
        Player newPlayer = createPlayer(10L, "newuser");
        SentenceGameService.SentenceGameState mockGameState = createMockGameState();

        when(playerService.getPlayerByUsername("newuser"))
            .thenReturn(Optional.empty());
        when(playerService.registerPlayer("newuser", "newuser@game.local", "password123"))
            .thenReturn(newPlayer);
        when(sentenceGameService.startGame(10L, DifficultyLevel.HARD))
            .thenReturn(mockGameState);

        // When & Then
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "HARD")
                .param("playerId", "newuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.wordCount").value(4));

        verify(playerService).getPlayerByUsername("newuser");
        verify(playerService).registerPlayer("newuser", "newuser@game.local", "password123");
        verify(sentenceGameService).startGame(10L, DifficultyLevel.HARD);
    }

    @Test
    void startGame_PlayerAlreadyExistsConcurrently_HandlesRaceCondition() throws Exception {
        // Given: Simulates concurrent player creation race condition
        Player existingPlayer = createPlayer(3L, "raceuser");
        SentenceGameService.SentenceGameState mockGameState = createMockGameState();

        when(playerService.getPlayerByUsername("raceuser"))
            .thenReturn(Optional.empty())  // First check: not found
            .thenReturn(Optional.of(existingPlayer));  // Second check: found after race
        when(playerService.registerPlayer("raceuser", "raceuser@game.local", "password123"))
            .thenThrow(new IllegalArgumentException("Player with username raceuser already exists"));
        when(sentenceGameService.startGame(3L, DifficultyLevel.EASY))
            .thenReturn(mockGameState);

        // When & Then
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "EASY")
                .param("playerId", "raceuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.wordCount").value(4));

        verify(sentenceGameService).startGame(3L, DifficultyLevel.EASY);
    }

    @Test
    void startGame_AutoCreatePlayerFails_ReturnsBadRequest() throws Exception {
        // Given
        when(playerService.getPlayerByUsername("failuser"))
            .thenReturn(Optional.empty());
        when(playerService.registerPlayer("failuser", "failuser@game.local", "password123"))
            .thenThrow(new IllegalArgumentException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "EASY")
                .param("playerId", "failuser"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Failed to create player: failuser"));

        verify(sentenceGameService, never()).startGame(anyLong(), any());
    }

    @Test
    void startGame_WithoutPlayerId_UsesDefaultPlayer() throws Exception {
        // Given
        SentenceGameService.SentenceGameState mockGameState = createMockGameState();

        when(sentenceGameService.startGame(1L, DifficultyLevel.EASY))
            .thenReturn(mockGameState);

        // When & Then
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "EASY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.wordCount").value(4));

        verify(sentenceGameService).startGame(1L, DifficultyLevel.EASY);
    }

    @Test
    void startGame_InvalidDifficulty_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "INVALID")
                .param("playerId", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());

        verify(sentenceGameService, never()).startGame(anyLong(), any());
    }

    @Test
    void startGame_ServiceThrowsException_ReturnsBadRequest() throws Exception {
        // Given
        when(sentenceGameService.startGame(1L, DifficultyLevel.EASY))
            .thenThrow(new RuntimeException("No sentences available"));

        // When & Then
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "EASY")
                .param("playerId", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("No sentences available"));
    }

    // ========================================================================
    // Tests: submitAnswer
    // ========================================================================

    @Test
    void submitAnswer_WithNumericPlayerId_Success() throws Exception {
        // Given
        AnswerSubmissionRequest request = new AnswerSubmissionRequest(
            "我喜欢学习中文",
            45,
            0
        );
        SentenceGameService.SentenceGameResult mockResult = createMockGameResult(100, 45, 0);

        when(sentenceGameService.submitAnswer(1L, "我喜欢学习中文", 45, 0))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/sentence-game/submit")
                .param("playerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").value(100))
            .andExpect(jsonPath("$.isValid").value(true));

        verify(sentenceGameService).submitAnswer(1L, "我喜欢学习中文", 45, 0);
    }

    @Test
    void submitAnswer_WithUsername_ResolvesToPlayerId() throws Exception {
        // Given
        Player mockPlayer = createPlayer(2L, "player2");
        AnswerSubmissionRequest request = new AnswerSubmissionRequest(
            "今天天气很好",
            30,
            1
        );
        SentenceGameService.SentenceGameResult mockResult = createMockGameResult(90, 30, 1);

        when(playerService.getPlayerByUsername("player2"))
            .thenReturn(Optional.of(mockPlayer));
        when(sentenceGameService.submitAnswer(2L, "今天天气很好", 30, 1))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/sentence-game/submit")
                .param("playerId", "player2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").value(90));

        verify(playerService).getPlayerByUsername("player2");
        verify(sentenceGameService).submitAnswer(2L, "今天天气很好", 30, 1);
    }

    @Test
    void submitAnswer_AutoCreatePlayer_Success() throws Exception {
        // Given
        Player newPlayer = createPlayer(7L, "newsubmitter");
        AnswerSubmissionRequest request = new AnswerSubmissionRequest(
            "中文很难",
            60,
            2
        );
        SentenceGameService.SentenceGameResult mockResult = createMockGameResult(70, 60, 2);

        when(playerService.getPlayerByUsername("newsubmitter"))
            .thenReturn(Optional.empty());
        when(playerService.registerPlayer("newsubmitter", "newsubmitter@game.local", "password123"))
            .thenReturn(newPlayer);
        when(sentenceGameService.submitAnswer(7L, "中文很难", 60, 2))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/sentence-game/submit")
                .param("playerId", "newsubmitter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").value(70));

        verify(playerService).registerPlayer("newsubmitter", "newsubmitter@game.local", "password123");
    }

    @Test
    void submitAnswer_WithoutPlayerId_UsesDefault() throws Exception {
        // Given
        AnswerSubmissionRequest request = new AnswerSubmissionRequest(
            "我爱中国",
            50,
            0
        );
        SentenceGameService.SentenceGameResult mockResult = createMockGameResult(95, 50, 0);

        when(sentenceGameService.submitAnswer(1L, "我爱中国", 50, 0))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/sentence-game/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").value(95));

        verify(sentenceGameService).submitAnswer(1L, "我爱中国", 50, 0);
    }

    @Test
    void submitAnswer_ServiceThrowsException_ReturnsBadRequest() throws Exception {
        // Given
        AnswerSubmissionRequest request = new AnswerSubmissionRequest(
            "错误答案",
            100,
            0
        );

        when(sentenceGameService.submitAnswer(1L, "错误答案", 100, 0))
            .thenThrow(new RuntimeException("No active game session"));

        // When & Then
        mockMvc.perform(post("/api/sentence-game/submit")
                .param("playerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("No active game session"));
    }

    @Test
    void submitAnswer_AutoCreatePlayerFails_ReturnsBadRequest() throws Exception {
        // Given
        AnswerSubmissionRequest request = new AnswerSubmissionRequest(
            "测试",
            40,
            0
        );

        when(playerService.getPlayerByUsername("failsubmit"))
            .thenReturn(Optional.empty());
        when(playerService.registerPlayer("failsubmit", "failsubmit@game.local", "password123"))
            .thenThrow(new IllegalArgumentException("Registration failed"));

        // When & Then
        mockMvc.perform(post("/api/sentence-game/submit")
                .param("playerId", "failsubmit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Failed to create player: failsubmit"));

        verify(sentenceGameService, never()).submitAnswer(anyLong(), anyString(), anyInt(), anyInt());
    }

    // ========================================================================
    // Tests: getHint
    // ========================================================================

    @Test
    void getHint_WithNumericPlayerId_Success() throws Exception {
        // Given
        SentenceGameService.SentenceHint mockHint = createMockHint(1);

        when(sentenceGameService.getHint(1L, 1))
            .thenReturn(mockHint);

        // When & Then
        mockMvc.perform(post("/api/sentence-game/hint/1")
                .param("playerId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.level").value(1))
            .andExpect(jsonPath("$.penalty").value(10));

        verify(sentenceGameService).getHint(1L, 1);
    }

    @Test
    void getHint_WithUsername_ResolvesToPlayerId() throws Exception {
        // Given
        Player mockPlayer = createPlayer(4L, "hintuser");
        SentenceGameService.SentenceHint mockHint = createMockHint(2);

        when(playerService.getPlayerByUsername("hintuser"))
            .thenReturn(Optional.of(mockPlayer));
        when(sentenceGameService.getHint(4L, 2))
            .thenReturn(mockHint);

        // When & Then
        mockMvc.perform(post("/api/sentence-game/hint/2")
                .param("playerId", "hintuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.level").value(2));

        verify(playerService).getPlayerByUsername("hintuser");
        verify(sentenceGameService).getHint(4L, 2);
    }

    @Test
    void getHint_AutoCreatePlayer_Success() throws Exception {
        // Given
        Player newPlayer = createPlayer(8L, "newhinter");
        SentenceGameService.SentenceHint mockHint = createMockHint(3);

        when(playerService.getPlayerByUsername("newhinter"))
            .thenReturn(Optional.empty());
        when(playerService.registerPlayer("newhinter", "newhinter@game.local", "password123"))
            .thenReturn(newPlayer);
        when(sentenceGameService.getHint(8L, 3))
            .thenReturn(mockHint);

        // When & Then
        mockMvc.perform(post("/api/sentence-game/hint/3")
                .param("playerId", "newhinter"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.level").value(3));

        verify(playerService).registerPlayer("newhinter", "newhinter@game.local", "password123");
    }

    @Test
    void getHint_WithoutPlayerId_UsesDefault() throws Exception {
        // Given
        SentenceGameService.SentenceHint mockHint = createMockHint(1);

        when(sentenceGameService.getHint(1L, 1))
            .thenReturn(mockHint);

        // When & Then
        mockMvc.perform(post("/api/sentence-game/hint/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.level").value(1));

        verify(sentenceGameService).getHint(1L, 1);
    }

    @Test
    void getHint_ServiceThrowsException_ReturnsBadRequest() throws Exception {
        // Given
        when(sentenceGameService.getHint(1L, 1))
            .thenThrow(new RuntimeException("Maximum hints reached"));

        // When & Then
        mockMvc.perform(post("/api/sentence-game/hint/1")
                .param("playerId", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Maximum hints reached"));
    }

    @Test
    void getHint_AutoCreatePlayerFails_ReturnsBadRequest() throws Exception {
        // Given
        when(playerService.getPlayerByUsername("failhint"))
            .thenReturn(Optional.empty());
        when(playerService.registerPlayer("failhint", "failhint@game.local", "password123"))
            .thenThrow(new IllegalArgumentException("Creation error"));

        // When & Then
        mockMvc.perform(post("/api/sentence-game/hint/1")
                .param("playerId", "failhint"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Failed to create player: failhint"));

        verify(sentenceGameService, never()).getHint(anyLong(), anyInt());
    }

    // ========================================================================
    // Tests: getHistory
    // ========================================================================

    @Test
    void getHistory_ReturnsPlayerHistory() throws Exception {
        // Given
        SentenceScore score1 = createMockScore();
        SentenceScore score2 = createMockScore();

        when(sentenceGameService.getPlayerHistory(5L, 10))
            .thenReturn(List.of(score1, score2));

        // When & Then
        mockMvc.perform(get("/api/sentence-game/history/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2));

        verify(sentenceGameService).getPlayerHistory(5L, 10);
    }

    @Test
    void getHistory_EmptyHistory_ReturnsEmptyArray() throws Exception {
        // Given
        when(sentenceGameService.getPlayerHistory(99L, 10))
            .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/sentence-game/history/99"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));

        verify(sentenceGameService).getPlayerHistory(99L, 10);
    }

    // ========================================================================
    // Tests: restartQuiz
    // ========================================================================

    @Test
    void restartQuiz_WithNumericPlayerId_Success() throws Exception {
        // Given
        doNothing().when(sentenceGameService).restartQuiz(6L);

        // When & Then
        mockMvc.perform(post("/api/sentence-game/restart")
                .param("playerId", "6"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("造句题目已重置，您可以重新开始挑战所有题目！"));

        verify(sentenceGameService).restartQuiz(6L);
    }

    @Test
    void restartQuiz_WithUsername_ResolvesToPlayerId() throws Exception {
        // Given
        Player mockPlayer = createPlayer(9L, "restartuser");

        when(playerService.getPlayerByUsername("restartuser"))
            .thenReturn(Optional.of(mockPlayer));
        doNothing().when(sentenceGameService).restartQuiz(9L);

        // When & Then
        mockMvc.perform(post("/api/sentence-game/restart")
                .param("playerId", "restartuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(playerService).getPlayerByUsername("restartuser");
        verify(sentenceGameService).restartQuiz(9L);
    }

    @Test
    void restartQuiz_PlayerNotFound_ReturnsBadRequest() throws Exception {
        // Given
        when(playerService.getPlayerByUsername("nonexistent"))
            .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/sentence-game/restart")
                .param("playerId", "nonexistent"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Player not found: nonexistent"));

        verify(sentenceGameService, never()).restartQuiz(anyLong());
    }

    @Test
    void restartQuiz_ServiceThrowsException_ReturnsBadRequest() throws Exception {
        // Given
        doThrow(new RuntimeException("Database error"))
            .when(sentenceGameService).restartQuiz(1L);

        // When & Then
        mockMvc.perform(post("/api/sentence-game/restart")
                .param("playerId", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Database error"));
    }
}
