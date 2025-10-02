package com.govtech.chinesescramble.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * HintUsage Entity - Records when a player uses a hint during a game
 *
 * Hint System:
 * Players can request up to 3 hints per game, each with increasing penalties:
 *
 * Level 1 Hint (-10 points):
 * - Idiom Game: Shows the first character
 * - Sentence Game: Shows the first word
 *
 * Level 2 Hint (-20 points):
 * - Idiom Game: Shows the first two characters
 * - Sentence Game: Shows the sentence structure/pattern
 *
 * Level 3 Hint (-30 points):
 * - Idiom Game: Shows three characters (only one missing)
 * - Sentence Game: Shows half of the sentence
 *
 * Data Collection:
 * - Tracks which hints are most used
 * - Analyzes hint effectiveness
 * - Identifies difficult questions (high hint usage)
 * - Used for game balance and difficulty tuning
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Entity
@Table(
    name = "hint_usage",
    indexes = {
        @Index(name = "idx_hint_session", columnList = "game_session_id"),
        @Index(name = "idx_hint_level", columnList = "hint_level"),
        @Index(name = "idx_hint_used_at", columnList = "used_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = "gameSession")
@ToString(exclude = "gameSession")
public class HintUsage extends BaseEntity {

    /**
     * Game session during which this hint was used
     * Each hint is tied to a specific game session
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_hint_session"))
    private GameSession gameSession;

    /**
     * Hint level requested by the player
     * Range: 1-3
     * 1 = Least helpful, smallest penalty
     * 2 = Moderately helpful, medium penalty
     * 3 = Most helpful, largest penalty
     */
    @Column(nullable = false, name = "hint_level")
    private Integer hintLevel;

    /**
     * Score penalty applied for using this hint
     * Level 1: -10 points
     * Level 2: -20 points
     * Level 3: -30 points
     * Deducted from final score calculation
     */
    @Column(nullable = false, name = "penalty_applied")
    private Integer penaltyApplied;

    /**
     * Timestamp when the hint was used
     * Used for analytics and gameplay timeline
     */
    @Column(nullable = false, name = "used_at")
    private LocalDateTime usedAt;

    /**
     * The actual hint content shown to the player
     * Stored for analytics and replay purposes
     * Examples:
     * - "First character: 一" (Level 1 idiom hint)
     * - "First word: 我" (Level 1 sentence hint)
     * - "Structure: Subject + Verb + Object" (Level 2 sentence hint)
     */
    @Column(length = 255, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4", name = "hint_content")
    private String hintContent;

    /**
     * Lifecycle callback before persist
     */
    @PrePersist
    private void onPrePersist() {
        if (usedAt == null) {
            usedAt = LocalDateTime.now();
        }
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
        if (hintLevel == null || hintLevel < 1 || hintLevel > 3) {
            throw new IllegalArgumentException("Hint level must be 1, 2, or 3");
        }
        if (penaltyApplied == null || penaltyApplied < 0) {
            throw new IllegalArgumentException("Penalty must be a positive value (deducted from score)");
        }
    }

    /**
     * Gets the expected penalty for a given hint level
     *
     * @param level the hint level (1-3)
     * @return the penalty amount
     */
    public static int getPenaltyForLevel(int level) {
        return switch (level) {
            case 1 -> 10;
            case 2 -> 20;
            case 3 -> 30;
            default -> throw new IllegalArgumentException("Invalid hint level: " + level);
        };
    }

    /**
     * Checks if this was a Level 1 hint
     *
     * @return true if hint level is 1
     */
    public boolean isLevel1() {
        return hintLevel != null && hintLevel == 1;
    }

    /**
     * Checks if this was a Level 2 hint
     *
     * @return true if hint level is 2
     */
    public boolean isLevel2() {
        return hintLevel != null && hintLevel == 2;
    }

    /**
     * Checks if this was a Level 3 hint (most helpful)
     *
     * @return true if hint level is 3
     */
    public boolean isLevel3() {
        return hintLevel != null && hintLevel == 3;
    }

    /**
     * Checks if this hint was used recently (within 1 minute)
     *
     * @return true if used in the last minute
     */
    public boolean isRecent() {
        if (usedAt == null) return false;
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        return usedAt.isAfter(oneMinuteAgo);
    }
}