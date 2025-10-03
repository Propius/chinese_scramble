package com.govtech.chinesescramble.entity.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SessionStatus enum
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
class SessionStatusTest {

    @Test
    void testIsActive_Active() {
        assertTrue(SessionStatus.ACTIVE.isActive());
    }

    @Test
    void testIsActive_Completed() {
        assertFalse(SessionStatus.COMPLETED.isActive());
    }

    @Test
    void testIsActive_Abandoned() {
        assertFalse(SessionStatus.ABANDONED.isActive());
    }

    @Test
    void testIsActive_Expired() {
        assertFalse(SessionStatus.EXPIRED.isActive());
    }

    @Test
    void testIsFinished_Active() {
        assertFalse(SessionStatus.ACTIVE.isFinished());
    }

    @Test
    void testIsFinished_Completed() {
        assertTrue(SessionStatus.COMPLETED.isFinished());
    }

    @Test
    void testIsFinished_Abandoned() {
        assertTrue(SessionStatus.ABANDONED.isFinished());
    }

    @Test
    void testIsFinished_Expired() {
        assertTrue(SessionStatus.EXPIRED.isFinished());
    }

    @Test
    void testIsCompleted_Active() {
        assertFalse(SessionStatus.ACTIVE.isCompleted());
    }

    @Test
    void testIsCompleted_Completed() {
        assertTrue(SessionStatus.COMPLETED.isCompleted());
    }

    @Test
    void testIsCompleted_Abandoned() {
        assertFalse(SessionStatus.ABANDONED.isCompleted());
    }

    @Test
    void testIsCompleted_Expired() {
        assertFalse(SessionStatus.EXPIRED.isCompleted());
    }

    @Test
    void testGetDescription_Active() {
        assertEquals("Game in progress", SessionStatus.ACTIVE.getDescription());
    }

    @Test
    void testGetDescription_Completed() {
        assertEquals("Game completed successfully", SessionStatus.COMPLETED.getDescription());
    }

    @Test
    void testGetDescription_Abandoned() {
        assertEquals("Game abandoned by player", SessionStatus.ABANDONED.getDescription());
    }

    @Test
    void testGetDescription_Expired() {
        assertEquals("Game session expired", SessionStatus.EXPIRED.getDescription());
    }

    @ParameterizedTest
    @EnumSource(SessionStatus.class)
    void testAllStatusesHaveDescription(SessionStatus status) {
        assertNotNull(status.getDescription());
        assertFalse(status.getDescription().isEmpty());
    }

    @Test
    void testEnumValues() {
        SessionStatus[] values = SessionStatus.values();
        assertEquals(4, values.length);
        assertEquals(SessionStatus.ACTIVE, values[0]);
        assertEquals(SessionStatus.COMPLETED, values[1]);
        assertEquals(SessionStatus.ABANDONED, values[2]);
        assertEquals(SessionStatus.EXPIRED, values[3]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(SessionStatus.ACTIVE, SessionStatus.valueOf("ACTIVE"));
        assertEquals(SessionStatus.COMPLETED, SessionStatus.valueOf("COMPLETED"));
        assertEquals(SessionStatus.ABANDONED, SessionStatus.valueOf("ABANDONED"));
        assertEquals(SessionStatus.EXPIRED, SessionStatus.valueOf("EXPIRED"));
    }

    @Test
    void testValueOf_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> SessionStatus.valueOf("INVALID"));
    }

    @Test
    void testEnumName() {
        assertEquals("ACTIVE", SessionStatus.ACTIVE.name());
        assertEquals("COMPLETED", SessionStatus.COMPLETED.name());
        assertEquals("ABANDONED", SessionStatus.ABANDONED.name());
        assertEquals("EXPIRED", SessionStatus.EXPIRED.name());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, SessionStatus.ACTIVE.ordinal());
        assertEquals(1, SessionStatus.COMPLETED.ordinal());
        assertEquals(2, SessionStatus.ABANDONED.ordinal());
        assertEquals(3, SessionStatus.EXPIRED.ordinal());
    }

    @Test
    void testMutualExclusivity_ActiveAndFinished() {
        for (SessionStatus status : SessionStatus.values()) {
            if (status.isActive()) {
                assertFalse(status.isFinished(), status.name() + " should not be both active and finished");
            }
        }
    }

    @Test
    void testFinishedStateCoverage() {
        // Verify all non-active states are finished
        long finishedCount = java.util.Arrays.stream(SessionStatus.values())
            .filter(SessionStatus::isFinished)
            .count();
        assertEquals(3, finishedCount);
    }

    @Test
    void testOnlyCompletedIsCompleted() {
        // Verify only COMPLETED status returns true for isCompleted()
        long completedCount = java.util.Arrays.stream(SessionStatus.values())
            .filter(SessionStatus::isCompleted)
            .count();
        assertEquals(1, completedCount);
    }
}
