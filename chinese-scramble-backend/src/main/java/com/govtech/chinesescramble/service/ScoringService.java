package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ScoringService - Calculates scores for game results
 *
 * Scoring System:
 *
 * Base Score (by difficulty):
 * - EASY: 100 points
 * - MEDIUM: 200 points
 * - HARD: 300 points
 * - EXPERT: 500 points
 *
 * Time Bonuses:
 * - Completed in < 30s: +50 points
 * - Completed in < 60s: +30 points
 * - Completed in < 90s: +15 points
 *
 * Accuracy Bonuses:
 * - 100% accuracy: +100 points
 * - 95-99% accuracy: +50 points
 * - 90-94% accuracy: +25 points
 *
 * Hint Penalties:
 * - Level 1 hint: -10 points
 * - Level 2 hint: -20 points
 * - Level 3 hint: -30 points
 *
 * Difficulty Multiplier:
 * Final score = (Base + Bonuses - Penalties) × Multiplier
 * - EASY: 1.0x
 * - MEDIUM: 1.2x
 * - HARD: 1.5x
 * - EXPERT: 2.0x
 *
 * Maximum Score Examples:
 * - EASY perfect (no hints, <30s): (100 + 50 + 100) × 1.0 = 250
 * - MEDIUM perfect: (200 + 50 + 100) × 1.2 = 420
 * - HARD perfect: (300 + 50 + 100) × 1.5 = 675
 * - EXPERT perfect: (500 + 50 + 100) × 2.0 = 1300
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScoringService {

    /**
     * Calculates idiom game score
     *
     * @param difficulty game difficulty
     * @param timeTaken time taken in seconds
     * @param accuracyRate accuracy rate (0.0 to 1.0)
     * @param hintsUsed number of hints used (0-3)
     * @return calculated score
     */
    public int calculateIdiomScore(
        DifficultyLevel difficulty,
        int timeTaken,
        double accuracyRate,
        int hintsUsed
    ) {
        log.debug("Calculating idiom score: difficulty={}, time={}s, accuracy={}, hints={}",
            difficulty, timeTaken, accuracyRate, hintsUsed);

        // 1. Base score
        int baseScore = difficulty.getBasePoints();
        log.debug("Base score: {}", baseScore);

        // 2. Time bonus
        int timeBonus = calculateTimeBonus(timeTaken);
        log.debug("Time bonus: {} (time: {}s)", timeBonus, timeTaken);

        // 3. Accuracy bonus
        int accuracyBonus = calculateAccuracyBonus(accuracyRate);
        log.debug("Accuracy bonus: {} (accuracy: {})", accuracyBonus, accuracyRate);

        // 4. Hint penalty
        int hintPenalty = calculateHintPenalty(hintsUsed);
        log.debug("Hint penalty: {} (hints used: {})", hintPenalty, hintsUsed);

        // 5. Calculate raw score
        int rawScore = baseScore + timeBonus + accuracyBonus - hintPenalty;
        log.debug("Raw score: {}", rawScore);

        // 6. Apply difficulty multiplier
        double multiplier = difficulty.getDifficultyMultiplier();
        int finalScore = (int) (rawScore * multiplier);
        log.debug("Final score: {} (multiplier: {}x)", finalScore, multiplier);

        // 7. Ensure score is non-negative
        finalScore = Math.max(0, finalScore);

        log.info("Idiom score calculated: {} points", finalScore);
        return finalScore;
    }

    /**
     * Calculates sentence game score
     *
     * @param difficulty game difficulty
     * @param timeTaken time taken in seconds
     * @param accuracyRate accuracy rate (0.0 to 1.0)
     * @param grammarScore grammar score (0-100)
     * @param hintsUsed number of hints used (0-3)
     * @return calculated score
     */
    public int calculateSentenceScore(
        DifficultyLevel difficulty,
        int timeTaken,
        double accuracyRate,
        int grammarScore,
        int hintsUsed
    ) {
        log.debug("Calculating sentence score: difficulty={}, time={}s, accuracy={}, grammar={}, hints={}",
            difficulty, timeTaken, accuracyRate, grammarScore, hintsUsed);

        // 1. Base score
        int baseScore = difficulty.getBasePoints();
        log.debug("Base score: {}", baseScore);

        // 2. Time bonus
        int timeBonus = calculateTimeBonus(timeTaken);
        log.debug("Time bonus: {} (time: {}s)", timeBonus, timeTaken);

        // 3. Accuracy bonus
        int accuracyBonus = calculateAccuracyBonus(accuracyRate);
        log.debug("Accuracy bonus: {} (accuracy: {})", accuracyBonus, accuracyRate);

        // 4. Grammar bonus (unique to sentence game)
        int grammarBonus = calculateGrammarBonus(grammarScore);
        log.debug("Grammar bonus: {} (grammar score: {})", grammarBonus, grammarScore);

        // 5. Hint penalty
        int hintPenalty = calculateHintPenalty(hintsUsed);
        log.debug("Hint penalty: {} (hints used: {})", hintPenalty, hintsUsed);

        // 6. Calculate raw score
        int rawScore = baseScore + timeBonus + accuracyBonus + grammarBonus - hintPenalty;
        log.debug("Raw score: {}", rawScore);

        // 7. Apply difficulty multiplier
        double multiplier = difficulty.getDifficultyMultiplier();
        int finalScore = (int) (rawScore * multiplier);
        log.debug("Final score: {} (multiplier: {}x)", finalScore, multiplier);

        // 8. Ensure score is non-negative
        finalScore = Math.max(0, finalScore);

        log.info("Sentence score calculated: {} points", finalScore);
        return finalScore;
    }

    /**
     * Calculates time bonus based on completion time
     *
     * @param timeTaken time in seconds
     * @return time bonus points
     */
    private int calculateTimeBonus(int timeTaken) {
        if (timeTaken < 30) {
            return 50; // Speed demon!
        } else if (timeTaken < 60) {
            return 30; // Fast
        } else if (timeTaken < 90) {
            return 15; // Good pace
        } else {
            return 0; // No bonus
        }
    }

    /**
     * Calculates accuracy bonus
     *
     * @param accuracyRate accuracy (0.0 to 1.0)
     * @return accuracy bonus points
     */
    private int calculateAccuracyBonus(double accuracyRate) {
        if (accuracyRate >= 1.0) {
            return 100; // Perfect!
        } else if (accuracyRate >= 0.95) {
            return 50; // Excellent
        } else if (accuracyRate >= 0.90) {
            return 25; // Good
        } else {
            return 0; // No bonus
        }
    }

    /**
     * Calculates grammar bonus for sentence games
     *
     * @param grammarScore grammar score (0-100)
     * @return grammar bonus points
     */
    private int calculateGrammarBonus(int grammarScore) {
        if (grammarScore >= 95) {
            return 50; // Excellent grammar
        } else if (grammarScore >= 85) {
            return 25; // Good grammar
        } else if (grammarScore >= 75) {
            return 10; // Acceptable grammar
        } else {
            return 0; // No bonus
        }
    }

    /**
     * Calculates hint penalty
     *
     * @param hintsUsed number of hints (0-3)
     * @return penalty points (positive value to subtract)
     */
    private int calculateHintPenalty(int hintsUsed) {
        return switch (hintsUsed) {
            case 0 -> 0;   // No hints used
            case 1 -> 10;  // Level 1 hint
            case 2 -> 30;  // Level 1 + Level 2 hints (10 + 20)
            case 3 -> 60;  // All hints (10 + 20 + 30)
            default -> 0;
        };
    }

    /**
     * Gets penalty for a specific hint level
     *
     * @param hintLevel hint level (1-3)
     * @return penalty for this hint level
     */
    public int getHintPenalty(int hintLevel) {
        return switch (hintLevel) {
            case 1 -> 10;
            case 2 -> 20;
            case 3 -> 30;
            default -> throw new IllegalArgumentException("Invalid hint level: " + hintLevel);
        };
    }

    /**
     * Calculates maximum possible score for difficulty
     *
     * @param difficulty game difficulty
     * @return maximum achievable score
     */
    public int getMaximumScore(DifficultyLevel difficulty) {
        // Perfect game: no hints, < 30s, 100% accuracy
        int baseScore = difficulty.getBasePoints();
        int perfectBonuses = 50 + 100; // Time + Accuracy
        int rawScore = baseScore + perfectBonuses;
        return (int) (rawScore * difficulty.getDifficultyMultiplier());
    }

    /**
     * Calculates score breakdown for display
     *
     * @param difficulty game difficulty
     * @param timeTaken time in seconds
     * @param accuracyRate accuracy (0.0 to 1.0)
     * @param hintsUsed hints used (0-3)
     * @param grammarScore grammar score (optional, for sentences)
     * @return score breakdown
     */
    public ScoreBreakdown getScoreBreakdown(
        DifficultyLevel difficulty,
        int timeTaken,
        double accuracyRate,
        int hintsUsed,
        Integer grammarScore
    ) {
        int baseScore = difficulty.getBasePoints();
        int timeBonus = calculateTimeBonus(timeTaken);
        int accuracyBonus = calculateAccuracyBonus(accuracyRate);
        int hintPenalty = calculateHintPenalty(hintsUsed);
        int grammarBonus = grammarScore != null ? calculateGrammarBonus(grammarScore) : 0;

        int rawScore = baseScore + timeBonus + accuracyBonus + grammarBonus - hintPenalty;
        double multiplier = difficulty.getDifficultyMultiplier();
        int finalScore = Math.max(0, (int) (rawScore * multiplier));

        return new ScoreBreakdown(
            baseScore,
            timeBonus,
            accuracyBonus,
            grammarBonus,
            hintPenalty,
            rawScore,
            multiplier,
            finalScore
        );
    }

    // ========================================================================
    // DTOs
    // ========================================================================

    /**
     * Score breakdown for display
     */
    public record ScoreBreakdown(
        int baseScore,
        int timeBonus,
        int accuracyBonus,
        int grammarBonus,
        int hintPenalty,
        int rawScore,
        double multiplier,
        int finalScore
    ) {
        public boolean isPerfectScore() {
            return hintPenalty == 0 && timeBonus == 50 && accuracyBonus == 100;
        }

        public String getTimeDescription() {
            return switch (timeBonus) {
                case 50 -> "闪电般的速度！(< 30秒)";
                case 30 -> "速度很快！(< 60秒)";
                case 15 -> "速度不错！(< 90秒)";
                default -> "可以更快！";
            };
        }

        public String getAccuracyDescription() {
            return switch (accuracyBonus) {
                case 100 -> "完美无缺！(100%)";
                case 50 -> "非常准确！(95-99%)";
                case 25 -> "相当不错！(90-94%)";
                default -> "继续努力！";
            };
        }
    }
}