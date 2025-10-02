package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.GameSession;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.entity.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * GameSessionRepository - Data access layer for GameSession entity
 *
 * Provides:
 * - Active session retrieval and management
 * - Session history queries
 * - Session expiry tracking
 * - Session statistics
 *
 * Used by:
 * - GameSessionService: Create, update, complete sessions
 * - SessionCleanupScheduler: Expire inactive sessions
 * - AnalyticsService: Session statistics
 * - PlayerService: Active game tracking
 *
 * Session Lifecycle:
 * - ACTIVE: Game in progress
 * - COMPLETED: Successfully finished
 * - ABANDONED: Player quit
 * - EXPIRED: Timed out (30+ minutes inactive)
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    /**
     * Find active session for a player
     * Players can only have one active session at a time
     *
     * @param playerId the player ID
     * @return Optional containing active session
     */
    @Query("""
        SELECT gs FROM GameSession gs
        WHERE gs.player.id = :playerId
          AND gs.status = 'ACTIVE'
        ORDER BY gs.startedAt DESC
        LIMIT 1
        """)
    Optional<GameSession> findActiveSessionByPlayer(@Param("playerId") Long playerId);

    /**
     * Find all sessions for a player
     * Ordered by start date descending
     *
     * @param playerId the player ID
     * @return list of player's sessions
     */
    @Query("""
        SELECT gs FROM GameSession gs
        WHERE gs.player.id = :playerId
        ORDER BY gs.startedAt DESC
        """)
    List<GameSession> findByPlayerId(@Param("playerId") Long playerId);

    /**
     * Find sessions by status
     * Used for session management and cleanup
     *
     * @param status the session status
     * @return list of sessions with status
     */
    List<GameSession> findByStatus(SessionStatus status);

    /**
     * Find active sessions older than threshold
     * Used by scheduled job to expire inactive sessions
     *
     * @param cutoffTime the time threshold (e.g., 30 minutes ago)
     * @return list of stale active sessions
     */
    @Query("""
        SELECT gs FROM GameSession gs
        WHERE gs.status = 'ACTIVE'
          AND gs.startedAt < :cutoffTime
        ORDER BY gs.startedAt ASC
        """)
    List<GameSession> findStaleActiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find completed sessions for a player
     * Used for session history
     *
     * @param playerId the player ID
     * @return list of completed sessions
     */
    @Query("""
        SELECT gs FROM GameSession gs
        WHERE gs.player.id = :playerId
          AND gs.status = 'COMPLETED'
        ORDER BY gs.completedAt DESC
        """)
    List<GameSession> findCompletedSessionsByPlayer(@Param("playerId") Long playerId);

    /**
     * Find sessions by game type and difficulty
     * Used for game type specific statistics
     *
     * @param playerId the player ID
     * @param gameType the game type
     * @param difficulty the difficulty level
     * @return list of matching sessions
     */
    @Query("""
        SELECT gs FROM GameSession gs
        WHERE gs.player.id = :playerId
          AND gs.gameType = :gameType
          AND gs.difficulty = :difficulty
        ORDER BY gs.startedAt DESC
        """)
    List<GameSession> findByPlayerIdAndGameTypeAndDifficulty(
        @Param("playerId") Long playerId,
        @Param("gameType") GameType gameType,
        @Param("difficulty") DifficultyLevel difficulty
    );

    /**
     * Count active sessions across all players
     * Used for server load monitoring
     *
     * @return count of active sessions
     */
    long countByStatus(SessionStatus status);

    /**
     * Count sessions for a player by status
     * Used for player statistics
     *
     * @param playerId the player ID
     * @param status the session status
     * @return count of sessions
     */
    @Query("""
        SELECT COUNT(gs) FROM GameSession gs
        WHERE gs.player.id = :playerId
          AND gs.status = :status
        """)
    long countByPlayerIdAndStatus(
        @Param("playerId") Long playerId,
        @Param("status") SessionStatus status
    );

    /**
     * Find sessions in date range
     * Used for analytics
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of sessions in range
     */
    @Query("""
        SELECT gs FROM GameSession gs
        WHERE gs.startedAt BETWEEN :startDate AND :endDate
        ORDER BY gs.startedAt DESC
        """)
    List<GameSession> findSessionsInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get session completion rate for a player
     * Used for player engagement metrics
     *
     * @param playerId the player ID
     * @return object array [totalSessions, completedCount, abandonedCount, expiredCount]
     */
    @Query("""
        SELECT COUNT(gs.id),
               SUM(CASE WHEN gs.status = 'COMPLETED' THEN 1 ELSE 0 END),
               SUM(CASE WHEN gs.status = 'ABANDONED' THEN 1 ELSE 0 END),
               SUM(CASE WHEN gs.status = 'EXPIRED' THEN 1 ELSE 0 END)
        FROM GameSession gs
        WHERE gs.player.id = :playerId
        """)
    Optional<Object[]> getSessionCompletionStats(@Param("playerId") Long playerId);

    /**
     * Find sessions with high hint usage (3 hints)
     * Used for difficulty analysis
     *
     * @param playerId the player ID
     * @return list of sessions with max hints
     */
    @Query("""
        SELECT gs FROM GameSession gs
        WHERE gs.player.id = :playerId
          AND SIZE(gs.hintsUsed) >= 3
        ORDER BY gs.startedAt DESC
        """)
    List<GameSession> findHighHintUsageSessions(@Param("playerId") Long playerId);

    /**
     * Find sessions with no hints used
     * Used for HINT_FREE achievement tracking
     *
     * @param playerId the player ID
     * @return list of hint-free sessions
     */
    @Query("""
        SELECT gs FROM GameSession gs
        WHERE gs.player.id = :playerId
          AND gs.status = 'COMPLETED'
          AND SIZE(gs.hintsUsed) = 0
        ORDER BY gs.startedAt DESC
        """)
    List<GameSession> findHintFreeSessions(@Param("playerId") Long playerId);

    /**
     * Calculate average session duration for player
     * Used for player statistics
     *
     * @param playerId the player ID
     * @return average duration in seconds
     */
    @Query("""
        SELECT AVG(TIMESTAMPDIFF(SECOND, gs.startedAt, gs.completedAt))
        FROM GameSession gs
        WHERE gs.player.id = :playerId
          AND gs.status = 'COMPLETED'
          AND gs.completedAt IS NOT NULL
        """)
    Optional<Double> calculateAverageSessionDuration(@Param("playerId") Long playerId);

    /**
     * Find most recent session for player
     * Used for "continue game" feature
     *
     * @param playerId the player ID
     * @return Optional containing most recent session
     */
    @Query("""
        SELECT gs FROM GameSession gs
        WHERE gs.player.id = :playerId
        ORDER BY gs.startedAt DESC
        LIMIT 1
        """)
    Optional<GameSession> findMostRecentSession(@Param("playerId") Long playerId);

    /**
     * Get daily session counts for player
     * Used for CONSISTENCY achievement tracking
     *
     * @param playerId the player ID
     * @param startDate the start date
     * @return list of object arrays [date, sessionCount]
     */
    @Query("""
        SELECT CAST(gs.startedAt AS DATE), COUNT(gs.id)
        FROM GameSession gs
        WHERE gs.player.id = :playerId
          AND gs.startedAt >= :startDate
          AND gs.status = 'COMPLETED'
        GROUP BY CAST(gs.startedAt AS DATE)
        ORDER BY CAST(gs.startedAt AS DATE) ASC
        """)
    List<Object[]> getDailySessionCounts(
        @Param("playerId") Long playerId,
        @Param("startDate") LocalDateTime startDate
    );

    /**
     * Find sessions by game type
     * Used for game type statistics
     *
     * @param gameType the game type
     * @return list of sessions
     */
    List<GameSession> findByGameType(GameType gameType);

    /**
     * Get game type distribution for player
     * Used for player preferences
     *
     * @param playerId the player ID
     * @return list of object arrays [gameType, sessionCount]
     */
    @Query("""
        SELECT gs.gameType, COUNT(gs.id)
        FROM GameSession gs
        WHERE gs.player.id = :playerId
        GROUP BY gs.gameType
        ORDER BY COUNT(gs.id) DESC
        """)
    List<Object[]> getGameTypeDistribution(@Param("playerId") Long playerId);

    /**
     * Get difficulty distribution for player
     * Used for player skill assessment
     *
     * @param playerId the player ID
     * @return list of object arrays [difficulty, sessionCount]
     */
    @Query("""
        SELECT gs.difficulty, COUNT(gs.id)
        FROM GameSession gs
        WHERE gs.player.id = :playerId
        GROUP BY gs.difficulty
        ORDER BY COUNT(gs.id) DESC
        """)
    List<Object[]> getDifficultyDistribution(@Param("playerId") Long playerId);

    /**
     * Find abandoned sessions for re-engagement
     * Used for player re-engagement campaigns
     *
     * @param playerId the player ID
     * @param limit the maximum number of results
     * @return list of abandoned sessions
     */
    @Query("""
        SELECT gs FROM GameSession gs
        WHERE gs.player.id = :playerId
          AND gs.status = 'ABANDONED'
        ORDER BY gs.completedAt DESC
        LIMIT :limit
        """)
    List<GameSession> findRecentAbandonedSessions(
        @Param("playerId") Long playerId,
        @Param("limit") int limit
    );

    /**
     * Count sessions for a player entity
     * Used for player statistics
     *
     * @param player the player entity
     * @return count of sessions
     */
    Long countByPlayer(com.govtech.chinesescramble.entity.Player player);

    /**
     * Find all sessions for a player entity
     * Ordered by start date descending
     *
     * @param player the player entity
     * @return list of player's sessions
     */
    List<GameSession> findByPlayer(com.govtech.chinesescramble.entity.Player player);

    /**
     * Find session for player entity with specific status
     * Used to find active sessions
     *
     * @param player the player entity
     * @param status the session status
     * @return Optional containing session if exists
     */
    Optional<GameSession> findByPlayerAndStatus(
        com.govtech.chinesescramble.entity.Player player,
        SessionStatus status
    );

    /**
     * Find recent sessions for a player (limited count)
     * Used for recent activity display
     *
     * @param playerId the player ID
     * @param limit the maximum number of results
     * @return list of recent sessions
     */
    @Query("""
        SELECT gs FROM GameSession gs
        WHERE gs.player.id = :playerId
        ORDER BY gs.startedAt DESC
        LIMIT :limit
        """)
    List<GameSession> findRecentSessions(
        @Param("playerId") Long playerId,
        @Param("limit") int limit
    );

    /**
     * Find expired sessions before a cutoff time
     * Used by scheduled cleanup job
     *
     * @param cutoffTime the expiry cutoff time
     * @return list of expired sessions
     */
    @Query("""
        SELECT gs FROM GameSession gs
        WHERE gs.status = 'ACTIVE'
          AND gs.startedAt < :cutoffTime
        ORDER BY gs.startedAt ASC
        """)
    List<GameSession> findExpiredSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
}