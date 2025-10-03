package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.GameSession;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.entity.enums.SessionStatus;
import com.govtech.chinesescramble.entity.enums.UserRole;
import com.govtech.chinesescramble.repository.GameSessionRepository;
import com.govtech.chinesescramble.repository.PlayerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Test class for GameSessionService
 * Tests cover:
 * - Session creation and lifecycle management
 * - Active session retrieval and state updates
 * - Session completion and abandonment
 * - Hint usage tracking and limits
 * - Session statistics and history
 * - JSON serialization/deserialization
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class GameSessionServiceTest {

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GameSessionService gameSessionService;

    private Player testPlayer;
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

        testSession = GameSession.builder()
            .player(testPlayer)
            .gameType(GameType.IDIOM)
            .difficulty(DifficultyLevel.EASY)
            .status(SessionStatus.ACTIVE)
            .startedAt(LocalDateTime.now())
            .sessionData("{\"idiom\":\"一帆风顺\"}")
            .build();
        ReflectionTestUtils.setField(testSession, "id", 1L);
    }

    @Test
    void testCreateSession_Success() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(gameSessionRepository.findActiveSessionByPlayer(1L)).thenReturn(Optional.empty());
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);

        // Act
        GameSession result = gameSessionService.createSession(1L, GameType.IDIOM, DifficultyLevel.EASY, "{}");

        // Assert
        assertNotNull(result);
        assertEquals(GameType.IDIOM, result.getGameType());
        assertEquals(DifficultyLevel.EASY, result.getDifficulty());
        assertEquals(SessionStatus.ACTIVE, result.getStatus());
        verify(gameSessionRepository).save(any(GameSession.class));
    }

    @Test
    void testCreateSession_PlayerNotFound() {
        // Arrange
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> gameSessionService.createSession(999L, GameType.IDIOM, DifficultyLevel.EASY, "{}"));
    }

    @Test
    void testCreateSession_AbandonExistingSession() {
        // Arrange
        GameSession existingSession = GameSession.builder()
            .player(testPlayer)
            .gameType(GameType.SENTENCE)
            .difficulty(DifficultyLevel.MEDIUM)
            .status(SessionStatus.ACTIVE)
            .startedAt(LocalDateTime.now().minusMinutes(5))
            .sessionData("{}")
            .build();
        ReflectionTestUtils.setField(existingSession, "id", 2L);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(gameSessionRepository.findActiveSessionByPlayer(1L)).thenReturn(Optional.of(existingSession));
        when(gameSessionRepository.findById(2L)).thenReturn(Optional.of(existingSession));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);

        // Act
        GameSession result = gameSessionService.createSession(1L, GameType.IDIOM, DifficultyLevel.EASY, "{}");

        // Assert
        assertNotNull(result);
        verify(gameSessionRepository).findById(2L); // Verify existing session was retrieved for abandonment
        verify(gameSessionRepository, times(2)).save(any(GameSession.class)); // Once for abandon, once for new session
    }

    @Test
    void testGetActiveSession_Found() {
        // Arrange
        when(gameSessionRepository.findActiveSessionByPlayer(1L)).thenReturn(Optional.of(testSession));

        // Act
        Optional<GameSession> result = gameSessionService.getActiveSession(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testSession.getId(), result.get().getId());
    }

    @Test
    void testGetActiveSession_NotFound() {
        // Arrange
        when(gameSessionRepository.findActiveSessionByPlayer(999L)).thenReturn(Optional.empty());

        // Act
        Optional<GameSession> result = gameSessionService.getActiveSession(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateSessionState_Success() {
        // Arrange
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);

        // Act
        GameSession result = gameSessionService.updateSessionState(1L, "{\"updated\":true}");

        // Assert
        assertNotNull(result);
        verify(gameSessionRepository).save(testSession);
    }

    @Test
    void testUpdateSessionState_SessionNotFound() {
        // Arrange
        when(gameSessionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> gameSessionService.updateSessionState(999L, "{}"));
    }

    @Test
    void testUpdateSessionState_InactiveSession() {
        // Arrange
        testSession.complete(100);
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        // Act & Assert
        assertThrows(IllegalStateException.class,
            () -> gameSessionService.updateSessionState(1L, "{}"));
    }

    @Test
    void testCompleteSession_Success() {
        // Arrange
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);

        // Act
        GameSession result = gameSessionService.completeSession(1L, 100);

        // Assert
        assertNotNull(result);
        verify(gameSessionRepository).save(testSession);
    }

    @Test
    void testCompleteSession_SessionNotFound() {
        // Arrange
        when(gameSessionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> gameSessionService.completeSession(999L, 100));
    }

    @Test
    void testCompleteSession_AlreadyCompleted() {
        // Arrange
        testSession.complete(90);
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        // Act & Assert
        assertThrows(IllegalStateException.class,
            () -> gameSessionService.completeSession(1L, 100));
    }

    @Test
    void testAbandonSession_Success() {
        // Arrange
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);

        // Act
        GameSession result = gameSessionService.abandonSession(1L);

        // Assert
        assertNotNull(result);
        verify(gameSessionRepository).save(testSession);
    }

    @Test
    void testAbandonSession_AlreadyInactive() {
        // Arrange
        testSession.complete(100);
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        // Act
        GameSession result = gameSessionService.abandonSession(1L);

        // Assert
        assertNotNull(result);
        verify(gameSessionRepository, never()).save(any(GameSession.class)); // Should not save if already inactive
    }

    @Test
    void testAddHintUsage_Success() {
        // Arrange
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);

        // Act
        GameSession result = gameSessionService.addHintUsage(1L, 1, 5, "一");

        // Assert
        assertNotNull(result);
        verify(gameSessionRepository).save(testSession);
    }

    @Test
    void testAddHintUsage_MaxHintsReached() {
        // Arrange
        testSession.addHintUsage(com.govtech.chinesescramble.entity.HintUsage.builder()
            .gameSession(testSession)
            .hintLevel(1)
            .penaltyApplied(5)
            .usedAt(LocalDateTime.now())
            .hintContent("一")
            .build());
        testSession.addHintUsage(com.govtech.chinesescramble.entity.HintUsage.builder()
            .gameSession(testSession)
            .hintLevel(2)
            .penaltyApplied(10)
            .usedAt(LocalDateTime.now())
            .hintContent("一帆")
            .build());
        testSession.addHintUsage(com.govtech.chinesescramble.entity.HintUsage.builder()
            .gameSession(testSession)
            .hintLevel(3)
            .penaltyApplied(15)
            .usedAt(LocalDateTime.now())
            .hintContent("一帆风")
            .build());

        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        // Act & Assert
        assertThrows(IllegalStateException.class,
            () -> gameSessionService.addHintUsage(1L, 1, 5, "一"));
    }

    @Test
    void testGetHintCount() {
        // Arrange
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        // Act
        int result = gameSessionService.getHintCount(1L);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void testGetPlayerSessions() {
        // Arrange
        List<GameSession> sessions = Arrays.asList(testSession);
        when(gameSessionRepository.findByPlayerId(1L)).thenReturn(sessions);

        // Act
        List<GameSession> result = gameSessionService.getPlayerSessions(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSession.getId(), result.get(0).getId());
    }

    @Test
    void testGetCompletedSessions() {
        // Arrange
        testSession.complete(100);
        List<GameSession> sessions = Arrays.asList(testSession);
        when(gameSessionRepository.findCompletedSessionsByPlayer(1L)).thenReturn(sessions);

        // Act
        List<GameSession> result = gameSessionService.getCompletedSessions(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetSessionStatistics_WithData() {
        // Arrange
        Object[] statsData = {10L, 7L, 2L, 1L};
        when(gameSessionRepository.getSessionCompletionStats(1L)).thenReturn(Optional.of(statsData));

        // Act
        GameSessionService.SessionStatistics result = gameSessionService.getSessionStatistics(1L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.totalSessions());
        assertEquals(7L, result.completedSessions());
        assertEquals(2L, result.abandonedSessions());
        assertEquals(1L, result.expiredSessions());
        assertEquals(70.0, result.completionRate(), 0.01);
    }

    @Test
    void testGetSessionStatistics_NoData() {
        // Arrange
        when(gameSessionRepository.getSessionCompletionStats(999L)).thenReturn(Optional.empty());

        // Act
        GameSessionService.SessionStatistics result = gameSessionService.getSessionStatistics(999L);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.totalSessions());
        assertEquals(0.0, result.completionRate());
    }

    @Test
    void testParseSessionData_Success() throws Exception {
        // Arrange
        Map<String, Object> expectedData = Map.of("idiom", "一帆风顺");
        when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(expectedData);

        // Act
        Map<String, Object> result = gameSessionService.parseSessionData("{\"idiom\":\"一帆风顺\"}");

        // Assert
        assertNotNull(result);
        assertEquals("一帆风顺", result.get("idiom"));
    }

    @Test
    void testParseSessionData_ParseError() throws Exception {
        // Arrange
        when(objectMapper.readValue(anyString(), eq(Map.class))).thenThrow(new RuntimeException("Parse error"));

        // Act
        Map<String, Object> result = gameSessionService.parseSessionData("invalid json");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Should return empty map on error
    }

    @Test
    void testSessionDataToJson_Success() throws Exception {
        // Arrange
        Map<String, Object> data = Map.of("idiom", "一帆风顺");
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("{\"idiom\":\"一帆风顺\"}");

        // Act
        String result = gameSessionService.sessionDataToJson(data);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("idiom"));
    }

    @Test
    void testSessionDataToJson_ConversionError() throws Exception {
        // Arrange
        Map<String, Object> data = Map.of("key", "value");
        when(objectMapper.writeValueAsString(anyMap())).thenThrow(new RuntimeException("Conversion error"));

        // Act
        String result = gameSessionService.sessionDataToJson(data);

        // Assert
        assertNotNull(result);
        assertEquals("{}", result); // Should return empty JSON on error
    }

    @Test
    void testExpireInactiveSessions() {
        // Arrange
        GameSession staleSession = GameSession.builder()
            .player(testPlayer)
            .gameType(GameType.IDIOM)
            .difficulty(DifficultyLevel.EASY)
            .status(SessionStatus.ACTIVE)
            .startedAt(LocalDateTime.now().minusMinutes(35))
            .sessionData("{}")
            .build();
        ReflectionTestUtils.setField(staleSession, "id", 2L);

        when(gameSessionRepository.findStaleActiveSessions(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(staleSession));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(staleSession);

        // Act
        gameSessionService.expireInactiveSessions();

        // Assert
        verify(gameSessionRepository).findStaleActiveSessions(any(LocalDateTime.class));
        verify(gameSessionRepository).save(staleSession);
    }

    @Test
    void testSessionStatistics_IncompleteSessions() {
        // Arrange
        GameSessionService.SessionStatistics stats = new GameSessionService.SessionStatistics(10L, 7L, 2L, 1L, 70.0);

        // Act
        long incomplete = stats.incompleteSessions();

        // Assert
        assertEquals(3L, incomplete); // 2 abandoned + 1 expired
    }

    @Test
    void testSessionStatistics_AbandonmentRate() {
        // Arrange
        GameSessionService.SessionStatistics stats = new GameSessionService.SessionStatistics(10L, 7L, 2L, 1L, 70.0);

        // Act
        double abandonmentRate = stats.abandonmentRate();

        // Assert
        assertEquals(20.0, abandonmentRate, 0.01); // 2/10 * 100
    }
}
