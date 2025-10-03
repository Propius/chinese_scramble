package com.govtech.chinesescramble.entity.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DifficultyLevel enum
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
class DifficultyLevelTest {

    @Test
    void testEasyTimeLimitSeconds() {
        assertEquals(180, DifficultyLevel.EASY.getTimeLimitSeconds());
    }

    @Test
    void testMediumTimeLimitSeconds() {
        assertEquals(120, DifficultyLevel.MEDIUM.getTimeLimitSeconds());
    }

    @Test
    void testHardTimeLimitSeconds() {
        assertEquals(90, DifficultyLevel.HARD.getTimeLimitSeconds());
    }

    @Test
    void testExpertTimeLimitSeconds() {
        assertEquals(60, DifficultyLevel.EXPERT.getTimeLimitSeconds());
    }

    @Test
    void testEasyBasePoints() {
        assertEquals(100, DifficultyLevel.EASY.getBasePoints());
    }

    @Test
    void testMediumBasePoints() {
        assertEquals(200, DifficultyLevel.MEDIUM.getBasePoints());
    }

    @Test
    void testHardBasePoints() {
        assertEquals(300, DifficultyLevel.HARD.getBasePoints());
    }

    @Test
    void testExpertBasePoints() {
        assertEquals(500, DifficultyLevel.EXPERT.getBasePoints());
    }

    @Test
    void testEasyDifficultyMultiplier() {
        assertEquals(1.0, DifficultyLevel.EASY.getDifficultyMultiplier());
    }

    @Test
    void testMediumDifficultyMultiplier() {
        assertEquals(1.5, DifficultyLevel.MEDIUM.getDifficultyMultiplier());
    }

    @Test
    void testHardDifficultyMultiplier() {
        assertEquals(2.0, DifficultyLevel.HARD.getDifficultyMultiplier());
    }

    @Test
    void testExpertDifficultyMultiplier() {
        assertEquals(3.0, DifficultyLevel.EXPERT.getDifficultyMultiplier());
    }

    @Test
    void testEasyDescription() {
        assertEquals("Easy (Beginner - HSK 1-2)", DifficultyLevel.EASY.getDescription());
    }

    @Test
    void testMediumDescription() {
        assertEquals("Medium (Intermediate - HSK 3-4)", DifficultyLevel.MEDIUM.getDescription());
    }

    @Test
    void testHardDescription() {
        assertEquals("Hard (Advanced - HSK 5)", DifficultyLevel.HARD.getDescription());
    }

    @Test
    void testExpertDescription() {
        assertEquals("Expert (Native - HSK 6)", DifficultyLevel.EXPERT.getDescription());
    }

    @Test
    void testEasyLabel() {
        assertEquals("简单", DifficultyLevel.EASY.getLabel());
    }

    @Test
    void testMediumLabel() {
        assertEquals("中等", DifficultyLevel.MEDIUM.getLabel());
    }

    @Test
    void testHardLabel() {
        assertEquals("困难", DifficultyLevel.HARD.getLabel());
    }

    @Test
    void testExpertLabel() {
        assertEquals("专家", DifficultyLevel.EXPERT.getLabel());
    }

    @Test
    void testEnumValues() {
        DifficultyLevel[] values = DifficultyLevel.values();
        assertEquals(4, values.length);
        assertEquals(DifficultyLevel.EASY, values[0]);
        assertEquals(DifficultyLevel.MEDIUM, values[1]);
        assertEquals(DifficultyLevel.HARD, values[2]);
        assertEquals(DifficultyLevel.EXPERT, values[3]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(DifficultyLevel.EASY, DifficultyLevel.valueOf("EASY"));
        assertEquals(DifficultyLevel.MEDIUM, DifficultyLevel.valueOf("MEDIUM"));
        assertEquals(DifficultyLevel.HARD, DifficultyLevel.valueOf("HARD"));
        assertEquals(DifficultyLevel.EXPERT, DifficultyLevel.valueOf("EXPERT"));
    }

    @Test
    void testValueOf_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> DifficultyLevel.valueOf("INVALID"));
    }

    @ParameterizedTest
    @EnumSource(DifficultyLevel.class)
    void testAllDifficultyLevelsHavePositiveTimeLimit(DifficultyLevel level) {
        assertTrue(level.getTimeLimitSeconds() > 0);
    }

    @ParameterizedTest
    @EnumSource(DifficultyLevel.class)
    void testAllDifficultyLevelsHavePositiveBasePoints(DifficultyLevel level) {
        assertTrue(level.getBasePoints() > 0);
    }

    @ParameterizedTest
    @EnumSource(DifficultyLevel.class)
    void testAllDifficultyLevelsHavePositiveMultiplier(DifficultyLevel level) {
        assertTrue(level.getDifficultyMultiplier() > 0);
    }

    @ParameterizedTest
    @EnumSource(DifficultyLevel.class)
    void testAllDifficultyLevelsHaveNonNullDescription(DifficultyLevel level) {
        assertNotNull(level.getDescription());
        assertFalse(level.getDescription().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(DifficultyLevel.class)
    void testAllDifficultyLevelsHaveNonNullLabel(DifficultyLevel level) {
        assertNotNull(level.getLabel());
        assertFalse(level.getLabel().isEmpty());
    }

    @Test
    void testDifficultyProgression_TimeDecreases() {
        assertTrue(DifficultyLevel.EASY.getTimeLimitSeconds() > DifficultyLevel.MEDIUM.getTimeLimitSeconds());
        assertTrue(DifficultyLevel.MEDIUM.getTimeLimitSeconds() > DifficultyLevel.HARD.getTimeLimitSeconds());
        assertTrue(DifficultyLevel.HARD.getTimeLimitSeconds() > DifficultyLevel.EXPERT.getTimeLimitSeconds());
    }

    @Test
    void testDifficultyProgression_PointsIncrease() {
        assertTrue(DifficultyLevel.EASY.getBasePoints() < DifficultyLevel.MEDIUM.getBasePoints());
        assertTrue(DifficultyLevel.MEDIUM.getBasePoints() < DifficultyLevel.HARD.getBasePoints());
        assertTrue(DifficultyLevel.HARD.getBasePoints() < DifficultyLevel.EXPERT.getBasePoints());
    }

    @Test
    void testDifficultyProgression_MultiplierIncreases() {
        assertTrue(DifficultyLevel.EASY.getDifficultyMultiplier() < DifficultyLevel.MEDIUM.getDifficultyMultiplier());
        assertTrue(DifficultyLevel.MEDIUM.getDifficultyMultiplier() < DifficultyLevel.HARD.getDifficultyMultiplier());
        assertTrue(DifficultyLevel.HARD.getDifficultyMultiplier() < DifficultyLevel.EXPERT.getDifficultyMultiplier());
    }

    @Test
    void testEnumName() {
        assertEquals("EASY", DifficultyLevel.EASY.name());
        assertEquals("MEDIUM", DifficultyLevel.MEDIUM.name());
        assertEquals("HARD", DifficultyLevel.HARD.name());
        assertEquals("EXPERT", DifficultyLevel.EXPERT.name());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, DifficultyLevel.EASY.ordinal());
        assertEquals(1, DifficultyLevel.MEDIUM.ordinal());
        assertEquals(2, DifficultyLevel.HARD.ordinal());
        assertEquals(3, DifficultyLevel.EXPERT.ordinal());
    }
}
