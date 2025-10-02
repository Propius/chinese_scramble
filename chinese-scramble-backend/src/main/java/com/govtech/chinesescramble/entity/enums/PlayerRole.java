package com.govtech.chinesescramble.entity.enums;

/**
 * PlayerRole - Alias for UserRole to maintain test compatibility
 *
 * This enum is an alias that delegates to UserRole for backward compatibility
 * with existing tests. New code should use UserRole directly.
 *
 * Role definitions:
 * - PLAYER: Regular player with basic game access
 * - ADMIN: Administrator with full system access
 * - MODERATOR: Moderator with limited administrative access
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 * @see UserRole
 * @deprecated Use UserRole instead
 */
@Deprecated
public enum PlayerRole {
    /**
     * Regular player - default role
     */
    PLAYER,

    /**
     * Administrator - full system access
     */
    ADMIN,

    /**
     * Moderator - limited administrative access
     */
    MODERATOR;

    /**
     * Converts to UserRole enum
     *
     * @return equivalent UserRole value
     */
    public UserRole toUserRole() {
        return UserRole.valueOf(this.name());
    }

    /**
     * Creates from UserRole enum
     *
     * @param userRole the UserRole to convert
     * @return equivalent PlayerRole value
     */
    public static PlayerRole fromUserRole(UserRole userRole) {
        return PlayerRole.valueOf(userRole.name());
    }

    /**
     * Gets the Spring Security authority name
     * Spring Security requires "ROLE_" prefix
     *
     * @return authority name (e.g., "ROLE_ADMIN")
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    /**
     * Gets a human-readable display name
     *
     * @return display name
     */
    public String getDisplayName() {
        return switch (this) {
            case PLAYER -> "Player";
            case ADMIN -> "Administrator";
            case MODERATOR -> "Moderator";
        };
    }

    /**
     * Gets a description of this role's permissions
     *
     * @return role description
     */
    public String getDescription() {
        return switch (this) {
            case PLAYER -> "Regular user with basic game access";
            case ADMIN -> "Full system administrator with all permissions";
            case MODERATOR -> "Content moderator with limited administrative access";
        };
    }

    /**
     * Checks if this role has administrative privileges
     *
     * @return true if ADMIN or MODERATOR
     */
    public boolean isAdministrative() {
        return this == ADMIN || this == MODERATOR;
    }

    /**
     * Checks if this role can access admin features
     *
     * @return true if ADMIN only
     */
    public boolean canAccessAdminFeatures() {
        return this == ADMIN;
    }
}
