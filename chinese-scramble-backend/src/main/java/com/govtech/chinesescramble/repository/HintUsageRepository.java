package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.HintUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * HintUsageRepository - Data access layer for HintUsage entity
 *
 * Provides:
 * - Hint usage retrieval by session and player
 * - Hint analytics queries
 * - Level distribution analysis
 * - Hint effectiveness tracking
 *
 * Used by:
 * - HintService: Track and provide hints
 * - AnalyticsService: Hint usage statistics
 * - ScoringService: Apply hint penalties
 * - DifficultyService: Balance hint availability
 *
 * Hint Levels:
 * - Level 1: -10 points (least helpful)
 * - Level 2: -20 points (moderately helpful)
 * - Level 3: -30 points (most helpful)
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Repository
public interface HintUsageRepository extends JpaRepository<HintUsage, Long> {

    /**
     * Find all hints used in a game session
     * Ordered by usage time ascending
     *
     * @param sessionId the game session ID
     * @return list of hints used in session
     */
    @Query("""
        SELECT hu FROM HintUsage hu
        WHERE hu.gameSession.id = :sessionId
        ORDER BY hu.usedAt ASC
        """)
    List<HintUsage> findByGameSessionId(@Param("sessionId") Long sessionId);

    /**
     * Find hints by level for a session
     * Used for hint strategy analysis
     *
     * @param sessionId the game session ID
     * @param hintLevel the hint level (1-3)
     * @return list of hints at level
     */
    @Query("""
        SELECT hu FROM HintUsage hu
        WHERE hu.gameSession.id = :sessionId
          AND hu.hintLevel = :hintLevel
        ORDER BY hu.usedAt ASC
        """)
    List<HintUsage> findByGameSessionIdAndHintLevel(
        @Param("sessionId") Long sessionId,
        @Param("hintLevel") Integer hintLevel
    );

    /**
     * Count hints used in a session
     * Used for quick hint count validation
     *
     * @param sessionId the game session ID
     * @return count of hints used
     */
    @Query("""
        SELECT COUNT(hu) FROM HintUsage hu
        WHERE hu.gameSession.id = :sessionId
        """)
    long countHintsByGameSession(@Param("sessionId") Long sessionId);

    /**
     * Find all hints used by a player
     * Used for player hint statistics
     *
     * @param playerId the player ID
     * @return list of all hints used by player
     */
    @Query("""
        SELECT hu FROM HintUsage hu
        WHERE hu.gameSession.player.id = :playerId
        ORDER BY hu.usedAt DESC
        """)
    List<HintUsage> findByPlayerId(@Param("playerId") Long playerId);

    /**
     * Count total hints used by a player
     * Used for player statistics
     *
     * @param playerId the player ID
     * @return total hint count
     */
    @Query("""
        SELECT COUNT(hu) FROM HintUsage hu
        WHERE hu.gameSession.player.id = :playerId
        """)
    long countHintsByPlayer(@Param("playerId") Long playerId);

    /**
     * Find hints used in date range
     * Used for analytics and reporting
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of hints used in range
     */
    @Query("""
        SELECT hu FROM HintUsage hu
        WHERE hu.usedAt BETWEEN :startDate AND :endDate
        ORDER BY hu.usedAt DESC
        """)
    List<HintUsage> findHintsInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get hint level distribution across all players
     * Used for hint system analytics
     *
     * @return list of object arrays [hintLevel, usageCount]
     */
    @Query("""
        SELECT hu.hintLevel, COUNT(hu.id)
        FROM HintUsage hu
        GROUP BY hu.hintLevel
        ORDER BY hu.hintLevel ASC
        """)
    List<Object[]> getHintLevelDistribution();

    /**
     * Get hint level distribution for a player
     * Used for player hint preference analysis
     *
     * @param playerId the player ID
     * @return list of object arrays [hintLevel, usageCount]
     */
    @Query("""
        SELECT hu.hintLevel, COUNT(hu.id)
        FROM HintUsage hu
        WHERE hu.gameSession.player.id = :playerId
        GROUP BY hu.hintLevel
        ORDER BY hu.hintLevel ASC
        """)
    List<Object[]> getHintLevelDistributionByPlayer(@Param("playerId") Long playerId);

    /**
     * Calculate total penalty applied for a player
     * Used for player statistics
     *
     * @param playerId the player ID
     * @return total penalty points
     */
    @Query("""
        SELECT SUM(hu.penaltyApplied) FROM HintUsage hu
        WHERE hu.gameSession.player.id = :playerId
        """)
    Optional<Integer> calculateTotalPenalty(@Param("playerId") Long playerId);

    /**
     * Calculate average hints per session for a player
     * Used for player skill assessment
     *
     * @param playerId the player ID
     * @return average hints per session
     */
    @Query("""
        SELECT AVG(hintCount)
        FROM (
            SELECT COUNT(hu.id) as hintCount
            FROM HintUsage hu
            WHERE hu.gameSession.player.id = :playerId
            GROUP BY hu.gameSession.id
        ) AS sessionHints
        """)
    Optional<Double> calculateAverageHintsPerSession(@Param("playerId") Long playerId);

