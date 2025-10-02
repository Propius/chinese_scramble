package com.govtech.chinesescramble.entity.enums;

/**
 * AchievementType - Types of achievements that can be unlocked
 *
 * Achievement System:
 * Players unlock achievements by completing specific milestones.
 * Each achievement type has specific unlock criteria.
 *
 * Achievement Categories:
 * 1. Milestone Achievements: FIRST_WIN, TOTAL_GAMES
 * 2. Performance Achievements: PERFECT_SCORE, SPEED_DEMON, TIME_TRIAL
 * 3. Skill Achievements: HINT_FREE, DIFFICULTY_MASTER
 * 4. Consistency Achievements: STREAK
 *
 * Achievement Data:
 * - Each type has a Chinese title (e.g., "第一次胜利", "速度之王")
 * - Description explains the unlock criteria
 * - Metadata stores achievement-specific data (score, time, etc.)
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public enum AchievementType {
    /**
     * FIRST_WIN - Complete first game successfully
     * Title: "第一次胜利" (First Victory)
     * Criteria: Win any game (idiom or sentence)
     */
    FIRST_WIN("第一次胜利", "完成第一个游戏", "Complete your first game successfully"),

    /**
     * SPEED_DEMON - Complete game under time threshold
     * Title: "速度之王" (Speed King)
     * Criteria: Complete game in under 30 seconds (any difficulty)
     */
    SPEED_DEMON("速度之王", "在30秒内完成游戏", "Complete a game in under 30 seconds"),

    /**
     * PERFECT_SCORE - 100% accuracy with no hints
     * Title: "完美主义者" (Perfectionist)
     * Criteria: Complete game with 100% accuracy and no hints used
     */
    PERFECT_SCORE("完美主义者", "不使用提示且完全正确", "Complete game with perfect accuracy and no hints"),

    /**
     * STREAK - Win streak achievements
     * Title: "连胜王" (Win Streak Champion)
     * Criteria: Win 5 consecutive games without losing
     */
    STREAK("连胜王", "连续胜利5场游戏", "Win 5 consecutive games"),

    /**
     * DIFFICULTY_MASTER - Complete all difficulties
     * Title: "难度大师" (Difficulty Master)
     * Criteria: Complete at least one game in each difficulty level (EASY, MEDIUM, HARD, EXPERT)
     */
    DIFFICULTY_MASTER("难度大师", "完成所有难度级别", "Complete games at all difficulty levels"),

    /**
     * HINT_FREE - Win without using hints
     * Title: "无提示高手" (No-Hint Expert)
     * Criteria: Win 10 games without using any hints
     */
    HINT_FREE("无提示高手", "不使用提示完成10场游戏", "Complete 10 games without using hints"),

    /**
     * TIME_TRIAL - Complete under time bonus threshold
     * Title: "时间挑战者" (Time Trial Champion)
     * Criteria: Complete game and earn maximum time bonus (complete in < 25% of time limit)
     */
    TIME_TRIAL("时间挑战者", "在时间限制的25%内完成游戏", "Complete game in under 25% of time limit"),

    /**
     * TOTAL_GAMES - Milestone for total games played
     * Title: "游戏达人" (Game Master)
     * Criteria: Play 100 total games (both idiom and sentence combined)
     */
    TOTAL_GAMES("游戏达人", "完成100场游戏", "Play 100 total games"),

    /**
     * HUNDRED_GAMES - Milestone for playing 100 games
     * Title: "百场达人" (Hundred Games Master)
     * Criteria: Play 100 total games (both idiom and sentence combined)
     */
    HUNDRED_GAMES("百场达人", "完成100场游戏", "Play 100 games"),

    /**
     * IDIOM_MASTER - Master all idiom difficulty levels
     * Title: "成语大师" (Idiom Master)
     * Criteria: Complete at least one idiom game in each difficulty level
     */
    IDIOM_MASTER("成语大师", "完成所有难度的成语游戏", "Master all idiom difficulty levels"),

    /**
     * SENTENCE_MASTER - Master all sentence difficulty levels
     * Title: "句子大师" (Sentence Master)
     * Criteria: Complete at least one sentence game in each difficulty level
     */
    SENTENCE_MASTER("句子大师", "完成所有难度的句子游戏", "Master all sentence difficulty levels"),

    /**
     * TOP_RANKED - Reach top position on leaderboard
     * Title: "顶级玩家" (Top Ranked Player)
     * Criteria: Reach #1 on any leaderboard (idiom or sentence)
     */
    TOP_RANKED("顶级玩家", "在排行榜上获得第一名", "Reach #1 on any leaderboard"),

    /**
     * CONSISTENCY - Play consistently over time
     * Title: "坚持不懈" (Consistency Champion)
     * Criteria: Play every day for 7 consecutive days
     */
    CONSISTENCY("坚持不懈", "连续7天每天都玩游戏", "Play every day for 7 consecutive days"),

    /**
     * HIGH_SCORER - Achieve high score in single game
     * Title: "高分达人" (High Scorer)
     * Criteria: Achieve 1000+ points in a single game
     */
    HIGH_SCORER("高分达人", "在单场游戏中获得1000分以上", "Achieve 1000+ points in a single game");

    /**
     * Chinese title for the achievement
     */
    private final String chineseTitle;

    /**
     * Chinese description of unlock criteria
     */
    private final String chineseDescription;

    /**
     * English description of unlock criteria
     */
    private final String englishDescription;

    /**
     * Constructor
     *
     * @param chineseTitle Chinese title
     * @param chineseDescription Chinese description
     * @param englishDescription English description
     */
    AchievementType(String chineseTitle, String chineseDescription, String englishDescription) {
        this.chineseTitle = chineseTitle;
        this.chineseDescription = chineseDescription;
        this.englishDescription = englishDescription;
    }

    /**
     * Gets the Chinese title
     *
     * @return Chinese title
     */
    public String getChineseTitle() {
        return chineseTitle;
    }

    /**
     * Gets the Chinese description
     *
     * @return Chinese description
     */
    public String getChineseDescription() {
        return chineseDescription;
    }

    /**
     * Gets the English description
     *
     * @return English description
     */
    public String getEnglishDescription() {
        return englishDescription;
    }

    /**
     * Gets the achievement type identifier for database storage
     *
     * @return achievement type name
     */
    public String getTypeIdentifier() {
        return this.name();
    }

    /**
     * Checks if this is a milestone achievement
     *
     * @return true if FIRST_WIN, TOTAL_GAMES, or HUNDRED_GAMES
     */
    public boolean isMilestone() {
        return this == FIRST_WIN || this == TOTAL_GAMES || this == HUNDRED_GAMES;
    }

    /**
     * Checks if this is a performance achievement
     *
     * @return true if PERFECT_SCORE, SPEED_DEMON, TIME_TRIAL, HIGH_SCORER, or TOP_RANKED
     */
    public boolean isPerformance() {
        return this == PERFECT_SCORE || this == SPEED_DEMON || this == TIME_TRIAL ||
               this == HIGH_SCORER || this == TOP_RANKED;
    }

    /**
     * Checks if this is a skill achievement
     *
     * @return true if HINT_FREE, DIFFICULTY_MASTER, IDIOM_MASTER, or SENTENCE_MASTER
     */
    public boolean isSkill() {
        return this == HINT_FREE || this == DIFFICULTY_MASTER ||
               this == IDIOM_MASTER || this == SENTENCE_MASTER;
    }

    /**
     * Checks if this is a consistency achievement
     *
     * @return true if STREAK or CONSISTENCY
     */
    public boolean isConsistency() {
        return this == STREAK || this == CONSISTENCY;
    }

    /**
     * Gets a display string for the achievement
     *
     * @return formatted achievement display (Chinese title - English description)
     */
    public String getDisplayString() {
        return String.format("%s - %s", chineseTitle, englishDescription);
    }

    /**
     * Gets the achievement category
     *
     * @return category name
     */
    public String getCategory() {
        if (isMilestone()) return "MILESTONE";
        if (isPerformance()) return "PERFORMANCE";
        if (isSkill()) return "SKILL";
        if (isConsistency()) return "CONSISTENCY";
        return "OTHER";
    }
}
