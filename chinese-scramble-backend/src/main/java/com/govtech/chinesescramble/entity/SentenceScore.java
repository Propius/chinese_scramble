package com.govtech.chinesescramble.entity;

import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.*;

/**
 * SentenceScore Entity - Represents a completed sentence crafting game attempt
 *
 * Game Flow:
 * 1. Player receives a target Chinese sentence and scrambled words
 * 2. Player constructs sentence by arranging words in correct order
 * 3. System validates grammar, word order, and meaning
 * 4. SentenceScore record is created with results
 *
 * Score Calculation Factors:
 * - Base points (difficulty-dependent)
 * - Time bonus
 * - Similarity to target sentence (Levenshtein distance or cosine similarity)
 * - Grammar correctness
 * - Hint penalties
 *
 * UTF-8 Encoding:
 * - targetSentence and playerSentence store Chinese text
 * - Example: "我喜欢学习中文" (wǒ xǐ huān xué xí zhōng wén - I like learning Chinese)
 * - validationErrors stores JSON array of error messages
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Entity
@Table(
    name = "sentence_scores",
    indexes = {
        @Index(name = "idx_sentence_score_player", columnList = "player_id"),
        @Index(name = "idx_sentence_score_difficulty", columnList = "difficulty"),
        @Index(name = "idx_sentence_score_created", columnList = "created_at"),
        @Index(name = "idx_sentence_score_score", columnList = "score")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SentenceScore extends BaseEntity {

    /**
     * Player who completed this sentence crafting game
     * Fetch: LAZY - load player only when needed
     * Optional: false - every score must have a player
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sentence_score_player"))
    private Player player;

    /**
     * The correct target sentence to construct
     * Stored in Chinese characters (UTF-8 encoding)
     * Example: "他昨天去图书馆借书了" (He went to the library to borrow books yesterday)
     * Length: up to 100 characters for complex sentences
     */
    @Column(nullable = false, length = 100, name = "target_sentence")
    private String targetSentence;

    /**
     * The sentence constructed by the player
     * May differ from target sentence in:
     * - Word order
     * - Word choice (if allowed by game rules)
     * - Grammar structure
     * Example: "昨天他去图书馆借书了" (word order variation)
     */
    @Column(nullable = false, length = 100, name = "player_sentence")
    private String playerSentence;

    /**
     * Final score achieved by the player
     * Calculated based on:
     * - Base points (100-500 depending on difficulty)
     * - Similarity score (how close to target sentence)
     * - Grammar correctness
     * - Time bonus
     * - Hint penalties
     * Range: 0 to 1000+
     */
    @Column(nullable = false)
    private Integer score;

    /**
     * Difficulty level of the sentence
     * EASY: Simple sentences (5-7 words)
     * MEDIUM: Moderate complexity (8-12 words)
     * HARD: Complex sentences (13-18 words)
     * EXPERT: Advanced grammar (19+ words)
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    /**
     * Time taken to complete the sentence (in seconds)
     * Used for time bonus calculation
     * Example: 120 seconds for MEDIUM difficulty
     */
    @Column(nullable = false, name = "time_taken")
    private Integer timeTaken;

    /**
     * Number of hints used during the game
     * Range: 0-3 (max 3 hints per question)
     * Hint types:
     * - Level 1: Show first word (-10 points)
     * - Level 2: Show sentence structure (-20 points)
     * - Level 3: Show half the sentence (-30 points)
     */
    @Column(nullable = false, name = "hints_used")
    @Builder.Default
    private Integer hintsUsed = 0;

    /**
     * Whether the game was completed successfully
     * true: Player submitted a sentence
     * false: Player abandoned the game
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    /**
     * Accuracy rate (similarity to target sentence)
     * Range: 0.0 to 1.0 (0% to 100%)
     * Calculation methods:
     * - Levenshtein distance (character-level similarity)
     * - Cosine similarity (word-level similarity)
     * - Semantic similarity (meaning preservation)
     * Example: 0.85 = 85% similar to target sentence
     */
    @Column(nullable = false, name = "accuracy_rate")
    private Double accuracyRate;

    /**
     * Grammar score for the constructed sentence
     * Evaluates grammatical correctness independent of similarity
     * Range: 0-100 (percentage of grammar rules followed)
     * Checks:
     * - Subject-verb agreement
     * - Word order correctness
     * - Proper use of particles (的、了、吗、呢、等)
     * - Sentence structure validity
     * Example: 85 = 85% grammatically correct
     */
    @Column(name = "grammar_score")
    private Integer grammarScore;

    /**
     * Similarity score to target sentence
     * Measures how close the player's sentence is to the target
     * Range: 0.0 to 1.0 (0% to 100%)
     * Calculation methods:
     * - Levenshtein distance
     * - Word order similarity
     * - Semantic similarity
     * Example: 0.92 = 92% similar
     */
    @Column(nullable = false, name = "similarity_score")
    private Double similarityScore;

    /**
     * Validation errors found in player's sentence
     * Stored as JSON array of error objects
     * Example: [{"type":"grammar","position":3,"message":"Incorrect word order"}]
     * NULL if no errors found
     * Used for providing feedback to player
     */
    @Column(columnDefinition = "TEXT", name = "validation_errors")
    private String validationErrors;

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
            throw new IllegalArgumentException("Maximum 3 hints allowed per sentence game");
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
     * Checks if this is a perfect score (100% accuracy, no hints, no errors)
     *
     * @return true if perfect score
     */
    public boolean isPerfectScore() {
        return completed &&
               accuracyRate != null && accuracyRate == 1.0 &&
               hintsUsed != null && hintsUsed == 0 &&
               (validationErrors == null || validationErrors.isEmpty());
    }

    /**
     * Gets the accuracy percentage (0-100)
     *
     * @return accuracy as percentage
     */
    public double getAccuracyPercentage() {
        return accuracyRate != null ? accuracyRate * 100.0 : 0.0;
    }

    /**
     * Checks if there are any validation errors
     *
     * @return true if validation errors exist
     */
    public boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }

    /**
     * Gets the similarity to target sentence
     *
     * @return similarity score 0.0 to 1.0
     */
    public double getSimilarityScoreValue() {
        return similarityScore != null ? similarityScore : 0.0;
    }
}