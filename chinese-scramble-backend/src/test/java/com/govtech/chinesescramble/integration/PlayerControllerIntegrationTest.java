package com.govtech.chinesescramble.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govtech.chinesescramble.dto.request.PlayerRegistrationRequest;
import com.govtech.chinesescramble.entity.Player;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for PlayerController
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PlayerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();
    }

    @Test
    void testRegisterPlayer_Success() throws Exception {
        // Given
        PlayerRegistrationRequest request = new PlayerRegistrationRequest(
            "newplayer",
            "newplayer@test.com",
            "password123"
        );

        // When/Then
        mockMvc.perform(post("/api/players/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("newplayer"))
            .andExpect(jsonPath("$.email").value("newplayer@test.com"))
            .andExpect(jsonPath("$.active").value(true));

        // Verify database
        assertThat(playerRepository.findByUsername("newplayer")).isPresent();
    }

    @Test
    void testRegisterPlayer_DuplicateUsername() throws Exception {
        // Given - Create existing player
        PlayerRegistrationRequest firstRequest = new PlayerRegistrationRequest(
            "existinguser",
            "first@test.com",
            "password123"
        );

        mockMvc.perform(post("/api/players/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
            .andExpect(status().isCreated());

        // When/Then - Try duplicate username
        PlayerRegistrationRequest duplicateRequest = new PlayerRegistrationRequest(
            "existinguser",
            "different@test.com",
            "password456"
        );

        mockMvc.perform(post("/api/players/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testRegisterPlayer_ValidationError() throws Exception {
        // Given - Invalid email
        PlayerRegistrationRequest request = new PlayerRegistrationRequest(
            "testuser",
            "invalid-email",
            "password123"
        );

        // When/Then
        mockMvc.perform(post("/api/players/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPlayer_Success() throws Exception {
        // Given - Register player first
        PlayerRegistrationRequest request = new PlayerRegistrationRequest(
            "getuser",
            "getuser@test.com",
            "password123"
        );

        String response = mockMvc.perform(post("/api/players/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long playerId = objectMapper.readTree(response).get("id").asLong();

        // When/Then
        mockMvc.perform(get("/api/players/" + playerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("getuser"))
            .andExpect(jsonPath("$.email").value("getuser@test.com"));
    }

    @Test
    void testGetPlayer_NotFound() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/players/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetPlayerStatistics() throws Exception {
        // Given - Register player
        PlayerRegistrationRequest request = new PlayerRegistrationRequest(
            "statsuser",
            "stats@test.com",
            "password123"
        );

        String response = mockMvc.perform(post("/api/players/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long playerId = objectMapper.readTree(response).get("id").asLong();

        // When/Then
        mockMvc.perform(get("/api/players/" + playerId + "/statistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalGames").exists())
            .andExpect(jsonPath("$.totalScore").exists())
            .andExpect(jsonPath("$.overallAccuracy").exists());
    }
}
