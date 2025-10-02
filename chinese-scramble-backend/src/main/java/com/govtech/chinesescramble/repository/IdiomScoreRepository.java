package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.IdiomScore;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * IdiomScoreRepository - Data access layer for IdiomScore entity
 *
 * Provides:
 * - Score retrieval and filtering by player, difficulty, date
 * - Leaderboard queries for top scores
 * - Analytics queries for statistics and insights
 * - Personal best and achievement tracking
 *
 * Used by:
 * - ScoringService: Save and retrieve scores
 * - LeaderboardService: Generate rankings
 * - AnalyticsService: Game statistics
 * - AchievementService: Unlock achievements
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Repository
public interface IdiomScoreRepository extends JpaRepository<IdiomScore, Long> {

    /**
     * Find all scores for a specific player
     * Ordered by createdAt descending (most recent first)
     *
     * @param playerId the player ID
     * @return list of player's scores
     */
    @Query("SELECT is FROM IdiomScore is WHERE is.player.id = :playerId ORDER BY is.createdAt DESC")
    List<IdiomScore> findByPlayerId(@Param("playerId") Long playerId);

    /**
     * Find scores by player and difficulty
     * Used for difficulty-specific statistics
     *
     * @param playerId the player ID
     * @param difficulty the difficulty level
     * @return list of scores for player at difficulty
     */
    @Query("""
        SELECT is FROM IdiomScore is
        WHERE is.player.id = :playerId AND is.difficulty = :difficulty
        ORDER BY is.createdAt DESC
        """)
    List<IdiomScore> findByPlayerIdAndDifficulty(
        @Param("playerId") Long playerId,
        @Param("difficulty") DifficultyLevel difficulty
    );

    /**
     * Find top scores across all players for a difficulty
     * Used for leaderboard generation
     *
     * @param difficulty the difficulty level
     * @param limit the maximum number of results
     * @return list of top scores
     */
    @Query("""
        SELECT is FROM IdiomScore is
        WHERE is.difficulty = :difficulty AND is.completed = true
        ORDER BY is.score DESC, is.timeTaken ASC
        LIMIT :limit
        """)
    List<IdiomScore> findTopScoresByDifficulty(
        @Param("difficulty") DifficultyLevel difficulty,
        @Param("limit") int limit
    );

    /**
     * Find player's personal best score for a difficulty
     * Used for achievement tracking and profile display
     *
     * @param playerId the player ID
     * @param difficulty the difficulty level
     * @return Optional containing personal best score
     */
    @Query("""
        SELECT is FROM IdiomScore is
        WHERE is.player.id = :playerId
          AND is.difficulty = :difficulty
          AND is.completed = true
        ORDER BY is.score DESC, is.timeTaken ASC
        LIMIT 1
        """)
    Optional<IdiomScore> findPersonalBest(
        @Param("playerId") Long playerId,
        @Param("difficulty") DifficultyLevel difficulty
    );

    /**
     * Find perfect scores (100% accuracy, no hints)
     * Used for achievement tracking (PERFECT_SCORE achievement)
     *
     * @param playerId the player ID
     * @return list of perfect scores
     */
    @Query("""
        SELECT is FROM IdiomScore is
        WHERE is.player.id = :playerId
          AND is.completed = true
          AND is.accuracyRate = 1.0
          AND is.hintsUsed = 0
        ORDER BY is.createdAt DESC
        """)
    List<IdiomScore> findPerfectScores(@Param("playerId") Long playerId);

    /**
     * Find speed demon scores (completed in under 30 seconds)
     * Used for SPEED_DEMON achievement
     *
     * @param playerId the player ID
     * @param maxTime the maximum time in seconds (30)
     * @return list of speed demon scores
     */
    @Query("""
        SELECT is FROM IdiomScore is
        WHERE is.player.id = :playerId
          AND is.completed = true
          AND is.timeTaken < :maxTime
        ORDER BY is.timeTaken ASC
        """)
    List<IdiomScore> findSpeedDemonScores(
        @Param("playerId") Long playerId,
        @Param("maxTime") Integer maxTime
    );

    /**
     * Count completed games for a player
     * Used for HUNDRED_GAMES achievement
     *
     * @param playerId the player ID
     * @return count of completed games
     */
    @Query("""
        SELECT COUNT(is) FROM IdiomScore is
        WHERE is.player.id = :playerId AND is.completed = true
        """)
    long countCompletedGamesByPlayer(@Param("playerId") Long playerId);

    /**
     * Count hint-free games for a player
     * Used for HINT_FREE achievement
     *
     * @param playerId the player ID
     * @return count of games completed without hints
     */
    @Query("""
        SELECT COUNT(is) FROM IdiomScore is
        WHERE is.player.id = :playerId
          AND is.completed = true
          AND is.hintsUsed = 0
        """)
    long countHintFreeGamesByPlayer(@Param("playerId") Long playerId);

    /**
     * Calculate average score for a player and difficulty
     * Used for statistics and player profile
     *
     * @param playerId the player ID
     * @param difficulty the difficulty level
     * @return average score
     */
    @Query("""
        SELECT AVG(is.score) FROM IdiomScore is
        WHERE is.player.id = :playerId
          AND is.difficulty = :difficulty
          AND is.completed = true
        """)
    Optional<Double> calculateAverageScore(
        @Param("playerId") Long playerId,
        @Param("difficulty") DifficultyLevel difficulty
    );

