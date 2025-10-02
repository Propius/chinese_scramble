package com.govtech.chinesescramble.entity.enums;

/**
 * DifficultyLevel - Game difficulty levels for Idiom and Sentence games
 *
 * Each level has different characteristics:
 *
 * EASY:
 * - Common idioms/simple sentences
 * - 3-minute time limit (180 seconds)
 * - Base points: 100
 * - Target audience: Beginners, HSK 1-2 level
 *
 * MEDIUM:
 * - Moderately difficult idioms/sentences
 * - 2-minute time limit (120 seconds)
 * - Base points: 200
 * - Target audience: Intermediate, HSK 3-4 level
 *
 * HARD:
 * - Advanced idioms/complex sentences
 * - 1.5-minute time limit (90 seconds)
 * - Base points: 300
 * - Target audience: Advanced, HSK 5 level
 *
 * EXPERT:
 * - Rare idioms/very complex sentences
 * - 1-minute time limit (60 seconds)
 * - Base points: 500
 * - Target audience: Native/near-native, HSK 6 level
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public enum DifficultyLevel {
    /**
     * Easy difficulty - for beginners
     */
    EASY,

    /**
     * Medium difficulty - for intermediate learners
     */
    MEDIUM,

    /**
     * Hard difficulty - for advanced learners
     */
    HARD,

    /**
     * Expert difficulty - for native/near-native speakers
     */
    EXPERT;

    /**
     * Gets the time limit in seconds for this difficulty level
     *
     * @return time limit in seconds
     */
    public int getTimeLimitSeconds() {
        return switch (this) {
            case EASY -> 180;    // 3 minutes
            case MEDIUM -> 120;  // 2 minutes
            case HARD -> 90;     // 1.5 minutes
            case EXPERT -> 60;   // 1 minute
        };
    }

    /**
     * Gets the base points for this difficulty level
     *
     * @return base points
     */
    public int getBasePoints() {
        return switch (this) {
            case EASY -> 100;
            case MEDIUM -> 200;
            case HARD -> 300;
            case EXPERT -> 500;
        };
    }

    /**
     * Gets the difficulty multiplier for score calculation
     *
     * @return difficulty multiplier
     */
    public double getDifficultyMultiplier() {
        return switch (this) {
            case EASY -> 1.0;
            case MEDIUM -> 1.5;
            case HARD -> 2.0;
            case EXPERT -> 3.0;
        };
    }

    /**
     * Gets a human-readable description of this difficulty
     *
     * @return description
     */
    public String getDescription() {
        return switch (this) {
            case EASY -> "Easy (Beginner - HSK 1-2)";
            case MEDIUM -> "Medium (Intermediate - HSK 3-4)";
            case HARD -> "Hard (Advanced - HSK 5)";
            case EXPERT -> "Expert (Native - HSK 6)";
        };
    }

    /**
     * Gets the Chinese label for this difficulty level
     *
     * @return Chinese label
     */
    public String getLabel() {
        return switch (this) {
            case EASY -> "简单";
            case MEDIUM -> "中等";
            case HARD -> "困难";
            case EXPERT -> "专家";
        };
    }
}