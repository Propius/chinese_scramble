package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.Leaderboard;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * LeaderboardRepository - Data access layer for Leaderboard entity
 *
 * Provides:
 * - Ranking queries for different game types and difficulties
 * - Player position lookup
 * - Top player retrieval
 * - Leaderboard updates and recalculations
 *
 * Used by:
 * - LeaderboardService: Generate and update rankings
 * - PlayerService: Display player rankings
 * - AnalyticsService: Leaderboard statistics
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {

    /**
     * Find leaderboard entry for a player, game type, and difficulty
     * Used to check if entry exists before creating
     *
     * @param playerId the player ID
     * @param gameType the game type
     * @param difficulty the difficulty level
     * @return Optional containing leaderboard entry
     */
    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.player.id = :playerId
          AND l.gameType = :gameType
          AND l.difficulty = :difficulty
        """)
    Optional<Leaderboard> findByPlayerIdAndGameTypeAndDifficulty(
        @Param("playerId") Long playerId,
        @Param("gameType") GameType gameType,
        @Param("difficulty") DifficultyLevel difficulty
    );

    /**
     * Find top N players for a game type and difficulty
     * Used for leaderboard display
     *
     * @param gameType the game type
     * @param difficulty the difficulty level
     * @param limit the maximum number of results
     * @return list of top leaderboard entries
     */
    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.gameType = :gameType
          AND l.difficulty = :difficulty
        ORDER BY l.rank ASC
        LIMIT :limit
        """)
    List<Leaderboard> findTopPlayersByGameTypeAndDifficulty(
        @Param("gameType") GameType gameType,
        @Param("difficulty") DifficultyLevel difficulty,
        @Param("limit") int limit
    );

    /**
     * Find all leaderboard entries for a player
     * Used for player profile display
     *
     * @param playerId the player ID
     * @return list of player's leaderboard entries
     */
    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.player.id = :playerId
        ORDER BY l.gameType ASC, l.difficulty ASC
        """)
    List<Leaderboard> findByPlayerId(@Param("playerId") Long playerId);

    /**
     * Find all top 10 rankings for a player
     * Used for achievement tracking (TOP_RANKED achievement)
     *
     * @param playerId the player ID
     * @return list of top 10 leaderboard entries
     */
    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.player.id = :playerId
          AND l.rank <= 10
        ORDER BY l.rank ASC
        """)
    List<Leaderboard> findTopTenEntriesByPlayer(@Param("playerId") Long playerId);

    /**
     * Find all #1 ranked entries for a player
     * Used for achievement tracking (TOP_RANKED #1 achievement)
     *
     * @param playerId the player ID
     * @return list of first place leaderboard entries
     */
    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.player.id = :playerId
          AND l.rank = 1
        """)
    List<Leaderboard> findFirstPlaceEntriesByPlayer(@Param("playerId") Long playerId);

    /**
     * Find leaderboard entries by game type
     * Used for game type specific leaderboards
     *
     * @param gameType the game type
     * @return list of leaderboard entries
     */
    List<Leaderboard> findByGameType(GameType gameType);

    /**
     * Find leaderboard entries by difficulty
     * Used for difficulty specific leaderboards
     *
     * @param difficulty the difficulty level
     * @return list of leaderboard entries
     */
    List<Leaderboard> findByDifficulty(DifficultyLevel difficulty);

    /**
     * Find players near a specific rank (±5 positions)
     * Used for contextual leaderboard display
     *
     * @param gameType the game type
     * @param difficulty the difficulty level
     * @param rank the center rank
     * @param offset the offset (±5)
     * @return list of leaderboard entries near rank
     */
    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.gameType = :gameType
          AND l.difficulty = :difficulty
          AND l.rank BETWEEN :rank - :offset AND :rank + :offset
        ORDER BY l.rank ASC
        """)
    List<Leaderboard> findPlayersNearRank(
        @Param("gameType") GameType gameType,
        @Param("difficulty") DifficultyLevel difficulty,
        @Param("rank") Integer rank,
        @Param("offset") Integer offset
    );

    /**
     * Count total players on a leaderboard
     * Used for rank calculation and pagination
     *
     * @param gameType the game type
     * @param difficulty the difficulty level
     * @return count of players on leaderboard
     */
    long countByGameTypeAndDifficulty(GameType gameType, DifficultyLevel difficulty);

    /**
     * Find players with rank better than a threshold
     * Used for achievement queries
     *
     * @param maxRank the maximum rank (e.g., 100)
     * @return list of top-ranked leaderboard entries
     */
    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.rank <= :maxRank
        ORDER BY l.rank ASC
        """)
    List<Leaderboard> findByRankLessThanEqual(@Param("maxRank") Integer maxRank);

    /**
     * Find leaderboard entries with minimum games played
     * Used for filtering inactive players
     *
     * @param minGames the minimum games played
     * @return list of active leaderboard entries
     */
    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.gamesPlayed >= :minGames
        ORDER BY l.totalScore DESC
        """)
    List<Leaderboard> findActivePlayersWithMinGames(@Param("minGames") Integer minGames);

    /**
     * Find players with highest accuracy rate
     * Used for accuracy leaderboard
     *
     * @param gameType the game type
     * @param difficulty the difficulty level
     * @param minGames minimum games for eligibility
     * @param limit the maximum number of results
     * @return list of highest accuracy players
     */
    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.gameType = :gameType
          AND l.difficulty = :difficulty
          AND l.gamesPlayed >= :minGames
        ORDER BY l.accuracyRate DESC, l.totalScore DESC
        LIMIT :limit
        """)
    List<Leaderboard> findTopAccuracyPlayers(
        @Param("gameType") GameType gameType,
        @Param("difficulty") DifficultyLevel difficulty,
        @Param("minGames") Integer minGames,
        @Param("limit") int limit
    );

    /**
     * Find all entries for recalculation
     * Used by scheduled job to update rankings
     *
     * @param gameType the game type
     * @param difficulty the difficulty level
     * @return list of all leaderboard entries sorted by score
     */
    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.gameType = :gameType
          AND l.difficulty = :difficulty
        ORDER BY l.totalScore DESC, l.averageScore DESC, l.gamesPlayed DESC
        """)
    List<Leaderboard> findAllForRankRecalculation(
        @Param("gameType") GameType gameType,
        @Param("difficulty") DifficultyLevel difficulty
    );

    /**
     * Get player's rank across all leaderboards
     * Used for player profile summary
     *
     * @param playerId the player ID
     * @return list of object arrays [gameType, difficulty, rank]
     */
    @Query("""
        SELECT l.gameType, l.difficulty, l.rank
        FROM Leaderboard l
        WHERE l.player.id = :playerId
        ORDER BY l.rank ASC
        """)
    List<Object[]> getPlayerRankings(@Param("playerId") Long playerId);

    /**
     * Find entries with stale data (not updated recently)
     * Used for cache invalidation
     *
     * @param cutoffDate the date before which entries are considered stale
     * @return list of stale leaderboard entries
     */
    List<Leaderboard> findByLastUpdatedBefore(LocalDateTime cutoffDate);

    /**
     * Get leaderboard statistics for a game type and difficulty
     * Used for analytics dashboard
     *
     * @param gameType the game type
     * @param difficulty the difficulty level
     * @return object array [totalPlayers, avgScore, avgAccuracy]
     */
    @Query("""
        SELECT COUNT(l.id),
               AVG(l.totalScore),
               AVG(l.accuracyRate)
        FROM Leaderboard l
        WHERE l.gameType = :gameType
          AND l.difficulty = :difficulty
        """)
    Optional<Object[]> getLeaderboardStatistics(
        @Param("gameType") GameType gameType,
        @Param("difficulty") DifficultyLevel difficulty
    );

    /**
     * Find leaderboard entry for a player entity, game type, and difficulty
     * Used to check if entry exists before creating
     *
     * @param player the player entity
     * @param gameType the game type
     * @param difficulty the difficulty level
     * @return Optional containing leaderboard entry
     */
    Optional<Leaderboard> findByPlayerAndGameTypeAndDifficulty(
        com.govtech.chinesescramble.entity.Player player,
        GameType gameType,
        DifficultyLevel difficulty
    );

    /**
     * Find all leaderboard entries ordered by total score descending
     * Used for ranking calculation
     *
     * @param gameType the game type
     * @param difficulty the difficulty level
     * @return list of leaderboard entries ordered by score
     */
    List<Leaderboard> findByGameTypeAndDifficultyOrderByTotalScoreDesc(
        GameType gameType,
        DifficultyLevel difficulty
    );

    /**
     * Find top N players by game type and difficulty
     * Used for leaderboard display
     *
     * @param gameType the game type
     * @param difficulty the difficulty level
     * @param limit the maximum number of results
     * @return list of top leaderboard entries
     */
    @Query("""
        SELECT l FROM Leaderboard l
        WHERE l.gameType = :gameType
          AND l.difficulty = :difficulty
        ORDER BY l.totalScore DESC, l.averageScore DESC
        LIMIT :limit
        """)
    List<Leaderboard> findTopPlayersByDifficulty(
        @Param("gameType") GameType gameType,
        @Param("difficulty") DifficultyLevel difficulty,
        @Param("limit") int limit
    );
}