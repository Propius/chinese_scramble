package com.govtech.chinesescramble.entity;

import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.*;

/**
 * IdiomScore Entity - Represents a completed idiom game attempt
 *
 * Game Flow:
 * 1. Player receives scrambled Chinese idiom characters (成语)
 * 2. Player arranges characters in correct order
 * 3. System validates answer and calculates score
 * 4. IdiomScore record is created with results
 *
 * Score Calculation Factors:
 * - Base points (difficulty-dependent)
 * - Time bonus (faster completion = higher score)
 * - Accuracy rate (character placement correctness)
 * - Hint penalty (each hint used reduces score)
 *
 * UTF-8 Encoding:
 * - Idiom field stores Chinese characters (4 characters typically)
 * - Example: "一马当先" (yī mǎ dāng xiān - to take the lead)
 * - Database column configured for UTF-8 character set
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Entity
@Table(
    name = "idiom_scores",
    indexes = {
        @Index(name = "idx_idiom_score_player", columnList = "player_id"),
        @Index(name = "idx_idiom_score_difficulty", columnList = "difficulty"),
        @Index(name = "idx_idiom_score_created", columnList = "created_at"),
        @Index(name = "idx_idiom_score_score", columnList = "score")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IdiomScore extends BaseEntity {

    /**
     * Player who completed this idiom game
     * Fetch: LAZY - load player only when needed
     * Optional: false - every score must have a player
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_idiom_score_player"))
    private Player player;

    /**
     * The correct idiom (成语) that was presented
     * Stored in Chinese characters (UTF-8 encoding)
     * Example: "画蛇添足" (huà shé tiān zú - to ruin the effect by adding something superfluous)
     * Length: typically 4 characters, max 20 for rare longer idioms
     */
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) CHARACTER SET utf8mb4")
    private String idiom;

    /**
     * Final score achieved by the player
     * Calculated based on:
     * - Base points (100-500 depending on difficulty)
     * - Time bonus (faster = more points)
     * - Accuracy bonus
     * - Hint penalties
     * Range: 0 to 1000+ (no upper limit)
     */
    @Column(nullable = false)
    private Integer score;

    /**
     * Difficulty level of the idiom
     * EASY: Common idioms (e.g., "一马当先")
     * MEDIUM: Moderately difficult (e.g., "画蛇添足")
     * HARD: Advanced idioms (e.g., "守株待兔")
     * EXPERT: Rare/complex idioms
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    /**
     * Time taken to complete the idiom (in seconds)
     * Used for time bonus calculation
     * Lower time = higher bonus
     * Example: 45 seconds for MEDIUM difficulty
     */
    @Column(nullable = false, name = "time_taken")
    private Integer timeTaken;

    /**
     * Number of hints used during the game
     * Range: 0-3 (max 3 hints per question)
     * Each hint incurs a penalty:
     * - Hint 1: -10 points
     * - Hint 2: -20 points
     * - Hint 3: -30 points
     */
    @Column(nullable = false, name = "hints_used")
    @Builder.Default
    private Integer hintsUsed = 0;

    /**
     * Whether the game was completed successfully
     * true: Player submitted an answer (correct or incorrect)
     * false: Player abandoned the game (timeout or quit)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    /**
     * Accuracy rate of character placement
     * Range: 0.0 to 1.0 (0% to 100%)
     * Calculation: (correct_characters / total_characters)
     * Example: 0.75 = 3 out of 4 characters correct
     * Used in score calculation as bonus multiplier
     */
    @Column(nullable = false, name = "accuracy_rate")
    private Double accuracyRate;

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
        if (hintsUsed != null && hintsUsed > 3) {
            throw new IllegalArgumentException("Maximum 3 hints allowed per idiom game");
        }
        if (hintsUsed != null && hintsUsed < 0) {
            throw new IllegalArgumentException("Hints used cannot be negative");
        }
        if (accuracyRate != null && (accuracyRate < 0.0 || accuracyRate > 1.0)) {
            throw new IllegalArgumentException("Accuracy rate must be between 0.0 and 1.0");
        }
        if (timeTaken != null && timeTaken < 0) {
            throw new IllegalArgumentException("Time taken cannot be negative");
        }
    }

    /**
     * Checks if this is a perfect score (100% accuracy, no hints)
     *
     * @return true if perfect score
     */
    public boolean isPerfectScore() {
        return completed &&
               accuracyRate != null && accuracyRate == 1.0 &&
               hintsUsed != null && hintsUsed == 0;
    }

    /**
     * Gets the accuracy percentage (0-100)
     *
     * @return accuracy as percentage
     */
    public double getAccuracyPercentage() {
        return accuracyRate != null ? accuracyRate * 100.0 : 0.0;
    }
}