package com.govtech.chinesescramble.entity.enums;

/**
 * ConfigType - Type of configuration data cached in database
 *
 * IDIOM:
 * - Idiom configuration data (idioms.json)
 * - Contains 50+ Chinese idioms with metadata
 * - Cached for fast access and hot-reload capability
 *
 * SENTENCE:
 * - Sentence configuration data (sentences.json)
 * - Contains 25+ Chinese sentences with metadata
 * - Cached for fast access and hot-reload capability
 *
 * FEATURE_FLAG:
 * - Feature flag configuration
 * - Controls which features are enabled/disabled
 * - Can be modified at runtime without restart
 *
 * GAME_SETTING:
 * - General game settings and parameters
 * - Scoring multipliers, time limits, etc.
 * - Cached for performance
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public enum ConfigType {
    /**
     * Idiom game configuration
     */
    IDIOM,

    /**
     * Sentence game configuration
     */
    SENTENCE,

    /**
     * Feature flag configuration
     */
    FEATURE_FLAG,

    /**
     * General game settings
     */
    GAME_SETTING;

    /**
     * Gets the configuration file path for this type
     *
     * @return file path relative to classpath
     */
    public String getFilePath() {
        return switch (this) {
            case IDIOM -> "config/idioms.json";
            case SENTENCE -> "config/sentences.json";
            case FEATURE_FLAG -> null; // stored in database only
            case GAME_SETTING -> "config/game-settings.json";
        };
    }

    /**
     * Checks if this config type is loaded from a file
     *
     * @return true if loaded from file
     */
    public boolean isFileBasedConfig() {
        return this != FEATURE_FLAG;
    }

    /**
     * Gets a human-readable description
     *
     * @return config type description
     */
    public String getDescription() {
        return switch (this) {
            case IDIOM -> "Chinese idiom configurations (成语)";
            case SENTENCE -> "Chinese sentence configurations (造句)";
            case FEATURE_FLAG -> "Feature toggle flags";
            case GAME_SETTING -> "General game settings and parameters";
        };
    }
}