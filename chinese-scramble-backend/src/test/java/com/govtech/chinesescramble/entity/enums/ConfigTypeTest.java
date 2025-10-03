package com.govtech.chinesescramble.entity.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigType enum
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
class ConfigTypeTest {

    @Test
    void testGetFilePath_Idiom() {
        assertEquals("config/idioms.json", ConfigType.IDIOM.getFilePath());
    }

    @Test
    void testGetFilePath_Sentence() {
        assertEquals("config/sentences.json", ConfigType.SENTENCE.getFilePath());
    }

    @Test
    void testGetFilePath_FeatureFlag() {
        assertNull(ConfigType.FEATURE_FLAG.getFilePath());
    }

    @Test
    void testGetFilePath_GameSetting() {
        assertEquals("config/game-settings.json", ConfigType.GAME_SETTING.getFilePath());
    }

    @Test
    void testIsFileBasedConfig_Idiom() {
        assertTrue(ConfigType.IDIOM.isFileBasedConfig());
    }

    @Test
    void testIsFileBasedConfig_Sentence() {
        assertTrue(ConfigType.SENTENCE.isFileBasedConfig());
    }

    @Test
    void testIsFileBasedConfig_FeatureFlag() {
        assertFalse(ConfigType.FEATURE_FLAG.isFileBasedConfig());
    }

    @Test
    void testIsFileBasedConfig_GameSetting() {
        assertTrue(ConfigType.GAME_SETTING.isFileBasedConfig());
    }

    @Test
    void testGetDescription_Idiom() {
        assertEquals("Chinese idiom configurations (成语)", ConfigType.IDIOM.getDescription());
    }

    @Test
    void testGetDescription_Sentence() {
        assertEquals("Chinese sentence configurations (造句)", ConfigType.SENTENCE.getDescription());
    }

    @Test
    void testGetDescription_FeatureFlag() {
        assertEquals("Feature toggle flags", ConfigType.FEATURE_FLAG.getDescription());
    }

    @Test
    void testGetDescription_GameSetting() {
        assertEquals("General game settings and parameters", ConfigType.GAME_SETTING.getDescription());
    }

    @ParameterizedTest
    @EnumSource(ConfigType.class)
    void testAllConfigTypesHaveDescription(ConfigType type) {
        assertNotNull(type.getDescription());
        assertFalse(type.getDescription().isEmpty());
    }

    @Test
    void testEnumValues() {
        ConfigType[] values = ConfigType.values();
        assertEquals(4, values.length);
        assertEquals(ConfigType.IDIOM, values[0]);
        assertEquals(ConfigType.SENTENCE, values[1]);
        assertEquals(ConfigType.FEATURE_FLAG, values[2]);
        assertEquals(ConfigType.GAME_SETTING, values[3]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(ConfigType.IDIOM, ConfigType.valueOf("IDIOM"));
        assertEquals(ConfigType.SENTENCE, ConfigType.valueOf("SENTENCE"));
        assertEquals(ConfigType.FEATURE_FLAG, ConfigType.valueOf("FEATURE_FLAG"));
        assertEquals(ConfigType.GAME_SETTING, ConfigType.valueOf("GAME_SETTING"));
    }

    @Test
    void testValueOf_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> ConfigType.valueOf("INVALID"));
    }

    @Test
    void testEnumName() {
        assertEquals("IDIOM", ConfigType.IDIOM.name());
        assertEquals("SENTENCE", ConfigType.SENTENCE.name());
        assertEquals("FEATURE_FLAG", ConfigType.FEATURE_FLAG.name());
        assertEquals("GAME_SETTING", ConfigType.GAME_SETTING.name());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, ConfigType.IDIOM.ordinal());
        assertEquals(1, ConfigType.SENTENCE.ordinal());
        assertEquals(2, ConfigType.FEATURE_FLAG.ordinal());
        assertEquals(3, ConfigType.GAME_SETTING.ordinal());
    }

    @Test
    void testFileBasedConfigConsistency() {
        // Verify file-based configs have file paths, non-file-based don't
        for (ConfigType type : ConfigType.values()) {
            if (type.isFileBasedConfig()) {
                assertNotNull(type.getFilePath(),
                    type.name() + " is file-based but has null file path");
            } else {
                assertNull(type.getFilePath(),
                    type.name() + " is not file-based but has a file path");
            }
        }
    }

    @Test
    void testFileBasedConfigCount() {
        long fileBasedCount = java.util.Arrays.stream(ConfigType.values())
            .filter(ConfigType::isFileBasedConfig)
            .count();
        assertEquals(3, fileBasedCount);
    }
}
