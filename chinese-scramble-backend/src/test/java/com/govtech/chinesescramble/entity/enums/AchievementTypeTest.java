package com.govtech.chinesescramble.entity.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AchievementType enum
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
class AchievementTypeTest {

    @Test
    void testFirstWinChineseTitle() {
        assertEquals("第一次胜利", AchievementType.FIRST_WIN.getChineseTitle());
    }

    @Test
    void testSpeedDemonChineseTitle() {
        assertEquals("速度之王", AchievementType.SPEED_DEMON.getChineseTitle());
    }

    @Test
    void testPerfectScoreChineseTitle() {
        assertEquals("完美主义者", AchievementType.PERFECT_SCORE.getChineseTitle());
    }

    @Test
    void testStreakChineseTitle() {
        assertEquals("连胜王", AchievementType.STREAK.getChineseTitle());
    }

    @Test
    void testDifficultyMasterChineseTitle() {
        assertEquals("难度大师", AchievementType.DIFFICULTY_MASTER.getChineseTitle());
    }

    @Test
    void testHintFreeChineseTitle() {
        assertEquals("无提示高手", AchievementType.HINT_FREE.getChineseTitle());
    }

    @Test
    void testTimeTrialChineseTitle() {
        assertEquals("时间挑战者", AchievementType.TIME_TRIAL.getChineseTitle());
    }

    @Test
    void testTotalGamesChineseTitle() {
        assertEquals("游戏达人", AchievementType.TOTAL_GAMES.getChineseTitle());
    }

    @Test
    void testHundredGamesChineseTitle() {
        assertEquals("百场达人", AchievementType.HUNDRED_GAMES.getChineseTitle());
    }

    @Test
    void testIdiomMasterChineseTitle() {
        assertEquals("成语大师", AchievementType.IDIOM_MASTER.getChineseTitle());
    }

    @Test
    void testSentenceMasterChineseTitle() {
        assertEquals("句子大师", AchievementType.SENTENCE_MASTER.getChineseTitle());
    }

    @Test
    void testTopRankedChineseTitle() {
        assertEquals("顶级玩家", AchievementType.TOP_RANKED.getChineseTitle());
    }

    @Test
    void testConsistencyChineseTitle() {
        assertEquals("坚持不懈", AchievementType.CONSISTENCY.getChineseTitle());
    }

    @Test
    void testHighScorerChineseTitle() {
        assertEquals("高分达人", AchievementType.HIGH_SCORER.getChineseTitle());
    }

    @Test
    void testFirstWinEnglishDescription() {
        assertEquals("Complete your first game successfully", AchievementType.FIRST_WIN.getEnglishDescription());
    }

    @Test
    void testSpeedDemonEnglishDescription() {
        assertEquals("Complete a game in under 30 seconds", AchievementType.SPEED_DEMON.getEnglishDescription());
    }

    @Test
    void testPerfectScoreEnglishDescription() {
        assertEquals("Complete game with perfect accuracy and no hints", AchievementType.PERFECT_SCORE.getEnglishDescription());
    }

