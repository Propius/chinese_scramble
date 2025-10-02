package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.SentenceScore;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SentenceScoreRepository - Data access layer for SentenceScore entity
 *
 * Provides:
 * - Score retrieval and filtering by player, difficulty, date
 * - Leaderboard queries for top scores
 * - Analytics queries for statistics and insights
 * - Grammar validation insights
 *
 * Used by:
 * - ScoringService: Save and retrieve scores
 * - LeaderboardService: Generate rankings
 * - AnalyticsService: Game statistics
 * - ValidationService: Grammar error analysis
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Repository
public interface SentenceScoreRepository extends JpaRepository<SentenceScore, Long> {

    /**
     * Find all scores for a specific player
     * Ordered by createdAt descending (most recent first)
     *
     * @param playerId the player ID
     * @return list of player's scores
     */
    @Query("SELECT ss FROM SentenceScore ss WHERE ss.player.id = :playerId ORDER BY ss.createdAt DESC")
    List<SentenceScore> findByPlayerId(@Param("playerId") Long playerId);

    /**
     * Find scores by player and difficulty
     * Used for difficulty-specific statistics
     *
     * @param playerId the player ID
     * @param difficulty the difficulty level
     * @return list of scores for player at difficulty
     */
    @Query("""
        SELECT ss FROM SentenceScore ss
        WHERE ss.player.id = :playerId AND ss.difficulty = :difficulty
        ORDER BY ss.createdAt DESC
        """)
    List<SentenceScore> findByPlayerIdAndDifficulty(
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
        SELECT ss FROM SentenceScore ss
        WHERE ss.difficulty = :difficulty AND ss.completed = true
        ORDER BY ss.score DESC, ss.timeTaken ASC
        LIMIT :limit
        """)
    List<SentenceScore> findTopScoresByDifficulty(
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
        SELECT ss FROM SentenceScore ss
        WHERE ss.player.id = :playerId
          AND ss.difficulty = :difficulty
          AND ss.completed = true
        ORDER BY ss.score DESC, ss.timeTaken ASC
        LIMIT 1
        """)
    Optional<SentenceScore> findPersonalBest(
        @Param("playerId") Long playerId,
        @Param("difficulty") DifficultyLevel difficulty
    );

    /**
     * Find perfect scores (100% accuracy, no hints, no grammar errors)
     * Used for achievement tracking (PERFECT_SCORE achievement)
     *
     * @param playerId the player ID
     * @return list of perfect scores
     */
    @Query("""
        SELECT ss FROM SentenceScore ss
        WHERE ss.player.id = :playerId
          AND ss.completed = true
          AND ss.accuracyRate = 1.0
          AND ss.hintsUsed = 0
          AND ss.grammarScore = 100
        ORDER BY ss.createdAt DESC
        """)
    List<SentenceScore> findPerfectScores(@Param("playerId") Long playerId);

    /**
     * Find high similarity scores (95%+ similarity)
     * Used for analytics on player performance
     *
     * @param playerId the player ID
     * @param minSimilarity the minimum similarity threshold (0.95)
     * @return list of high similarity scores
     */
    @Query("""
        SELECT ss FROM SentenceScore ss
        WHERE ss.player.id = :playerId
          AND ss.completed = true
          AND ss.accuracyRate >= :minSimilarity
        ORDER BY ss.accuracyRate DESC
        """)
    List<SentenceScore> findHighSimilarityScores(
        @Param("playerId") Long playerId,
        @Param("minSimilarity") Double minSimilarity
    );

    /**
     * Count completed games for a player
     * Used for HUNDRED_GAMES achievement
     *
     * @param playerId the player ID
     * @return count of completed games
     */
    @Query("""
        SELECT COUNT(ss) FROM SentenceScore ss
        WHERE ss.player.id = :playerId AND ss.completed = true
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
        SELECT COUNT(ss) FROM SentenceScore ss
        WHERE ss.player.id = :playerId
          AND ss.completed = true
          AND ss.hintsUsed = 0
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
        SELECT AVG(ss.score) FROM SentenceScore ss
        WHERE ss.player.id = :playerId
          AND ss.difficulty = :difficulty
          AND ss.completed = true
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
        SELECT AVG(ss.accuracyRate) FROM SentenceScore ss
        WHERE ss.player.id = :playerId AND ss.completed = true
        """)
    Optional<Double> calculateAverageAccuracy(@Param("playerId") Long playerId);

