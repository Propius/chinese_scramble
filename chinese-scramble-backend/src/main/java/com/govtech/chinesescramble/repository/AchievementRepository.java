package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AchievementRepository - Data access layer for Achievement entity
 *
 * Provides:
 * - Achievement retrieval by player and type
 * - Achievement unlock checking
 * - Recent achievements tracking
 * - Achievement statistics
 *
 * Used by:
 * - AchievementService: Unlock and track achievements
 * - PlayerService: Display player achievements
 * - AnalyticsService: Achievement statistics
 *
 * Achievement Types:
 * - FIRST_WIN: Complete first game
 * - SPEED_DEMON: Complete in under 30 seconds
 * - PERFECT_SCORE: 100% accuracy, no hints
 * - HUNDRED_GAMES: Play 100 games
 * - IDIOM_MASTER: Master all idiom difficulties
 * - SENTENCE_MASTER: Master all sentence difficulties
 * - TOP_RANKED: Reach #1 on leaderboard
 * - CONSISTENCY: Play 7 consecutive days
 * - HIGH_SCORER: 1000+ points in single game
 * - HINT_FREE: Complete 10 games without hints
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    /**
     * Find all achievements for a player
     * Ordered by unlock date descending (most recent first)
     *
     * @param playerId the player ID
     * @return list of player's achievements
     */
    @Query("""
        SELECT a FROM Achievement a
        WHERE a.player.id = :playerId
        ORDER BY a.unlockedAt DESC
        """)
    List<Achievement> findByPlayerId(@Param("playerId") Long playerId);

    /**
     * Find achievement by player and type
     * Used to check if achievement already unlocked
     *
     * @param playerId the player ID
     * @param achievementType the achievement type
     * @return Optional containing achievement if exists
     */
    @Query("""
        SELECT a FROM Achievement a
        WHERE a.player.id = :playerId
          AND a.achievementType = :achievementType
        """)
    Optional<Achievement> findByPlayerIdAndAchievementType(
        @Param("playerId") Long playerId,
        @Param("achievementType") String achievementType
    );

    /**
     * Check if player has unlocked a specific achievement
     * Used for quick achievement checks
     *
     * @param playerId the player ID
     * @param achievementType the achievement type
     * @return true if achievement exists
     */
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Achievement a
        WHERE a.player.id = :playerId
          AND a.achievementType = :achievementType
        """)
    boolean existsByPlayerIdAndAchievementType(
        @Param("playerId") Long playerId,
        @Param("achievementType") String achievementType
    );

    /**
     * Find recently unlocked achievements (within 24 hours)
     * Used for notifications and recent activity feed
     *
     * @param playerId the player ID
     * @return list of recent achievements
     */
    @Query("""
        SELECT a FROM Achievement a
        WHERE a.player.id = :playerId
          AND a.unlockedAt >= :cutoffTime
        ORDER BY a.unlockedAt DESC
        """)
    List<Achievement> findRecentAchievements(
        @Param("playerId") Long playerId,
        @Param("cutoffTime") LocalDateTime cutoffTime
    );

    /**
     * Find achievements unlocked today
     * Used for daily achievement notifications
     *
     * @param playerId the player ID
     * @param todayStart the start of today
     * @return list of today's achievements
     */
    @Query("""
        SELECT a FROM Achievement a
        WHERE a.player.id = :playerId
          AND a.unlockedAt >= :todayStart
        ORDER BY a.unlockedAt DESC
        """)
    List<Achievement> findTodaysAchievements(
        @Param("playerId") Long playerId,
        @Param("todayStart") LocalDateTime todayStart
    );

    /**
     * Count total achievements for a player
     * Used for player profile statistics
     *
     * @param playerId the player ID
     * @return count of achievements
     */
    long countByPlayerId(Long playerId);

    /**
     * Count achievements unlocked in date range
     * Used for analytics
     *
     * @param playerId the player ID
     * @param startDate the start date
     * @param endDate the end date
     * @return count of achievements in range
     */
    @Query("""
        SELECT COUNT(a) FROM Achievement a
        WHERE a.player.id = :playerId
          AND a.unlockedAt BETWEEN :startDate AND :endDate
        """)
    long countAchievementsInDateRange(
        @Param("playerId") Long playerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find all players who unlocked a specific achievement
     * Used for achievement rarity statistics
     *
     * @param achievementType the achievement type
     * @return list of achievements
     */
    List<Achievement> findByAchievementType(String achievementType);

    /**
     * Count players who unlocked a specific achievement
     * Used for achievement rarity calculation
     *
     * @param achievementType the achievement type
     * @return count of players with achievement
     */
    long countByAchievementType(String achievementType);

    /**
     * Find most recent achievement across all players
     * Used for global activity feed
     *
     * @param limit the maximum number of results
     * @return list of recent achievements
     */
    @Query("""
        SELECT a FROM Achievement a
        ORDER BY a.unlockedAt DESC
        LIMIT :limit
        """)
    List<Achievement> findMostRecentAchievements(@Param("limit") int limit);

    /**
     * Find rare achievements (unlocked by < 5% of players)
     * Used for achievement showcase
     *
     * @param playerId the player ID
     * @param totalPlayers the total player count
     * @return list of rare achievements
     */
    @Query("""
        SELECT a FROM Achievement a
        WHERE a.player.id = :playerId
          AND a.achievementType IN (
              SELECT ach.achievementType
              FROM Achievement ach
              GROUP BY ach.achievementType
              HAVING COUNT(DISTINCT ach.player.id) < (:totalPlayers * 0.05)
          )
        ORDER BY a.unlockedAt DESC
        """)
    List<Achievement> findRareAchievements(
        @Param("playerId") Long playerId,
        @Param("totalPlayers") long totalPlayers
    );

    /**
     * Get achievement type distribution
     * Used for analytics dashboard
     *
     * @return list of object arrays [achievementType, unlockCount]
     */
    @Query("""
        SELECT a.achievementType, COUNT(a.id)
        FROM Achievement a
        GROUP BY a.achievementType
        ORDER BY COUNT(a.id) DESC
        """)
    List<Object[]> getAchievementDistribution();

    /**
     * Find players with most achievements
     * Used for achievement leaderboard
     *
     * @param limit the maximum number of results
     * @return list of object arrays [playerId, playerUsername, achievementCount]
     */
    @Query("""
        SELECT a.player.id, a.player.username, COUNT(a.id)
        FROM Achievement a
        WHERE a.player.active = true
        GROUP BY a.player.id, a.player.username
        ORDER BY COUNT(a.id) DESC
        LIMIT :limit
        """)
    List<Object[]> findTopAchievementPlayers(@Param("limit") int limit);

    /**
     * Find achievements unlocked on a specific date
     * Used for achievement timeline
     *
     * @param playerId the player ID
     * @param date the specific date
     * @return list of achievements unlocked on date
     */
    @Query("""
        SELECT a FROM Achievement a
        WHERE a.player.id = :playerId
          AND CAST(a.unlockedAt AS DATE) = :date
        ORDER BY a.unlockedAt ASC
        """)
    List<Achievement> findAchievementsByDate(
        @Param("playerId") Long playerId,
        @Param("date") LocalDateTime date
    );

    /**
     * Get achievement unlock rate over time
     * Used for analytics charts
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of object arrays [date, unlockCount]
     */
    @Query("""
        SELECT CAST(a.unlockedAt AS DATE), COUNT(a.id)
        FROM Achievement a
        WHERE a.unlockedAt BETWEEN :startDate AND :endDate
        GROUP BY CAST(a.unlockedAt AS DATE)
        ORDER BY CAST(a.unlockedAt AS DATE) ASC
        """)
    List<Object[]> getAchievementUnlockRateOverTime(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find first achievement unlocked by player
     * Used for onboarding analytics
     *
     * @param playerId the player ID
     * @return Optional containing first achievement
     */
    @Query("""
        SELECT a FROM Achievement a
        WHERE a.player.id = :playerId
        ORDER BY a.unlockedAt ASC
        LIMIT 1
        """)
    Optional<Achievement> findFirstAchievement(@Param("playerId") Long playerId);

    /**
     * Find most recent achievement for player
     * Used for profile display
     *
     * @param playerId the player ID
     * @return Optional containing most recent achievement
     */
    @Query("""
        SELECT a FROM Achievement a
        WHERE a.player.id = :playerId
        ORDER BY a.unlockedAt DESC
        LIMIT 1
        """)
    Optional<Achievement> findMostRecentAchievement(@Param("playerId") Long playerId);

    /**
     * Find all achievements for a player entity
     * Ordered by unlock date descending (most recent first)
     *
     * @param player the player entity
     * @return list of player's achievements
     */
    List<Achievement> findByPlayer(com.govtech.chinesescramble.entity.Player player);

    /**
     * Find achievement by player entity and type
     * Used to check if achievement already unlocked
     *
     * @param player the player entity
     * @param achievementType the achievement type
     * @return Optional containing achievement if exists
     */
    Optional<Achievement> findByPlayerAndAchievementType(
        com.govtech.chinesescramble.entity.Player player,
        String achievementType
    );

    /**
     * Check if player has unlocked a specific achievement
     * Used for quick achievement checks
     *
     * @param player the player entity
     * @param achievementType the achievement type
     * @return true if achievement exists
     */
    boolean existsByPlayerAndAchievementType(
        com.govtech.chinesescramble.entity.Player player,
        String achievementType
    );

    /**
     * Count total achievements for a player entity
     * Used for player profile statistics
     *
     * @param player the player entity
     * @return count of achievements
     */
    long countByPlayer(com.govtech.chinesescramble.entity.Player player);

    /**
     * Find achievements unlocked for a player entity
     * Only returns achievements where unlockedAt is not null
     *
     * @param player the player entity
     * @return list of unlocked achievements
     */
    List<Achievement> findByPlayerAndUnlockedAtNotNull(com.govtech.chinesescramble.entity.Player player);

    /**
     * Find achievements for a player ordered by unlock date descending
     * Used for achievement timeline display
     *
     * @param player the player entity
     * @return list of achievements ordered by most recent first
     */
    List<Achievement> findByPlayerOrderByUnlockedAtDesc(com.govtech.chinesescramble.entity.Player player);
}