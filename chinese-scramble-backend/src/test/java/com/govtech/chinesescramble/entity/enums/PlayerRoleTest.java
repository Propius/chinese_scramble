package com.govtech.chinesescramble.entity.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlayerRole enum
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
class PlayerRoleTest {

    @Test
    void testToUserRole_Player() {
        assertEquals(UserRole.PLAYER, PlayerRole.PLAYER.toUserRole());
    }

    @Test
    void testToUserRole_Admin() {
        assertEquals(UserRole.ADMIN, PlayerRole.ADMIN.toUserRole());
    }

    @Test
    void testToUserRole_Moderator() {
        assertEquals(UserRole.MODERATOR, PlayerRole.MODERATOR.toUserRole());
    }

    @Test
    void testFromUserRole_Player() {
        assertEquals(PlayerRole.PLAYER, PlayerRole.fromUserRole(UserRole.PLAYER));
    }

    @Test
    void testFromUserRole_Admin() {
        assertEquals(PlayerRole.ADMIN, PlayerRole.fromUserRole(UserRole.ADMIN));
    }

    @Test
    void testFromUserRole_Moderator() {
        assertEquals(PlayerRole.MODERATOR, PlayerRole.fromUserRole(UserRole.MODERATOR));
    }

    @Test
    void testGetAuthority_Player() {
        assertEquals("ROLE_PLAYER", PlayerRole.PLAYER.getAuthority());
    }

    @Test
    void testGetAuthority_Admin() {
        assertEquals("ROLE_ADMIN", PlayerRole.ADMIN.getAuthority());
    }

    @Test
    void testGetAuthority_Moderator() {
        assertEquals("ROLE_MODERATOR", PlayerRole.MODERATOR.getAuthority());
    }

    @Test
    void testGetDisplayName_Player() {
        assertEquals("Player", PlayerRole.PLAYER.getDisplayName());
    }

    @Test
    void testGetDisplayName_Admin() {
        assertEquals("Administrator", PlayerRole.ADMIN.getDisplayName());
    }

    @Test
    void testGetDisplayName_Moderator() {
        assertEquals("Moderator", PlayerRole.MODERATOR.getDisplayName());
    }

    @Test
    void testGetDescription_Player() {
        assertEquals("Regular user with basic game access", PlayerRole.PLAYER.getDescription());
    }

    @Test
    void testGetDescription_Admin() {
        assertEquals("Full system administrator with all permissions", PlayerRole.ADMIN.getDescription());
    }

    @Test
    void testGetDescription_Moderator() {
        assertEquals("Content moderator with limited administrative access", PlayerRole.MODERATOR.getDescription());
    }

    @Test
    void testIsAdministrative_Player() {
        assertFalse(PlayerRole.PLAYER.isAdministrative());
    }

    @Test
    void testIsAdministrative_Admin() {
        assertTrue(PlayerRole.ADMIN.isAdministrative());
    }

    @Test
    void testIsAdministrative_Moderator() {
        assertTrue(PlayerRole.MODERATOR.isAdministrative());
    }

    @Test
    void testCanAccessAdminFeatures_Player() {
        assertFalse(PlayerRole.PLAYER.canAccessAdminFeatures());
    }

    @Test
    void testCanAccessAdminFeatures_Admin() {
        assertTrue(PlayerRole.ADMIN.canAccessAdminFeatures());
    }

    @Test
    void testCanAccessAdminFeatures_Moderator() {
        assertFalse(PlayerRole.MODERATOR.canAccessAdminFeatures());
    }

    @ParameterizedTest
    @EnumSource(PlayerRole.class)
    void testAllRolesHaveAuthority(PlayerRole role) {
        assertNotNull(role.getAuthority());
        assertTrue(role.getAuthority().startsWith("ROLE_"));
    }

    @ParameterizedTest
    @EnumSource(PlayerRole.class)
    void testAllRolesHaveDisplayName(PlayerRole role) {
        assertNotNull(role.getDisplayName());
        assertFalse(role.getDisplayName().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(PlayerRole.class)
    void testAllRolesHaveDescription(PlayerRole role) {
        assertNotNull(role.getDescription());
        assertFalse(role.getDescription().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(PlayerRole.class)
    void testConversionToUserRoleAndBack(PlayerRole role) {
        UserRole userRole = role.toUserRole();
        PlayerRole backToPlayerRole = PlayerRole.fromUserRole(userRole);
        assertEquals(role, backToPlayerRole);
    }

    @Test
    void testEnumValues() {
        PlayerRole[] values = PlayerRole.values();
        assertEquals(3, values.length);
        assertEquals(PlayerRole.PLAYER, values[0]);
        assertEquals(PlayerRole.ADMIN, values[1]);
        assertEquals(PlayerRole.MODERATOR, values[2]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(PlayerRole.PLAYER, PlayerRole.valueOf("PLAYER"));
        assertEquals(PlayerRole.ADMIN, PlayerRole.valueOf("ADMIN"));
        assertEquals(PlayerRole.MODERATOR, PlayerRole.valueOf("MODERATOR"));
    }

    @Test
    void testValueOf_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> PlayerRole.valueOf("INVALID"));
    }

    @Test
    void testEnumName() {
        assertEquals("PLAYER", PlayerRole.PLAYER.name());
        assertEquals("ADMIN", PlayerRole.ADMIN.name());
        assertEquals("MODERATOR", PlayerRole.MODERATOR.name());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, PlayerRole.PLAYER.ordinal());
        assertEquals(1, PlayerRole.ADMIN.ordinal());
        assertEquals(2, PlayerRole.MODERATOR.ordinal());
    }
}
