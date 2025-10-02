package com.govtech.chinesescramble.entity;

import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Leaderboard Entity - Represents a player's ranking entry on the leaderboard
 *
 * Purpose:
 * - Maintains player rankings for different game modes and difficulties
 * - Pre-calculated aggregated statistics for fast leaderboard queries
 * - Updated after each completed game
 *
 * Leaderboard Types:
 * - Idiom + Easy: Top players in easy idiom games
 * - Idiom + Medium: Top players in medium idiom games
 * - Idiom + Hard/Expert: Top players in hard/expert idiom games
 * - Sentence + Easy/Medium/Hard/Expert: Similar for sentence games
 * - Combined: Overall rankings across all games
 *
 * Ranking Algorithm:
 * 1. Calculate total score across all games of this type/difficulty
 * 2. Sort by total score descending
 * 3. Assign rank (1 = highest score)
 * 4. Update periodically via scheduled job
 *
 * Performance Notes:
 * - Indexed on (game_type, difficulty, rank) for fast queries
 * - Unique constraint on (player_id, game_type, difficulty)
 * - Cached with 5-minute TTL (configured in application.yml)
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Entity
@Table(
    name = "leaderboard",
    indexes = {
        @Index(name = "idx_leaderboard_game_type", columnList = "game_type,difficulty"),
        @Index(name = "idx_leaderboard_rank", columnList = "rank"),
        @Index(name = "idx_leaderboard_score", columnList = "total_score")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_leaderboard_player_game",
            columnNames = {"player_id", "game_type", "difficulty"}
        )
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Leaderboard extends BaseEntity {

    /**
     * Player whose ranking this entry represents
     * One player can have multiple leaderboard entries (different game types/difficulties)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_leaderboard_player"))
    private Player player;

    /**
     * Game type for this leaderboard entry
     * IDIOM: Rankings for idiom games only
     * SENTENCE: Rankings for sentence games only
     * COMBINED: Overall rankings across both game types
     */
    @Column(nullable = false, length = 20, name = "game_type")
    @Enumerated(EnumType.STRING)
    private GameType gameType;

    /**
     * Difficulty level for this leaderboard entry
     * Each difficulty has its own leaderboard
     * COMBINED game type may use null difficulty for overall ranking
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    /**
     * Total score accumulated across all games
     * Sum of all IdiomScore or SentenceScore records
     * Updated after each completed game
     */
    @Column(nullable = false, name = "total_score")
    private Integer totalScore;

    /**
     * Total number of games played
     * Used for calculating average score and player activity
     */
    @Column(nullable = false, name = "games_played")
    private Integer gamesPlayed;

    /**
     * Average score per game
     * Calculated as: totalScore / gamesPlayed
     * Used for tiebreaker when total scores are equal
     */
    @Column(nullable = false, name = "average_score")
    private Double averageScore;

    /**
     * Current rank on this leaderboard
     * 1 = highest ranked player
     * Updated by LeaderboardService after score changes
     */
    @Column(nullable = false)
    private Integer rank;

    /**
     * Overall accuracy rate across all games
     * Average of accuracy rates from all games
     * Range: 0.0 to 1.0 (0% to 100%)
     */
    @Column(nullable = false, name = "accuracy_rate")
    private Double accuracyRate;

    /**
     * Timestamp when this leaderboard entry was last updated
     * Used for cache invalidation and freshness checks
     */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    /**
     * Updates the leaderboard entry with new game data
     *
     * @param newScore score from the latest game
     * @param newAccuracy accuracy from the latest game
     */
    public void updateWithNewGame(Integer newScore, Double newAccuracy) {
        this.totalScore += newScore;
        this.gamesPlayed += 1;
        this.averageScore = (double) this.totalScore / this.gamesPlayed;

        // Recalculate weighted average accuracy
        double totalAccuracy = this.accuracyRate * (this.gamesPlayed - 1) + newAccuracy;
        this.accuracyRate = totalAccuracy / this.gamesPlayed;

        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Gets accuracy as percentage (0-100)
     *
     * @return accuracy percentage
     */
    public double getAccuracyPercentage() {
        return accuracyRate != null ? accuracyRate * 100.0 : 0.0;
    }

    /**
     * Checks if this is a top 10 ranking
     *
     * @return true if rank is 1-10
     */
    public boolean isTopTen() {
        return rank != null && rank <= 10;
    }

    /**
     * Checks if this is the #1 ranked player
     *
     * @return true if rank is 1
     */
    public boolean isFirstPlace() {
        return rank != null && rank == 1;
    }

    /**
     * Lifecycle callback before persist
     */
    @PrePersist
    private void onPrePersist() {
        validateFields();
    }

    /**
     * Lifecycle callback before update
     */
    @PreUpdate
    private void onPreUpdate() {
        validateFields();
    }

    /**
     * Validates all fields
     */
    private void validateFields() {
        if (rank != null && rank < 1) {
            throw new IllegalArgumentException("Rank must be positive (1 or greater)");
        }
        if (gamesPlayed != null && gamesPlayed < 0) {
            throw new IllegalArgumentException("Games played cannot be negative");
        }
        if (accuracyRate != null && (accuracyRate < 0.0 || accuracyRate > 1.0)) {
            throw new IllegalArgumentException("Accuracy rate must be between 0.0 and 1.0");
        }
    }

    /**
     * Gets the best score achieved (equivalent to total score for display purposes)
     * In this implementation, we track total cumulative score
     * For best single-game score, query the score repositories directly
     *
     * @return total score
     */
    public Integer getBestScore() {
        return this.totalScore;
    }

    /**
     * Gets the total number of games played
     *
     * @return games played count
     */
    public Integer getTotalGames() {
        return this.gamesPlayed;
    }

    /**
     * Gets the timestamp when the player last played
     * Uses lastUpdated as proxy for last played time
     *
     * @return last played timestamp
     */
    public LocalDateTime getLastPlayed() {
        return this.lastUpdated;
    }
}