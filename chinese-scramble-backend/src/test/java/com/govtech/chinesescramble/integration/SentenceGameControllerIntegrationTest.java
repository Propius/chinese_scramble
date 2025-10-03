package com.govtech.chinesescramble.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govtech.chinesescramble.dto.request.AnswerSubmissionRequest;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.UserRole;
import com.govtech.chinesescramble.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for SentenceGameController
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SentenceGameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlayerRepository playerRepository;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        testPlayer = Player.builder()
            .username("sentencegamer")
            .email("sentence@test.com")
            .passwordHash("hashedpassword")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        testPlayer.setCreatedAt(now);
        testPlayer.setUpdatedAt(now);
        testPlayer = playerRepository.save(testPlayer);
    }

    @Test
    void testStartGame_Success() throws Exception {
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "EASY")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scrambledWords").isArray())
            .andExpect(jsonPath("$.difficulty").value("EASY"));
    }

    @Test
    void testStartGame_WithNumericPlayerId() throws Exception {
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "MEDIUM")
                .param("playerId", testPlayer.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scrambledWords").isArray())
            .andExpect(jsonPath("$.difficulty").value("MEDIUM"));
    }

    @Test
    void testStartGame_AutoCreatePlayer() throws Exception {
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "EASY")
                .param("playerId", "newsentenceuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scrambledWords").isArray());
    }

    @Test
    void testStartGame_WithoutPlayerId() throws Exception {
        // Without playerId, controller uses default player ID (1L) which may not exist
        // Expect either success or bad request
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "HARD"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testStartGame_InvalidDifficulty() throws Exception {
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "INVALID")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testSubmitAnswer() throws Exception {
        // Start game first
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "EASY")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isOk());

        // Submit answer
        AnswerSubmissionRequest answerRequest = new AnswerSubmissionRequest(
            "我喜欢学习中文",
            60,
            0
        );

        mockMvc.perform(post("/api/sentence-game/submit")
                .param("playerId", testPlayer.getUsername())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)))
            .andExpect(status().isOk());
    }

    @Test
    void testSubmitAnswer_WithNumericPlayerId() throws Exception {
        // Start game first
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "EASY")
                .param("playerId", testPlayer.getId().toString()))
            .andExpect(status().isOk());

        // Submit answer with numeric ID
        AnswerSubmissionRequest answerRequest = new AnswerSubmissionRequest(
            "今天天气很好",
            45,
            1
        );

        mockMvc.perform(post("/api/sentence-game/submit")
                .param("playerId", testPlayer.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)))
            .andExpect(status().isOk());
    }

    @Test
    void testSubmitAnswer_WithoutPlayerId() throws Exception {
        // Without playerId, expects error since default player may not exist
        AnswerSubmissionRequest answerRequest = new AnswerSubmissionRequest(
            "我爱中国",
            30,
            0
        );

        mockMvc.perform(post("/api/sentence-game/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testGetHint() throws Exception {
        // Start game first
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "MEDIUM")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isOk());

        // Get hint
        mockMvc.perform(post("/api/sentence-game/hint/1")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isOk());
    }

    @Test
    void testGetHint_WithNumericPlayerId() throws Exception {
        // Start game first
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "EASY")
                .param("playerId", testPlayer.getId().toString()))
            .andExpect(status().isOk());

        // Get hint with numeric ID
        mockMvc.perform(post("/api/sentence-game/hint/2")
                .param("playerId", testPlayer.getId().toString()))
            .andExpect(status().isOk());
    }

    @Test
    void testGetHint_WithoutPlayerId() throws Exception {
        // Without playerId, expects error since default player may not exist
        mockMvc.perform(post("/api/sentence-game/hint/1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testGetHistory() throws Exception {
        mockMvc.perform(get("/api/sentence-game/history/" + testPlayer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testRestartQuiz_WithUsername() throws Exception {
        mockMvc.perform(post("/api/sentence-game/restart")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testRestartQuiz_WithNumericId() throws Exception {
        mockMvc.perform(post("/api/sentence-game/restart")
                .param("playerId", testPlayer.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testRestartQuiz_PlayerNotFound() throws Exception {
        mockMvc.perform(post("/api/sentence-game/restart")
                .param("playerId", "nonexistentuser999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testMultipleHintLevels() throws Exception {
        // Start game
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "HARD")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isOk());

        // Request hint level 1
        mockMvc.perform(post("/api/sentence-game/hint/1")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isOk());

        // Request hint level 2
        mockMvc.perform(post("/api/sentence-game/hint/2")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isOk());

        // Request hint level 3
        mockMvc.perform(post("/api/sentence-game/hint/3")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isOk());
    }

    @Test
    void testFullGameFlow() throws Exception {
        // 1. Start game
        mockMvc.perform(get("/api/sentence-game/start")
                .param("difficulty", "EASY")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scrambledWords").isArray());

        // 2. Get hint
        mockMvc.perform(post("/api/sentence-game/hint/1")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isOk());

        // 3. Submit answer
        AnswerSubmissionRequest answerRequest = new AnswerSubmissionRequest(
            "我很高兴",
            50,
            1
        );

        mockMvc.perform(post("/api/sentence-game/submit")
                .param("playerId", testPlayer.getUsername())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)))
            .andExpect(status().isOk());

        // 4. Check history
        mockMvc.perform(get("/api/sentence-game/history/" + testPlayer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());

        // 5. Restart quiz
        mockMvc.perform(post("/api/sentence-game/restart")
                .param("playerId", testPlayer.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
