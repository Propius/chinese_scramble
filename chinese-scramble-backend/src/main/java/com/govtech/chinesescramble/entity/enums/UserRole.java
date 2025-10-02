package com.govtech.chinesescramble.entity.enums;

/**
 * UserRole - User roles for access control and permissions
 *
 * PLAYER:
 * - Default role for all registered users
 * - Can play games, view leaderboards, manage own profile
 * - Cannot access admin features
 *
 * ADMIN:
 * - Full system access
 * - Can manage users, configure feature flags, reload configuration
 * - Can view all game statistics and analytics
 * - Can modify leaderboards if needed
 *
 * MODERATOR:
 * - Limited administrative access
 * - Can view user reports, moderate content
 * - Cannot change system configuration
 * - Cannot modify user roles
 *
 * Security Notes:
 * - Roles are checked using @PreAuthorize annotations on controller methods
 * - Spring Security integration required
 * - Role hierarchy: ADMIN > MODERATOR > PLAYER
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public enum UserRole {
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