package com.govtech.chinesescramble.entity.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GameType enum
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
class GameTypeTest {

    @Test
    void testIdiomDisplayName() {
        assertEquals("Idiom Scramble (成语拼字)", GameType.IDIOM.getDisplayName());
    }

    @Test
    void testSentenceDisplayName() {
        assertEquals("Sentence Crafting (造句游戏)", GameType.SENTENCE.getDisplayName());
    }

    @Test
    void testCombinedDisplayName() {
        assertEquals("Combined Rankings", GameType.COMBINED.getDisplayName());
    }

    @Test
    void testIdiomDescription() {
        assertEquals("Unscramble Chinese idiom characters in the correct order",
            GameType.IDIOM.getDescription());
    }

    @Test
    void testSentenceDescription() {
        assertEquals("Construct Chinese sentences by arranging words correctly",
            GameType.SENTENCE.getDescription());
    }

    @Test
    void testCombinedDescription() {
        assertEquals("Overall rankings combining both idiom and sentence games",
            GameType.COMBINED.getDescription());
    }

    @Test
    void testIsPlayable_Idiom() {
        assertTrue(GameType.IDIOM.isPlayable());
    }

    @Test
    void testIsPlayable_Sentence() {
        assertTrue(GameType.SENTENCE.isPlayable());
    }

    @Test
    void testIsPlayable_Combined() {
        assertFalse(GameType.COMBINED.isPlayable());
    }

    @ParameterizedTest
    @EnumSource(GameType.class)
    void testAllGameTypesHaveNonNullDisplayName(GameType type) {
        assertNotNull(type.getDisplayName());
        assertFalse(type.getDisplayName().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(GameType.class)
    void testAllGameTypesHaveNonNullDescription(GameType type) {
        assertNotNull(type.getDescription());
        assertFalse(type.getDescription().isEmpty());
    }

    @Test
    void testEnumValues() {
        GameType[] values = GameType.values();
        assertEquals(3, values.length);
        assertEquals(GameType.IDIOM, values[0]);
        assertEquals(GameType.SENTENCE, values[1]);
        assertEquals(GameType.COMBINED, values[2]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(GameType.IDIOM, GameType.valueOf("IDIOM"));
        assertEquals(GameType.SENTENCE, GameType.valueOf("SENTENCE"));
        assertEquals(GameType.COMBINED, GameType.valueOf("COMBINED"));
    }

    @Test
    void testValueOf_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> GameType.valueOf("INVALID"));
    }

    @Test
    void testEnumName() {
        assertEquals("IDIOM", GameType.IDIOM.name());
        assertEquals("SENTENCE", GameType.SENTENCE.name());
        assertEquals("COMBINED", GameType.COMBINED.name());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, GameType.IDIOM.ordinal());
        assertEquals(1, GameType.SENTENCE.ordinal());
        assertEquals(2, GameType.COMBINED.ordinal());
    }

    @Test
    void testPlayableGameTypes() {
        long playableCount = java.util.Arrays.stream(GameType.values())
            .filter(GameType::isPlayable)
            .count();
        assertEquals(2, playableCount);
    }
}