    /**
     * Calculate average grammar score for a player
     * Used for player skill assessment
     *
     * @param playerId the player ID
     * @return average grammar score (0-100)
     */
    @Query("""
        SELECT AVG(ss.grammarScore) FROM SentenceScore ss
        WHERE ss.player.id = :playerId AND ss.completed = true
        """)
    Optional<Double> calculateAverageGrammarScore(@Param("playerId") Long playerId);

    /**
     * Find scores with grammar errors
     * Used for ValidationService to analyze common errors
     *
     * @param playerId the player ID
     * @return list of scores with grammar errors
     */
    @Query("""
        SELECT ss FROM SentenceScore ss
        WHERE ss.player.id = :playerId
          AND ss.completed = true
          AND ss.validationErrors IS NOT NULL
        ORDER BY ss.createdAt DESC
        """)
    List<SentenceScore> findScoresWithGrammarErrors(@Param("playerId") Long playerId);

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
        SELECT ss FROM SentenceScore ss
        WHERE ss.player.id = :playerId
          AND ss.createdAt BETWEEN :startDate AND :endDate
        ORDER BY ss.createdAt DESC
        """)
    List<SentenceScore> findScoresInDateRange(
        @Param("playerId") Long playerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find most frequently attempted target sentences
     * Used for analytics to identify popular content
     *
     * @param limit the maximum number of results
     * @return list of object arrays [targetSentence, attemptCount]
     */
    @Query("""
        SELECT ss.targetSentence, COUNT(ss.id) as attemptCount
        FROM SentenceScore ss
        WHERE ss.completed = true
        GROUP BY ss.targetSentence
        ORDER BY attemptCount DESC
        LIMIT :limit
        """)
    List<Object[]> findMostAttemptedSentences(@Param("limit") int limit);

    /**
     * Find most difficult sentences (lowest average grammar score)
     * Used for difficulty balancing
     *
     * @param limit the maximum number of results
     * @return list of object arrays [targetSentence, avgGrammarScore]
     */
    @Query("""
        SELECT ss.targetSentence, AVG(ss.grammarScore) as avgGrammarScore
        FROM SentenceScore ss
        WHERE ss.completed = true
        GROUP BY ss.targetSentence
        HAVING COUNT(ss.id) >= 10
        ORDER BY avgGrammarScore ASC
        LIMIT :limit
        """)
    List<Object[]> findMostDifficultSentences(@Param("limit") int limit);

    /**
     * Get daily game count for a player (for CONSISTENCY achievement)
     * Used to track consecutive daily play streaks
     *
     * @param playerId the player ID
     * @param startDate the start date (e.g., 7 days ago)
     * @return list of object arrays [date, gameCount]
     */
    @Query("""
        SELECT CAST(ss.createdAt AS DATE), COUNT(ss.id)
        FROM SentenceScore ss
        WHERE ss.player.id = :playerId
          AND ss.createdAt >= :startDate
          AND ss.completed = true
        GROUP BY CAST(ss.createdAt AS DATE)
        ORDER BY CAST(ss.createdAt AS DATE) ASC
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
        SELECT ss FROM SentenceScore ss
        WHERE ss.player.id = :playerId
        ORDER BY ss.createdAt DESC
        LIMIT 10
        """)
    List<SentenceScore> findRecentScores(@Param("playerId") Long playerId);

    /**
     * Calculate total score for a player and difficulty
     * Used for leaderboard calculation
     *
     * @param playerId the player ID
     * @param difficulty the difficulty level
     * @return total score
     */
    @Query("""
        SELECT SUM(ss.score) FROM SentenceScore ss
        WHERE ss.player.id = :playerId
          AND ss.difficulty = :difficulty
          AND ss.completed = true
        """)
    Optional<Long> calculateTotalScore(
        @Param("playerId") Long playerId,
        @Param("difficulty") DifficultyLevel difficulty
    );

    /**
     * Find top players by average grammar score
     * Used for grammar mastery leaderboard
     *
     * @param limit the maximum number of results
     * @return list of object arrays [playerId, playerUsername, avgGrammarScore]
     */
    @Query("""
        SELECT ss.player.id, ss.player.username, AVG(ss.grammarScore) as avgGrammarScore
        FROM SentenceScore ss
        WHERE ss.completed = true
        GROUP BY ss.player.id, ss.player.username
        HAVING COUNT(ss.id) >= 5
        ORDER BY avgGrammarScore DESC
        LIMIT :limit
        """)
    List<Object[]> findTopGrammarPlayers(@Param("limit") int limit);

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
    List<SentenceScore> findByPlayerOrderByCreatedAtDesc(com.govtech.chinesescramble.entity.Player player);
}