    /**
     * Calculate average accuracy rate for a player
     * Used for statistics and leaderboard
     *
     * @param playerId the player ID
     * @return average accuracy rate (0.0 to 1.0)
     */
    @Query("""
        SELECT AVG(is.accuracyRate) FROM IdiomScore is
        WHERE is.player.id = :playerId AND is.completed = true
        """)
    Optional<Double> calculateAverageAccuracy(@Param("playerId") Long playerId);

    /**
     * Find scores played in a date range
     * Used for analytics and daily challenge tracking
     *
     * @param playerId the player ID
     * @param startDate the start of date range
     * @param endDate the end of date range
     * @return list of scores in date range
     */
    @Query("""
        SELECT is FROM IdiomScore is
        WHERE is.player.id = :playerId
          AND is.createdAt BETWEEN :startDate AND :endDate
        ORDER BY is.createdAt DESC
        """)
    List<IdiomScore> findScoresInDateRange(
        @Param("playerId") Long playerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find most frequently played idioms
     * Used for analytics to identify popular content
     *
     * @param limit the maximum number of results
     * @return list of object arrays [idiom, playCount]
     */
    @Query("""
        SELECT is.idiom, COUNT(is.id) as playCount
        FROM IdiomScore is
        WHERE is.completed = true
        GROUP BY is.idiom
        ORDER BY playCount DESC
        LIMIT :limit
        """)
    List<Object[]> findMostPlayedIdioms(@Param("limit") int limit);

    /**
     * Find most difficult idioms (lowest average accuracy)
     * Used for difficulty balancing
     *
     * @param limit the maximum number of results
     * @return list of object arrays [idiom, avgAccuracy]
     */
    @Query("""
        SELECT is.idiom, AVG(is.accuracyRate) as avgAccuracy
        FROM IdiomScore is
        WHERE is.completed = true
        GROUP BY is.idiom
        HAVING COUNT(is.id) >= 10
        ORDER BY avgAccuracy ASC
        LIMIT :limit
        """)
    List<Object[]> findMostDifficultIdioms(@Param("limit") int limit);

    /**
     * Get daily game count for a player (for CONSISTENCY achievement)
     * Used to track consecutive daily play streaks
     *
     * @param playerId the player ID
     * @param startDate the start date (e.g., 7 days ago)
     * @return list of object arrays [date, gameCount]
     */
    @Query("""
        SELECT CAST(is.createdAt AS DATE), COUNT(is.id)
        FROM IdiomScore is
        WHERE is.player.id = :playerId
          AND is.createdAt >= :startDate
          AND is.completed = true
        GROUP BY CAST(is.createdAt AS DATE)
        ORDER BY CAST(is.createdAt AS DATE) ASC
        """)
    List<Object[]> getDailyGameCounts(
        @Param("playerId") Long playerId,
        @Param("startDate") LocalDateTime startDate
    );

    /**
     * Find recent scores for a player (last 10)
     * Used for recent activity display
     *
     * @param playerId the player ID
     * @return list of recent scores
     */
    @Query("""
        SELECT is FROM IdiomScore is
        WHERE is.player.id = :playerId
        ORDER BY is.createdAt DESC
        LIMIT 10
        """)
    List<IdiomScore> findRecentScores(@Param("playerId") Long playerId);

    /**
     * Calculate total score for a player and difficulty
     * Used for leaderboard calculation
     *
     * @param playerId the player ID
     * @param difficulty the difficulty level
     * @return total score
     */
    @Query("""
        SELECT SUM(is.score) FROM IdiomScore is
        WHERE is.player.id = :playerId
          AND is.difficulty = :difficulty
          AND is.completed = true
        """)
    Optional<Long> calculateTotalScore(
        @Param("playerId") Long playerId,
        @Param("difficulty") DifficultyLevel difficulty
    );

    /**
     * Count total games played by a player entity
     * Used for statistics and achievements
     *
     * @param player the player entity
     * @return count of games
     */
    Long countByPlayer(com.govtech.chinesescramble.entity.Player player);

    /**
     * Find scores by player entity ordered by createdAt descending
     * Used for recent activity display
     *
     * @param player the player entity
     * @return list of scores ordered by most recent first
     */
    List<IdiomScore> findByPlayerOrderByCreatedAtDesc(com.govtech.chinesescramble.entity.Player player);

    /**
     * Find player's best score for a difficulty
     * Returns the highest score value
     *
     * @param playerId the player ID
     * @param difficulty the difficulty level
     * @return Optional containing best score value
     */
    @Query("""
        SELECT MAX(is.score) FROM IdiomScore is
        WHERE is.player.id = :playerId
          AND is.difficulty = :difficulty
          AND is.completed = true
        """)
    Optional<Double> findPlayerBestScore(
        @Param("playerId") Long playerId,
        @Param("difficulty") DifficultyLevel difficulty
    );

    /**
     * Count player games by difficulty
     * Used for leaderboard statistics
     *
     * @param playerId the player ID
     * @param difficulty the difficulty level
     * @return count of games at difficulty
     */
    @Query("""
        SELECT COUNT(is) FROM IdiomScore is
        WHERE is.player.id = :playerId
          AND is.difficulty = :difficulty
          AND is.completed = true
        """)
    Long countPlayerGames(
        @Param("playerId") Long playerId,
        @Param("difficulty") DifficultyLevel difficulty
    );

    /**
     * Calculate average score for a player across all difficulties
     * Used for overall player statistics
     *
     * @param player the player entity
     * @return average score
     */
    @Query("""
        SELECT AVG(is.score) FROM IdiomScore is
        WHERE is.player = :player
          AND is.completed = true
        """)
    Double findAverageScoreByPlayer(@Param("player") com.govtech.chinesescramble.entity.Player player);
}