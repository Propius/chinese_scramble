package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PlayerRepository - Data access layer for Player entity
 *
 * Provides:
 * - Standard CRUD operations via JpaRepository
 * - Custom queries for authentication and player management
 * - User search and filtering capabilities
 * - Activity tracking queries
 *
 * Used by:
 * - AuthenticationService: Login, registration, token validation
 * - PlayerService: Profile management, statistics
 * - AdminService: User administration
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    /**
     * Find player by username (exact match)
     * Used for testing and specific lookups
     *
     * @param username the username to search for
     * @return Optional containing player if found
     */
    Optional<Player> findByUsername(String username);

    /**
     * Find player by username (case-insensitive)
     * Used for login authentication
     *
     * @param username the username to search for
     * @return Optional containing player if found
     */
    Optional<Player> findByUsernameIgnoreCase(String username);

    /**
     * Find player by email (case-insensitive)
     * Used for registration validation and password recovery
     *
     * @param email the email to search for
     * @return Optional containing player if found
     */
    Optional<Player> findByEmailIgnoreCase(String email);

    /**
     * Check if username exists (case-insensitive)
     * Used for registration validation
     *
     * @param username the username to check
     * @return true if username is already taken
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Check if email exists (case-insensitive)
     * Used for registration validation
     *
     * @param email the email to check
     * @return true if email is already registered
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Find all active players
     * Used for admin user management
     *
     * @return list of active players
     */
    List<Player> findByActiveTrue();

    /**
     * Find all inactive players
     * Used for admin user management and cleanup
     *
     * @return list of inactive players
     */
    List<Player> findByActiveFalse();

    /**
     * Find players by role
     * Used for admin user management
     *
     * @param role the user role
     * @return list of players with specified role
     */
    List<Player> findByRole(UserRole role);

    /**
     * Find players registered after a specific date
     * Used for analytics and reporting
     *
     * @param date the cutoff date
     * @return list of players registered after date
     */
    List<Player> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find players who logged in recently
     * Used for activity tracking and engagement metrics
     *
     * @param cutoffDate the date threshold
     * @return list of recently active players
     */
    @Query("SELECT p FROM Player p WHERE p.lastLoginAt >= :cutoffDate AND p.active = true")
    List<Player> findRecentlyActivePlayers(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find top players by total score across all games
     * Used for overall leaderboard
     *
     * @param limit the maximum number of results
     * @return list of top players ordered by total score
     */
    @Query("""
        SELECT p FROM Player p
        LEFT JOIN p.idiomScores is
        LEFT JOIN p.sentenceScores ss
        WHERE p.active = true
        GROUP BY p.id
        ORDER BY (COALESCE(SUM(is.score), 0) + COALESCE(SUM(ss.score), 0)) DESC
        LIMIT :limit
        """)
    List<Player> findTopPlayersByTotalScore(@Param("limit") int limit);

    /**
     * Count active players
     * Used for analytics dashboard
     *
     * @return count of active players
     */
    long countByActiveTrue();

    /**
     * Count players registered in a date range
     * Used for growth analytics
     *
     * @param startDate the start of date range
     * @param endDate the end of date range
     * @return count of players registered in range
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Search players by username or email (case-insensitive, partial match)
     * Used for admin search functionality
     *
     * @param searchTerm the search term
     * @return list of matching players
     */
    @Query("""
        SELECT p FROM Player p
        WHERE LOWER(p.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           OR LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        ORDER BY p.createdAt DESC
        """)
    List<Player> searchPlayers(@Param("searchTerm") String searchTerm);

    /**
     * Find inactive players who haven't logged in for a specified duration
     * Used for account cleanup and re-engagement campaigns
     *
     * @param inactiveSince the date threshold
     * @return list of inactive players
     */
    @Query("""
        SELECT p FROM Player p
        WHERE p.active = true
          AND (p.lastLoginAt IS NULL OR p.lastLoginAt < :inactiveSince)
        ORDER BY p.lastLoginAt ASC NULLS FIRST
        """)
    List<Player> findInactivePlayers(@Param("inactiveSince") LocalDateTime inactiveSince);

    /**
     * Get player statistics including game counts and scores
     * Used for player profile page
     *
     * @param playerId the player ID
     * @return player statistics as object array
     */
    @Query("""
        SELECT p.id,
               p.username,
               COUNT(DISTINCT is.id) as idiomGameCount,
               COUNT(DISTINCT ss.id) as sentenceGameCount,
               COALESCE(SUM(is.score), 0) as totalIdiomScore,
               COALESCE(SUM(ss.score), 0) as totalSentenceScore,
               COALESCE(AVG(is.accuracyRate), 0) as avgIdiomAccuracy,
               COALESCE(AVG(ss.accuracyRate), 0) as avgSentenceAccuracy
        FROM Player p
        LEFT JOIN p.idiomScores is
        LEFT JOIN p.sentenceScores ss
        WHERE p.id = :playerId
        GROUP BY p.id, p.username
        """)
    Optional<Object[]> getPlayerStatistics(@Param("playerId") Long playerId);
}