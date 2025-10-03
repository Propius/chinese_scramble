package com.govtech.chinesescramble.entity.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserRole enum
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
class UserRoleTest {

    @Test
    void testGetAuthority_Player() {
        assertEquals("ROLE_PLAYER", UserRole.PLAYER.getAuthority());
    }

    @Test
    void testGetAuthority_Admin() {
        assertEquals("ROLE_ADMIN", UserRole.ADMIN.getAuthority());
    }

    @Test
    void testGetAuthority_Moderator() {
        assertEquals("ROLE_MODERATOR", UserRole.MODERATOR.getAuthority());
    }

    @Test
    void testGetDisplayName_Player() {
        assertEquals("Player", UserRole.PLAYER.getDisplayName());
    }

    @Test
    void testGetDisplayName_Admin() {
        assertEquals("Administrator", UserRole.ADMIN.getDisplayName());
    }

    @Test
    void testGetDisplayName_Moderator() {
        assertEquals("Moderator", UserRole.MODERATOR.getDisplayName());
    }

    @Test
    void testGetDescription_Player() {
        assertEquals("Regular user with basic game access", UserRole.PLAYER.getDescription());
    }

    @Test
    void testGetDescription_Admin() {
        assertEquals("Full system administrator with all permissions", UserRole.ADMIN.getDescription());
    }

    @Test
    void testGetDescription_Moderator() {
        assertEquals("Content moderator with limited administrative access", UserRole.MODERATOR.getDescription());
    }

    @Test
    void testIsAdministrative_Player() {
        assertFalse(UserRole.PLAYER.isAdministrative());
    }

    @Test
    void testIsAdministrative_Admin() {
        assertTrue(UserRole.ADMIN.isAdministrative());
    }

    @Test
    void testIsAdministrative_Moderator() {
        assertTrue(UserRole.MODERATOR.isAdministrative());
    }

    @Test
    void testCanAccessAdminFeatures_Player() {
        assertFalse(UserRole.PLAYER.canAccessAdminFeatures());
    }

    @Test
    void testCanAccessAdminFeatures_Admin() {
        assertTrue(UserRole.ADMIN.canAccessAdminFeatures());
    }

    @Test
    void testCanAccessAdminFeatures_Moderator() {
        assertFalse(UserRole.MODERATOR.canAccessAdminFeatures());
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void testAllRolesHaveAuthority(UserRole role) {
        assertNotNull(role.getAuthority());
        assertTrue(role.getAuthority().startsWith("ROLE_"));
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void testAllRolesHaveDisplayName(UserRole role) {
        assertNotNull(role.getDisplayName());
        assertFalse(role.getDisplayName().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void testAllRolesHaveDescription(UserRole role) {
        assertNotNull(role.getDescription());
        assertFalse(role.getDescription().isEmpty());
    }

    @Test
    void testEnumValues() {
        UserRole[] values = UserRole.values();
        assertEquals(3, values.length);
        assertEquals(UserRole.PLAYER, values[0]);
        assertEquals(UserRole.ADMIN, values[1]);
        assertEquals(UserRole.MODERATOR, values[2]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(UserRole.PLAYER, UserRole.valueOf("PLAYER"));
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
        assertEquals(UserRole.MODERATOR, UserRole.valueOf("MODERATOR"));
    }

    @Test
    void testValueOf_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> UserRole.valueOf("INVALID"));
    }

    @Test
    void testEnumName() {
        assertEquals("PLAYER", UserRole.PLAYER.name());
        assertEquals("ADMIN", UserRole.ADMIN.name());
        assertEquals("MODERATOR", UserRole.MODERATOR.name());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, UserRole.PLAYER.ordinal());
        assertEquals(1, UserRole.ADMIN.ordinal());
        assertEquals(2, UserRole.MODERATOR.ordinal());
    }

    @Test
    void testAdministrativeRoleCount() {
        long adminCount = java.util.Arrays.stream(UserRole.values())
            .filter(UserRole::isAdministrative)
            .count();
        assertEquals(2, adminCount);
    }

    @Test
    void testAdminFeatureAccessCount() {
        long adminFeatureCount = java.util.Arrays.stream(UserRole.values())
            .filter(UserRole::canAccessAdminFeatures)
            .count();
        assertEquals(1, adminFeatureCount);
    }

    @Test
    void testRoleHierarchy() {
        // Verify role hierarchy: ADMIN can access everything moderator can
        if (UserRole.MODERATOR.isAdministrative()) {
            assertTrue(UserRole.ADMIN.isAdministrative(),
                "ADMIN should have at least moderator privileges");
        }
    }
}
