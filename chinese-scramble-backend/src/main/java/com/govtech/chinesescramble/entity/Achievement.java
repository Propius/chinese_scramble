package com.govtech.chinesescramble.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Achievement Entity - Represents an unlocked achievement for a player
 *
 * Achievement System:
 * Players unlock achievements by completing specific milestones:
 * - FIRST_WIN: Complete first game successfully
 * - SPEED_DEMON: Complete game in under 30 seconds
 * - PERFECT_SCORE: 100% accuracy with no hints
 * - HUNDRED_GAMES: Play 100 games
 * - IDIOM_MASTER: Master all idiom difficulties
 * - SENTENCE_MASTER: Master all sentence difficulties
 * - TOP_RANKED: Reach #1 on any leaderboard
 * - CONSISTENCY: Play every day for 7 days
 * - HIGH_SCORER: Achieve 1000+ points in a single game
 * - HINT_FREE: Complete 10 games without using hints
 *
 * Achievement Data:
 * - title: Chinese title for the achievement (e.g., "速度之王")
 * - description: Chinese description
 * - metadata: JSON string with additional data (e.g., {"score": 1500, "time": 25})
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Entity
@Table(
    name = "achievements",
    indexes = {
        @Index(name = "idx_achievement_player", columnList = "player_id"),
        @Index(name = "idx_achievement_type", columnList = "achievement_type"),
        @Index(name = "idx_achievement_unlocked", columnList = "unlocked_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Achievement extends BaseEntity {

    /**
     * Player who unlocked this achievement
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_achievement_player"))
    private Player player;

    /**
     * Achievement type identifier
     * Examples: FIRST_WIN, SPEED_DEMON, PERFECT_SCORE, etc.
     * Used for uniqueness checking and achievement logic
     */
    @Column(nullable = false, length = 50, name = "achievement_type")
    private String achievementType;

    /**
     * Achievement title in Chinese
     * Displayed to the player when unlocked
     * Example: "速度之王" (Speed King), "完美主义者" (Perfectionist)
     */
    @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4")
    private String title;

    /**
     * Achievement description in Chinese
     * Explains what the player did to earn this
     * Example: "在30秒内完成一个成语游戏" (Complete an idiom game in under 30 seconds)
     */
    @Column(nullable = false, length = 255, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
    private String description;

    /**
     * Timestamp when the achievement was unlocked
     * Immutable - once unlocked, cannot be changed
     */
    @Column(nullable = false, updatable = false, name = "unlocked_at")
    private LocalDateTime unlockedAt;

    /**
     * Optional metadata about how the achievement was earned
     * Stored as JSON string for flexibility
     * Examples:
     * - {"score": 1500, "time": 25} for SPEED_DEMON
     * - {"streak_days": 7} for CONSISTENCY
     * - {"total_games": 100} for HUNDRED_GAMES
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * Sets unlocked timestamp to current time on entity creation
     */
    @PrePersist
    private void setUnlockedAt() {
        if (unlockedAt == null) {
            unlockedAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if this achievement was unlocked today
     *
     * @return true if unlocked today
     */
    public boolean isUnlockedToday() {
        if (unlockedAt == null) return false;
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        return unlockedAt.isAfter(today);
    }

    /**
     * Checks if this achievement was unlocked recently (within 24 hours)
     *
     * @return true if unlocked in the last 24 hours
     */
    public boolean isRecentlyUnlocked() {
        if (unlockedAt == null) return false;
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return unlockedAt.isAfter(twentyFourHoursAgo);
    }

    /**
     * Gets a display string for the achievement
     *
     * @return formatted achievement display
     */
    public String getDisplayString() {
        return String.format("%s - %s", title, description);
    }
}