    /**
     * Find sessions with maximum hints (3 hints)
     * Used for difficulty analysis
     *
     * @return list of object arrays [sessionId, hintCount]
     */
    @Query("""
        SELECT hu.gameSession.id, COUNT(hu.id)
        FROM HintUsage hu
        GROUP BY hu.gameSession.id
        HAVING COUNT(hu.id) >= 3
        ORDER BY COUNT(hu.id) DESC
        """)
    List<Object[]> findMaxHintSessions();

    /**
     * Find most recent hint used by a player
     * Used for hint cooldown and rate limiting
     *
     * @param playerId the player ID
     * @return Optional containing most recent hint
     */
    @Query("""
        SELECT hu FROM HintUsage hu
        WHERE hu.gameSession.player.id = :playerId
        ORDER BY hu.usedAt DESC
        LIMIT 1
        """)
    Optional<HintUsage> findMostRecentHint(@Param("playerId") Long playerId);

    /**
     * Count hints used by player in date range
     * Used for daily/weekly/monthly hint analytics
     *
     * @param playerId the player ID
     * @param startDate the start date
     * @param endDate the end date
     * @return count of hints in range
     */
    @Query("""
        SELECT COUNT(hu) FROM HintUsage hu
        WHERE hu.gameSession.player.id = :playerId
          AND hu.usedAt BETWEEN :startDate AND :endDate
        """)
    long countHintsByPlayerInDateRange(
        @Param("playerId") Long playerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get daily hint usage counts
     * Used for hint usage trends
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of object arrays [date, hintCount]
     */
    @Query("""
        SELECT CAST(hu.usedAt AS DATE), COUNT(hu.id)
        FROM HintUsage hu
        WHERE hu.usedAt BETWEEN :startDate AND :endDate
        GROUP BY CAST(hu.usedAt AS DATE)
        ORDER BY CAST(hu.usedAt AS DATE) ASC
        """)
    List<Object[]> getDailyHintUsageCounts(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find players who never use hints
     * Used for HINT_FREE achievement and player segmentation
     *
     * @param minSessions minimum sessions to be eligible
     * @return list of object arrays [playerId, sessionCount]
     */
    @Query("""
        SELECT gs.player.id, COUNT(DISTINCT gs.id)
        FROM GameSession gs
        WHERE gs.status = 'COMPLETED'
          AND NOT EXISTS (
              SELECT 1 FROM HintUsage hu
              WHERE hu.gameSession.id = gs.id
          )
        GROUP BY gs.player.id
        HAVING COUNT(DISTINCT gs.id) >= :minSessions
        ORDER BY COUNT(DISTINCT gs.id) DESC
        """)
    List<Object[]> findPlayersWhoNeverUseHints(@Param("minSessions") long minSessions);

    /**
     * Get hint effectiveness metrics
     * Used for hint system optimization
     *
     * @return list of object arrays [hintLevel, avgPenalty, usageCount]
     */
    @Query("""
        SELECT hu.hintLevel,
               AVG(hu.penaltyApplied),
               COUNT(hu.id)
        FROM HintUsage hu
        GROUP BY hu.hintLevel
        ORDER BY hu.hintLevel ASC
        """)
    List<Object[]> getHintEffectivenessMetrics();

    /**
     * Find hint usage by game type
     * Used for game-specific hint analytics
     *
     * @param gameType the game type
     * @return list of hints for game type
     */
    @Query("""
        SELECT hu FROM HintUsage hu
        WHERE hu.gameSession.gameType = :gameType
        ORDER BY hu.usedAt DESC
        """)
    List<HintUsage> findByGameType(@Param("gameType") String gameType);

    /**
     * Get hint usage rate by difficulty
     * Used for difficulty balancing
     *
     * @return list of object arrays [difficulty, hintCount, sessionCount, hintRate]
     */
    @Query("""
        SELECT gs.difficulty,
               COUNT(hu.id) as hintCount,
               COUNT(DISTINCT gs.id) as sessionCount,
               CAST(COUNT(hu.id) AS DOUBLE) / COUNT(DISTINCT gs.id) as hintRate
        FROM GameSession gs
        LEFT JOIN gs.hintsUsed hu
        WHERE gs.status = 'COMPLETED'
        GROUP BY gs.difficulty
        ORDER BY gs.difficulty ASC
        """)
    List<Object[]> getHintUsageRateByDifficulty();

    /**
     * Find heavy hint users (top 10% by hint usage)
     * Used for player segmentation and support
     *
     * @param limit the maximum number of results
     * @return list of object arrays [playerId, playerUsername, totalHints]
     */
    @Query("""
        SELECT hu.gameSession.player.id,
               hu.gameSession.player.username,
               COUNT(hu.id) as totalHints
        FROM HintUsage hu
        GROUP BY hu.gameSession.player.id, hu.gameSession.player.username
        ORDER BY COUNT(hu.id) DESC
        LIMIT :limit
        """)
    List<Object[]> findHeavyHintUsers(@Param("limit") int limit);
}