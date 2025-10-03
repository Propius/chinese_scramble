package com.govtech.chinesescramble.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govtech.chinesescramble.dto.request.AnswerSubmissionRequest;
import com.govtech.chinesescramble.entity.IdiomScore;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.UserRole;
import com.govtech.chinesescramble.service.IdiomGameService;
import com.govtech.chinesescramble.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for IdiomGameController
 * Tests controller layer including request/response handling, validation, and error scenarios
 *
 * Coverage targets:
 * - Valid request handling
 * - Invalid parameter handling
 * - Error response handling
 * - Player resolution (ID vs username)
 * - Auto-creation logic
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@WebMvcTest(controllers = IdiomGameController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
    })
class IdiomGameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IdiomGameService idiomGameService;

    @MockBean
    private PlayerService playerService;

    // ========================================================================
    // Test Helper Methods
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

    private IdiomGameService.IdiomGameState createMockGameState() {
        return new IdiomGameService.IdiomGameState(
            List.of("一", "心", "意", "一"),
            4,
            120,
            DifficultyLevel.EASY,
            "wholehearted; concentrated",
            "yī xīn yī yì"
        );
    }

    private IdiomGameService.IdiomGameResult createMockGameResult(int score, int timeTaken, int hintsUsed) {
        return new IdiomGameService.IdiomGameResult(
            true,
            "一心一意",
            "一心一意",
            score,
            100.0,
            timeTaken,
            hintsUsed,
            List.of(),
            "wholehearted",
            "concentrated; single-minded",
            "Example usage",
            "yī xīn yī yì"
        );
    }

    // ========================================================================
    // startGame() Tests
    // ========================================================================

    @Test
    void startGame_ValidRequest_Returns200() throws Exception {
        // Given
        IdiomGameService.IdiomGameState mockGameState = createMockGameState();
        when(idiomGameService.startGame(1L, DifficultyLevel.EASY))
            .thenReturn(mockGameState);

        // When & Then
        mockMvc.perform(get("/api/idiom-game/start")
                .param("difficulty", "EASY")
                .param("playerId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scrambledCharacters").isArray())
            .andExpect(jsonPath("$.characterCount").value(4));

        verify(idiomGameService).startGame(1L, DifficultyLevel.EASY);
    }

    @Test
    void startGame_WithUsername_ResolvesToPlayerId() throws Exception {
        // Given
        Player mockPlayer = createPlayer(5L, "testuser");
        IdiomGameService.IdiomGameState mockGameState = createMockGameState();

        when(playerService.getPlayerByUsername("testuser"))
            .thenReturn(Optional.of(mockPlayer));
        when(idiomGameService.startGame(5L, DifficultyLevel.MEDIUM))
            .thenReturn(mockGameState);

        // When & Then
        mockMvc.perform(get("/api/idiom-game/start")
                .param("difficulty", "MEDIUM")
                .param("playerId", "testuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.characterCount").value(4));

        verify(playerService).getPlayerByUsername("testuser");
        verify(idiomGameService).startGame(5L, DifficultyLevel.MEDIUM);
    }

    @Test
    void startGame_NewUsername_AutoCreatesPlayer() throws Exception {
        // Given
        Player newPlayer = createPlayer(10L, "newuser");
        IdiomGameService.IdiomGameState mockGameState = createMockGameState();

        when(playerService.getPlayerByUsername("newuser"))
            .thenReturn(Optional.empty());
        when(playerService.registerPlayer("newuser", "newuser@game.local", "password123"))
            .thenReturn(newPlayer);
        when(idiomGameService.startGame(10L, DifficultyLevel.EASY))
            .thenReturn(mockGameState);

        // When & Then
        mockMvc.perform(get("/api/idiom-game/start")
                .param("difficulty", "EASY")
                .param("playerId", "newuser"))
            .andExpect(status().isOk());

        verify(playerService).getPlayerByUsername("newuser");
        verify(playerService).registerPlayer("newuser", "newuser@game.local", "password123");
        verify(idiomGameService).startGame(10L, DifficultyLevel.EASY);
    }

    @Test
    void startGame_InvalidDifficulty_Returns400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/idiom-game/start")
                .param("difficulty", "INVALID")
                .param("playerId", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());

        verify(idiomGameService, never()).startGame(anyLong(), any());
    }

    @Test
    void startGame_NoPlayerId_UsesDefault() throws Exception {
        // Given
        IdiomGameService.IdiomGameState mockGameState = createMockGameState();

        when(idiomGameService.startGame(1L, DifficultyLevel.HARD))
            .thenReturn(mockGameState);

        // When & Then
        mockMvc.perform(get("/api/idiom-game/start")
                .param("difficulty", "HARD"))
            .andExpect(status().isOk());

        verify(idiomGameService).startGame(1L, DifficultyLevel.HARD);
    }

    @Test
    void startGame_ServiceThrowsException_Returns500() throws Exception {
        // Given
        when(idiomGameService.startGame(anyLong(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/idiom-game/start")
                .param("difficulty", "EASY")
                .param("playerId", "1"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("Database connection failed"));
    }

    @Test
    void startGame_PlayerCreationFails_Returns400() throws Exception {
        // Given
        when(playerService.getPlayerByUsername("baduser"))
            .thenReturn(Optional.empty());
        when(playerService.registerPlayer("baduser", "baduser@game.local", "password123"))
            .thenThrow(new IllegalArgumentException("Invalid username format"));

        // When & Then
        mockMvc.perform(get("/api/idiom-game/start")
                .param("difficulty", "EASY")
                .param("playerId", "baduser"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Failed to create player: baduser"));
    }

    // ========================================================================
    // submitAnswer() Tests
    // ========================================================================

    @Test
    void submitAnswer_ValidRequest_Returns200() throws Exception {
        // Given
        AnswerSubmissionRequest request = new AnswerSubmissionRequest("一心一意", 45, 0);
        IdiomGameService.IdiomGameResult mockResult = createMockGameResult(100, 45, 0);

        when(idiomGameService.submitAnswer(1L, "一心一意", 45, 0))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/idiom-game/submit")
                .param("playerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isCorrect").value(true))
            .andExpect(jsonPath("$.score").value(100));

        verify(idiomGameService).submitAnswer(1L, "一心一意", 45, 0);
    }

    @Test
    void submitAnswer_WithUsername_ResolvesToPlayerId() throws Exception {
        // Given
        Player mockPlayer = createPlayer(7L, "player7");

        AnswerSubmissionRequest request = new AnswerSubmissionRequest("一心一意", 50, 1);
        IdiomGameService.IdiomGameResult mockResult = createMockGameResult(90, 50, 1);

        when(playerService.getPlayerByUsername("player7"))
            .thenReturn(Optional.of(mockPlayer));
        when(idiomGameService.submitAnswer(7L, "一心一意", 50, 1))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/idiom-game/submit")
                .param("playerId", "player7")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").value(90));

        verify(playerService).getPlayerByUsername("player7");
        verify(idiomGameService).submitAnswer(7L, "一心一意", 50, 1);
    }

    @Test
    void submitAnswer_NoPlayerId_UsesDefault() throws Exception {
        // Given
        AnswerSubmissionRequest request = new AnswerSubmissionRequest("一心一意", 60, 2);
        IdiomGameService.IdiomGameResult mockResult = createMockGameResult(80, 60, 2);

        when(idiomGameService.submitAnswer(1L, "一心一意", 60, 2))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/idiom-game/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(idiomGameService).submitAnswer(1L, "一心一意", 60, 2);
    }

    @Test
    void submitAnswer_ServiceThrowsException_Returns400() throws Exception {
        // Given
        AnswerSubmissionRequest request = new AnswerSubmissionRequest("错误答案", 30, 0);
        when(idiomGameService.submitAnswer(anyLong(), anyString(), anyInt(), anyInt()))
            .thenThrow(new IllegalStateException("No active game session"));

        // When & Then
        mockMvc.perform(post("/api/idiom-game/submit")
                .param("playerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("No active game session"));
    }

    // ========================================================================
    // getHint() Tests
    // ========================================================================

    @Test
    void getHint_ValidRequest_Returns200() throws Exception {
        // Given
        IdiomGameService.IdiomHint mockHint = new IdiomGameService.IdiomHint(
            1,
            "💡 This idiom means wholehearted",
            10
        );

        when(idiomGameService.getHint(1L, 1))
            .thenReturn(mockHint);

        // When & Then
        mockMvc.perform(post("/api/idiom-game/hint/1")
                .param("playerId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.level").value(1))
            .andExpect(jsonPath("$.content").value("💡 This idiom means wholehearted"))
            .andExpect(jsonPath("$.penalty").value(10));

        verify(idiomGameService).getHint(1L, 1);
    }

    @Test
    void getHint_WithUsername_ResolvesToPlayerId() throws Exception {
        // Given
        Player mockPlayer = createPlayer(3L, "hintuser");

        IdiomGameService.IdiomHint mockHint = new IdiomGameService.IdiomHint(
            2,
            "🔤 First character: 一",
            20
        );

        when(playerService.getPlayerByUsername("hintuser"))
            .thenReturn(Optional.of(mockPlayer));
        when(idiomGameService.getHint(3L, 2))
            .thenReturn(mockHint);

        // When & Then
        mockMvc.perform(post("/api/idiom-game/hint/2")
                .param("playerId", "hintuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.level").value(2));

        verify(idiomGameService).getHint(3L, 2);
    }

    @Test
    void getHint_NoPlayerId_UsesDefault() throws Exception {
        // Given
        IdiomGameService.IdiomHint mockHint = new IdiomGameService.IdiomHint(
            3,
            "📝 First half: 一心",
            30
        );

        when(idiomGameService.getHint(1L, 3))
            .thenReturn(mockHint);

        // When & Then
        mockMvc.perform(post("/api/idiom-game/hint/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.level").value(3));

        verify(idiomGameService).getHint(1L, 3);
    }

    @Test
    void getHint_ServiceThrowsException_Returns400() throws Exception {
        // Given
        when(idiomGameService.getHint(anyLong(), anyInt()))
            .thenThrow(new IllegalStateException("Maximum hints already used"));

        // When & Then
        mockMvc.perform(post("/api/idiom-game/hint/1")
                .param("playerId", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Maximum hints already used"));
    }

    // ========================================================================
    // getHistory() Tests
    // ========================================================================

    @Test
    void getHistory_ValidRequest_Returns200() throws Exception {
        // Given
        Player mockPlayer = createPlayer(1L, "testuser");

        IdiomScore score1 = IdiomScore.builder()
            .player(mockPlayer)
            .idiom("一心一意")
            .score(100)
            .build();
        ReflectionTestUtils.setField(score1, "id", 1L);
        ReflectionTestUtils.setField(score1, "createdAt", LocalDateTime.now());

        IdiomScore score2 = IdiomScore.builder()
            .player(mockPlayer)
            .idiom("三心二意")
            .score(85)
            .build();
        ReflectionTestUtils.setField(score2, "id", 2L);
        ReflectionTestUtils.setField(score2, "createdAt", LocalDateTime.now().minusDays(1));

        List<IdiomScore> mockHistory = List.of(score1, score2);

        when(idiomGameService.getPlayerHistory(1L, 10))
            .thenReturn(mockHistory);

        // When & Then
        mockMvc.perform(get("/api/idiom-game/history/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2));

        verify(idiomGameService).getPlayerHistory(1L, 10);
    }

    // ========================================================================
    // restartQuiz() Tests
    // ========================================================================

    @Test
    void restartQuiz_ValidPlayerId_Returns200() throws Exception {
        // Given
        doNothing().when(idiomGameService).restartQuiz(1L);

        // When & Then
        mockMvc.perform(post("/api/idiom-game/restart")
                .param("playerId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists());

        verify(idiomGameService).restartQuiz(1L);
    }

    @Test
    void restartQuiz_WithUsername_ResolvesToPlayerId() throws Exception {
        // Given
        Player mockPlayer = createPlayer(8L, "restartuser");

        when(playerService.getPlayerByUsername("restartuser"))
            .thenReturn(Optional.of(mockPlayer));
        doNothing().when(idiomGameService).restartQuiz(8L);

        // When & Then
        mockMvc.perform(post("/api/idiom-game/restart")
                .param("playerId", "restartuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(playerService).getPlayerByUsername("restartuser");
        verify(idiomGameService).restartQuiz(8L);
    }

    @Test
    void restartQuiz_PlayerNotFound_Returns400() throws Exception {
        // Given
        when(playerService.getPlayerByUsername("unknownuser"))
            .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/idiom-game/restart")
                .param("playerId", "unknownuser"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Player not found: unknownuser"));

        verify(idiomGameService, never()).restartQuiz(anyLong());
    }

    @Test
    void restartQuiz_ServiceThrowsException_Returns400() throws Exception {
        // Given
        doThrow(new RuntimeException("Database error"))
            .when(idiomGameService).restartQuiz(1L);

        // When & Then
        mockMvc.perform(post("/api/idiom-game/restart")
                .param("playerId", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Database error"));
    }
}
