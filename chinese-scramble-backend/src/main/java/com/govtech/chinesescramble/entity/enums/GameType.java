package com.govtech.chinesescramble.entity.enums;

/**
 * GameType - Type of word scramble game
 *
 * IDIOM (成语拼字):
 * - Player receives scrambled Chinese idiom characters
 * - Must arrange 4 characters in correct order
 * - Examples: "一马当先", "画蛇添足", "守株待兔"
 * - Focus: Character recognition and idiom knowledge
 *
 * SENTENCE (造句游戏):
 * - Player receives scrambled words to form a sentence
 * - Must arrange words in grammatically correct order
 * - Examples: "我喜欢学习中文", "他昨天去图书馆借书了"
 * - Focus: Grammar, word order, sentence structure
 *
 * COMBINED:
 * - Used for leaderboards and statistics
 * - Combines scores from both game types
 * - Provides overall player ranking
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public enum GameType {
    /**
     * Idiom scramble game (成语拼字)
     */
    IDIOM,

    /**
     * Sentence crafting game (造句游戏)
     */
    SENTENCE,

    /**
     * Combined ranking (both game types)
     */
    COMBINED;

    /**
     * Gets a human-readable name for this game type
     *
     * @return game type name
     */
    public String getDisplayName() {
        return switch (this) {
            case IDIOM -> "Idiom Scramble (成语拼字)";
            case SENTENCE -> "Sentence Crafting (造句游戏)";
            case COMBINED -> "Combined Rankings";
        };
    }

    /**
     * Gets a description of this game type
     *
     * @return game type description
     */
    public String getDescription() {
        return switch (this) {
            case IDIOM -> "Unscramble Chinese idiom characters in the correct order";
            case SENTENCE -> "Construct Chinese sentences by arranging words correctly";
            case COMBINED -> "Overall rankings combining both idiom and sentence games";
        };
    }

    /**
     * Checks if this game type represents an actual playable game
     *
     * @return true if playable (not COMBINED)
     */
    public boolean isPlayable() {
        return this != COMBINED;
    }
}