package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.GameSession;
import com.govtech.chinesescramble.entity.HintUsage;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.entity.enums.SessionStatus;
import com.govtech.chinesescramble.repository.GameSessionRepository;
import com.govtech.chinesescramble.repository.PlayerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GameSessionService - Manages game session lifecycle
 *
 * Features:
 * - Create new game sessions
 * - Track active sessions (one per player)
 * - Complete/abandon sessions
 * - Auto-expire inactive sessions (30+ minutes)
 * - Hint usage tracking
 * - Session state persistence (JSON)
 * - Resume game capability
 *
 * Session Lifecycle:
 * 1. ACTIVE: Player starts game → session created
 * 2. Playing: Hints may be used, state saved
 * 3. End: Three possible outcomes:
 *    a) COMPLETED: Player submits answer (score saved)
 *    b) ABANDONED: Player quits without submitting
 *    c) EXPIRED: Auto-expired after 30 minutes of inactivity
 *
 * Session Data Structure:
 * {
 *   "gameType": "IDIOM",
 *   "difficulty": "MEDIUM",
 *   "question": "井底之蛙",
 *   "scrambled": ["蛙", "之", "底", "井"],
 *   "allowedChars": ["井", "底", "之", "蛙"],
 *   "timeRemaining": 120,
 *   "startedAt": "2025-01-30T12:00:00"
 * }
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final PlayerRepository playerRepository;
    private final ObjectMapper objectMapper;

    private static final int SESSION_TIMEOUT_MINUTES = 30;

    /**
     * Creates a new game session
     * Ensures only one active session per player
     *
     * @param playerId player ID
     * @param gameType game type
     * @param difficulty difficulty level
     * @param sessionData initial session state (JSON)
     * @return created session
     */
    public GameSession createSession(
        Long playerId,
        GameType gameType,
        DifficultyLevel difficulty,
        String sessionData
    ) {
        log.info("Creating session: player={}, gameType={}, difficulty={}",
            playerId, gameType, difficulty);

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        // Check for existing active session
        Optional<GameSession> existingSession = gameSessionRepository
            .findActiveSessionByPlayer(playerId);

        if (existingSession.isPresent()) {
            log.warn("Player {} already has an active session, abandoning it", playerId);
            abandonSession(existingSession.get().getId());
        }

        // Create new session
        GameSession session = GameSession.builder()
            .player(player)
            .gameType(gameType)
            .difficulty(difficulty)
            .status(SessionStatus.ACTIVE)
            .startedAt(LocalDateTime.now())
            .sessionData(sessionData)
            .build();

        GameSession saved = gameSessionRepository.save(session);
        log.info("Session created: id={}", saved.getId());

        return saved;
    }

    /**
     * Gets active session for a player
     *
     * @param playerId player ID
     * @return Optional containing active session
     */
    @Transactional(readOnly = true)
    public Optional<GameSession> getActiveSession(Long playerId) {
        return gameSessionRepository.findActiveSessionByPlayer(playerId);
    }

    /**
     * Updates session state (for resume capability)
     *
     * @param sessionId session ID
     * @param sessionData updated state (JSON)
     * @return updated session
     */
    public GameSession updateSessionState(Long sessionId, String sessionData) {
        log.debug("Updating session state: sessionId={}", sessionId);

        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.isActive()) {
            throw new IllegalStateException("Cannot update inactive session: " + sessionId);
        }

        session.setSessionData(sessionData);
        return gameSessionRepository.save(session);
    }

    /**
     * Completes a session with score
     *
     * @param sessionId session ID
     * @param score final score
     * @return completed session
     */
    public GameSession completeSession(Long sessionId, Integer score) {
        log.info("Completing session: sessionId={}, score={}", sessionId, score);

        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.isActive()) {
            throw new IllegalStateException("Session is not active: " + sessionId);
        }

        session.complete(score);
        GameSession saved = gameSessionRepository.save(session);

        log.info("Session completed: id={}, score={}, duration={}s",
            saved.getId(), score, saved.getDurationSeconds());

        return saved;
    }

    /**
     * Abandons a session (player quit)
     *
     * @param sessionId session ID
     * @return abandoned session
     */
    public GameSession abandonSession(Long sessionId) {
        log.info("Abandoning session: sessionId={}", sessionId);

        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.isActive()) {
            log.warn("Attempting to abandon non-active session: {}", sessionId);
            return session;
        }

        session.abandon();
        return gameSessionRepository.save(session);
    }

    /**
     * Adds a hint usage to session
     *
     * @param sessionId session ID
     * @param hintLevel hint level (1-3)
     * @param penaltyApplied penalty points
     * @param hintContent hint content (Chinese)
     * @return updated session
     */
    public GameSession addHintUsage(
        Long sessionId,
        Integer hintLevel,
        Integer penaltyApplied,
        String hintContent
    ) {
        log.info("Adding hint usage: session={}, level={}, penalty={}",
            sessionId, hintLevel, penaltyApplied);

        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.isActive()) {
            throw new IllegalStateException("Cannot add hint to inactive session: " + sessionId);
        }

        // Check hint limit (max 3 hints per session)
        if (session.getHintCount() >= 3) {
            throw new IllegalStateException("Maximum hints (3) already used in session: " + sessionId);
        }

        HintUsage hintUsage = HintUsage.builder()
            .gameSession(session)
            .hintLevel(hintLevel)
            .penaltyApplied(penaltyApplied)
            .usedAt(LocalDateTime.now())
            .hintContent(hintContent)
            .build();

        session.addHintUsage(hintUsage);
        return gameSessionRepository.save(session);
    }

    /**
     * Gets hint count for session
     *
     * @param sessionId session ID
     * @return number of hints used
     */
    @Transactional(readOnly = true)
    public int getHintCount(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        return session.getHintCount();
    }

    /**
     * Gets session history for player
     *
     * @param playerId player ID
     * @return list of sessions
     */
    @Transactional(readOnly = true)
    public List<GameSession> getPlayerSessions(Long playerId) {
        return gameSessionRepository.findByPlayerId(playerId);
    }

    /**
     * Gets completed sessions for player
     *
     * @param playerId player ID
     * @return list of completed sessions
     */
    @Transactional(readOnly = true)
    public List<GameSession> getCompletedSessions(Long playerId) {
        return gameSessionRepository.findCompletedSessionsByPlayer(playerId);
    }

    /**
     * Gets session completion statistics
     *
     * @param playerId player ID
     * @return statistics
     */
    @Transactional(readOnly = true)
    public SessionStatistics getSessionStatistics(Long playerId) {
        Optional<Object[]> stats = gameSessionRepository.getSessionCompletionStats(playerId);

        if (stats.isEmpty()) {
            return new SessionStatistics(0L, 0L, 0L, 0L, 0.0);
        }

        Object[] data = stats.get();
        long total = ((Number) data[0]).longValue();
        long completed = ((Number) data[1]).longValue();
        long abandoned = ((Number) data[2]).longValue();
        long expired = ((Number) data[3]).longValue();

        double completionRate = total > 0 ? (completed * 100.0 / total) : 0.0;

        return new SessionStatistics(total, completed, abandoned, expired, completionRate);
    }

    /**
     * Scheduled task to expire inactive sessions
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void expireInactiveSessions() {
        log.debug("Checking for inactive sessions to expire");

        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES);
        List<GameSession> staleSessions = gameSessionRepository.findStaleActiveSessions(cutoffTime);

        if (staleSessions.isEmpty()) {
            return;
        }

        int expiredCount = 0;
        for (GameSession session : staleSessions) {
            try {
                session.expire();
                gameSessionRepository.save(session);
                expiredCount++;
                log.info("Expired session: id={}, player={}, age={}min",
                    session.getId(),
                    session.getPlayer().getId(),
                    (System.currentTimeMillis() - session.getStartedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()) / 60000);
            } catch (Exception e) {
                log.error("Failed to expire session: {}", session.getId(), e);
            }
        }

        if (expiredCount > 0) {
            log.info("Expired {} inactive sessions", expiredCount);
        }
    }

    /**
     * Parses session data JSON
     *
     * @param sessionData JSON string
     * @return Map of session data
     */
    @Transactional(readOnly = true)
    public Map<String, Object> parseSessionData(String sessionData) {
        try {
            return objectMapper.readValue(sessionData, Map.class);
        } catch (Exception e) {
            log.error("Failed to parse session data", e);
            return Map.of();
        }
    }

    /**
     * Converts session data map to JSON
     *
     * @param sessionData data map
     * @return JSON string
     */
    public String sessionDataToJson(Map<String, Object> sessionData) {
        try {
            return objectMapper.writeValueAsString(sessionData);
        } catch (Exception e) {
            log.error("Failed to convert session data to JSON", e);
            return "{}";
        }
    }

    // ========================================================================
    // DTOs
    // ========================================================================

    /**
     * Session statistics
     */
    public record SessionStatistics(
        long totalSessions,
        long completedSessions,
        long abandonedSessions,
        long expiredSessions,
        double completionRate
    ) {
        public long incompleteSessions() {
            return abandonedSessions + expiredSessions;
        }

        public double abandonmentRate() {
            return totalSessions > 0 ? (abandonedSessions * 100.0 / totalSessions) : 0.0;
        }
    }
}