package com.govtech.chinesescramble.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * FeatureFlag Entity - Controls feature availability at runtime
 *
 * Purpose:
 * - Enable/disable features without code deployment
 * - Gradual feature rollout
 * - A/B testing capabilities
 * - Emergency feature kill switch
 *
 * Supported Features:
 * - idiom-scramble: Enable idiom game mode
 * - sentence-crafting: Enable sentence game mode
 * - leaderboard: Enable leaderboard display
 * - audio-pronunciation: Enable audio pronunciation feature
 * - hints: Enable hint system
 * - practice-mode: Enable practice mode (no score)
 * - achievements: Enable achievement system
 *
 * Configuration:
 * - Default values set in application.yml
 * - Database values override defaults
 * - Cached for performance (5-minute TTL)
 *
 * Admin Access:
 * - Only ADMIN role can modify feature flags
 * - Changes logged for audit trail
 * - Tracked via enabledAt/disabledAt timestamps
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Entity
@Table(
    name = "feature_flags",
    indexes = {
        @Index(name = "idx_feature_flag_name", columnList = "feature_name"),
        @Index(name = "idx_feature_flag_enabled", columnList = "enabled")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeatureFlag extends BaseEntity {

    /**
     * Unique feature name identifier
     * Kebab-case naming convention
     * Examples: "idiom-scramble", "sentence-crafting", "audio-pronunciation"
     */
    @Column(nullable = false, unique = true, length = 100, name = "feature_name")
    private String featureName;

    /**
     * Whether the feature is currently enabled
     * true: Feature is active and available to users
     * false: Feature is disabled and hidden
     * Default: false (features disabled until explicitly enabled)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    /**
     * Human-readable description of the feature
     * Helps admins understand what this flag controls
     * Example: "Enable Chinese idiom scramble game mode (成语拼字)"
     */
    @Column(length = 500)
    private String description;

    /**
     * Timestamp when the feature was last enabled
     * Used for tracking and analytics
     * Null if never enabled
     */
    @Column(name = "enabled_at")
    private LocalDateTime enabledAt;

    /**
     * Timestamp when the feature was last disabled
     * Used for tracking and analytics
     * Null if never disabled or currently enabled
     */
    @Column(name = "disabled_at")
    private LocalDateTime disabledAt;

    /**
     * Enables this feature and records the timestamp
     */
    public void enable() {
        this.enabled = true;
        this.enabledAt = LocalDateTime.now();
        this.disabledAt = null;
    }

    /**
     * Disables this feature and records the timestamp
     */
    public void disable() {
        this.enabled = false;
        this.disabledAt = LocalDateTime.now();
    }

    /**
     * Toggles the feature state
     */
    public void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }

    /**
     * Checks if the feature was recently enabled (within 7 days)
     *
     * @return true if enabled in the last 7 days
     */
    public boolean isRecentlyEnabled() {
        if (!enabled || enabledAt == null) return false;
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return enabledAt.isAfter(sevenDaysAgo);
    }

    /**
     * Gets a display string showing feature status
     *
     * @return formatted status string
     */
    public String getStatusDisplay() {
        return String.format("%s: %s", featureName, enabled ? "ENABLED" : "DISABLED");
    }

    /**
     * Lifecycle callback before persist
     */
    @PrePersist
    private void onPrePersist() {
        validateFeatureName();
    }

    /**
     * Lifecycle callback before update
     */
    @PreUpdate
    private void onPreUpdate() {
        validateFeatureName();
    }

    /**
     * Validates feature name format (kebab-case)
     */
    private void validateFeatureName() {
        if (featureName != null && !featureName.matches("^[a-z0-9]+(-[a-z0-9]+)*$")) {
            throw new IllegalArgumentException(
                "Feature name must be kebab-case (lowercase letters, numbers, hyphens only)"
            );
        }
    }
}