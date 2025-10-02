package com.govtech.chinesescramble.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govtech.chinesescramble.dto.request.AnswerSubmissionRequest;
import com.govtech.chinesescramble.dto.request.GameStartRequest;
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
 * Integration test for IdiomGameController
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IdiomGameControllerIntegrationTest {

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

        testPlayer = Player.builder()
            .username("idiomgamer")
            .email("idiom@test.com")
            .passwordHash("hashedpassword")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        testPlayer = playerRepository.save(testPlayer);
    }

    @Test
    void testStartGame_Success() throws Exception {
        // Given
        GameStartRequest request = new GameStartRequest(DifficultyLevel.EASY);

        // When/Then
        mockMvc.perform(post("/api/games/idiom/start")
                .param("playerId", testPlayer.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scrambledCharacters").isArray())
            .andExpect(jsonPath("$.difficulty").value("EASY"))
            .andExpect(jsonPath("$.timeLimit").exists());
    }

    @Test
    void testStartGame_PlayerNotFound() throws Exception {
        // Given
        GameStartRequest request = new GameStartRequest(DifficultyLevel.EASY);

        // When/Then
        mockMvc.perform(post("/api/games/idiom/start")
                .param("playerId", "999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testSubmitAnswer() throws Exception {
        // Given - Start game first
        GameStartRequest startRequest = new GameStartRequest(DifficultyLevel.EASY);

        mockMvc.perform(post("/api/games/idiom/start")
                .param("playerId", testPlayer.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startRequest)))
            .andExpect(status().isOk());

        // When/Then - Submit answer
        AnswerSubmissionRequest answerRequest = new AnswerSubmissionRequest(
            "一帆风顺",
            45,
            0
        );

        mockMvc.perform(post("/api/games/idiom/submit")
                .param("playerId", testPlayer.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)))
            .andExpect(status().isOk());
    }

    @Test
    void testGetHint() throws Exception {
        // Given - Start game first
        GameStartRequest startRequest = new GameStartRequest(DifficultyLevel.MEDIUM);

        mockMvc.perform(post("/api/games/idiom/start")
                .param("playerId", testPlayer.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startRequest)))
            .andExpect(status().isOk());

        // When/Then - Get hint
        mockMvc.perform(post("/api/games/idiom/hint/1")
                .param("playerId", testPlayer.getId().toString()))
            .andExpect(status().isOk());
    }

    @Test
    void testGetHistory() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/games/idiom/history/" + testPlayer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