    @ParameterizedTest
    @EnumSource(AchievementType.class)
    void testAllAchievementTypesHaveNonNullChineseTitle(AchievementType type) {
        assertNotNull(type.getChineseTitle());
        assertFalse(type.getChineseTitle().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(AchievementType.class)
    void testAllAchievementTypesHaveNonNullChineseDescription(AchievementType type) {
        assertNotNull(type.getChineseDescription());
        assertFalse(type.getChineseDescription().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(AchievementType.class)
    void testAllAchievementTypesHaveNonNullEnglishDescription(AchievementType type) {
        assertNotNull(type.getEnglishDescription());
        assertFalse(type.getEnglishDescription().isEmpty());
    }

    @Test
    void testTypeIdentifier() {
        assertEquals("FIRST_WIN", AchievementType.FIRST_WIN.getTypeIdentifier());
        assertEquals("SPEED_DEMON", AchievementType.SPEED_DEMON.getTypeIdentifier());
        assertEquals("PERFECT_SCORE", AchievementType.PERFECT_SCORE.getTypeIdentifier());
    }

    @Test
    void testIsMilestone_FirstWin() {
        assertTrue(AchievementType.FIRST_WIN.isMilestone());
    }

    @Test
    void testIsMilestone_TotalGames() {
        assertTrue(AchievementType.TOTAL_GAMES.isMilestone());
    }

    @Test
    void testIsMilestone_HundredGames() {
        assertTrue(AchievementType.HUNDRED_GAMES.isMilestone());
    }

    @Test
    void testIsMilestone_NotMilestone() {
        assertFalse(AchievementType.SPEED_DEMON.isMilestone());
        assertFalse(AchievementType.PERFECT_SCORE.isMilestone());
        assertFalse(AchievementType.STREAK.isMilestone());
    }

    @Test
    void testIsPerformance_PerfectScore() {
        assertTrue(AchievementType.PERFECT_SCORE.isPerformance());
    }

    @Test
    void testIsPerformance_SpeedDemon() {
        assertTrue(AchievementType.SPEED_DEMON.isPerformance());
    }

    @Test
    void testIsPerformance_TimeTrial() {
        assertTrue(AchievementType.TIME_TRIAL.isPerformance());
    }

    @Test
    void testIsPerformance_HighScorer() {
        assertTrue(AchievementType.HIGH_SCORER.isPerformance());
    }

    @Test
    void testIsPerformance_TopRanked() {
        assertTrue(AchievementType.TOP_RANKED.isPerformance());
    }

    @Test
    void testIsPerformance_NotPerformance() {
        assertFalse(AchievementType.FIRST_WIN.isPerformance());
        assertFalse(AchievementType.STREAK.isPerformance());
        assertFalse(AchievementType.HINT_FREE.isPerformance());
    }

    @Test
    void testIsSkill_HintFree() {
        assertTrue(AchievementType.HINT_FREE.isSkill());
    }

    @Test
    void testIsSkill_DifficultyMaster() {
        assertTrue(AchievementType.DIFFICULTY_MASTER.isSkill());
    }

    @Test
    void testIsSkill_IdiomMaster() {
        assertTrue(AchievementType.IDIOM_MASTER.isSkill());
    }

    @Test
    void testIsSkill_SentenceMaster() {
        assertTrue(AchievementType.SENTENCE_MASTER.isSkill());
    }

    @Test
    void testIsSkill_NotSkill() {
        assertFalse(AchievementType.FIRST_WIN.isSkill());
        assertFalse(AchievementType.SPEED_DEMON.isSkill());
        assertFalse(AchievementType.STREAK.isSkill());
    }

    @Test
    void testIsConsistency_Streak() {
        assertTrue(AchievementType.STREAK.isConsistency());
    }

    @Test
    void testIsConsistency_Consistency() {
        assertTrue(AchievementType.CONSISTENCY.isConsistency());
    }

    @Test
    void testIsConsistency_NotConsistency() {
        assertFalse(AchievementType.FIRST_WIN.isConsistency());
        assertFalse(AchievementType.SPEED_DEMON.isConsistency());
        assertFalse(AchievementType.HINT_FREE.isConsistency());
    }

    @Test
    void testGetDisplayString() {
        assertEquals("第一次胜利 - Complete your first game successfully",
            AchievementType.FIRST_WIN.getDisplayString());
        assertEquals("速度之王 - Complete a game in under 30 seconds",
            AchievementType.SPEED_DEMON.getDisplayString());
    }

    @ParameterizedTest
    @EnumSource(AchievementType.class)
    void testAllAchievementTypesHaveDisplayString(AchievementType type) {
        assertNotNull(type.getDisplayString());
        assertTrue(type.getDisplayString().contains(" - "));
    }

    @Test
    void testGetCategory_Milestone() {
        assertEquals("MILESTONE", AchievementType.FIRST_WIN.getCategory());
        assertEquals("MILESTONE", AchievementType.TOTAL_GAMES.getCategory());
        assertEquals("MILESTONE", AchievementType.HUNDRED_GAMES.getCategory());
    }

    @Test
    void testGetCategory_Performance() {
        assertEquals("PERFORMANCE", AchievementType.PERFECT_SCORE.getCategory());
        assertEquals("PERFORMANCE", AchievementType.SPEED_DEMON.getCategory());
        assertEquals("PERFORMANCE", AchievementType.TIME_TRIAL.getCategory());
        assertEquals("PERFORMANCE", AchievementType.HIGH_SCORER.getCategory());
        assertEquals("PERFORMANCE", AchievementType.TOP_RANKED.getCategory());
    }

    @Test
    void testGetCategory_Skill() {
        assertEquals("SKILL", AchievementType.HINT_FREE.getCategory());
        assertEquals("SKILL", AchievementType.DIFFICULTY_MASTER.getCategory());
        assertEquals("SKILL", AchievementType.IDIOM_MASTER.getCategory());
        assertEquals("SKILL", AchievementType.SENTENCE_MASTER.getCategory());
    }

    @Test
    void testGetCategory_Consistency() {
        assertEquals("CONSISTENCY", AchievementType.STREAK.getCategory());
        assertEquals("CONSISTENCY", AchievementType.CONSISTENCY.getCategory());
    }

    @Test
    void testEnumValues() {
        AchievementType[] values = AchievementType.values();
        assertEquals(14, values.length);
        assertEquals(AchievementType.FIRST_WIN, values[0]);
        assertEquals(AchievementType.HIGH_SCORER, values[13]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(AchievementType.FIRST_WIN, AchievementType.valueOf("FIRST_WIN"));
        assertEquals(AchievementType.SPEED_DEMON, AchievementType.valueOf("SPEED_DEMON"));
        assertEquals(AchievementType.PERFECT_SCORE, AchievementType.valueOf("PERFECT_SCORE"));
    }

    @Test
    void testValueOf_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> AchievementType.valueOf("INVALID"));
    }

    @Test
    void testEnumName() {
        assertEquals("FIRST_WIN", AchievementType.FIRST_WIN.name());
        assertEquals("SPEED_DEMON", AchievementType.SPEED_DEMON.name());
        assertEquals("PERFECT_SCORE", AchievementType.PERFECT_SCORE.name());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, AchievementType.FIRST_WIN.ordinal());
        assertEquals(1, AchievementType.SPEED_DEMON.ordinal());
        assertEquals(2, AchievementType.PERFECT_SCORE.ordinal());
    }

    @ParameterizedTest
    @EnumSource(AchievementType.class)
    void testAllAchievementTypesHaveCategory(AchievementType type) {
        assertNotNull(type.getCategory());
        assertTrue(type.getCategory().matches("MILESTONE|PERFORMANCE|SKILL|CONSISTENCY|OTHER"));
    }

    @Test
    void testCategoryCoverage() {
        // Verify each achievement is in exactly one category
        for (AchievementType type : AchievementType.values()) {
            int categoryCount = 0;
            if (type.isMilestone()) categoryCount++;
            if (type.isPerformance()) categoryCount++;
            if (type.isSkill()) categoryCount++;
            if (type.isConsistency()) categoryCount++;
            assertEquals(1, categoryCount, type.name() + " should be in exactly one category");
        }
    }
}
