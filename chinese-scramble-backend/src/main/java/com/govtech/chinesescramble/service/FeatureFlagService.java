package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.FeatureFlag;
import com.govtech.chinesescramble.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FeatureFlagService - Manages runtime feature toggles
 *
 * Features:
 * - Enable/disable features without deployment
 * - Spring Cache integration (5-minute TTL)
 * - Feature availability checks
 * - Audit trail (enabled/disabled timestamps)
 * - Admin feature management
 *
 * Supported Features:
 * - idiom-scramble: Idiom game mode
 * - sentence-crafting: Sentence game mode
 * - leaderboard: Leaderboard display
 * - audio-pronunciation: Audio pronunciation
 * - hints: Hint system
 * - practice-mode: Practice mode (no scoring)
 * - achievements: Achievement system
 * - daily-challenge: Daily challenge mode
 * - multiplayer: Multiplayer competitive mode
 *
 * Usage:
 * <pre>
 * {@code
 * // Check if feature is enabled
 * if (featureFlagService.isFeatureEnabled("leaderboard")) {
 *     // Show leaderboard
 * }
 *
 * // Enable a feature (admin only)
 * featureFlagService.enableFeature("daily-challenge");
 *
 * // Disable a feature (admin only)
 * featureFlagService.disableFeature("multiplayer");
 * }
 * </pre>
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;

    /**
     * Checks if a feature is enabled
     * Uses Spring Cache with 5-minute TTL
     *
     * @param featureName the feature name (kebab-case)
     * @return true if feature is enabled
     */
    @Cacheable(value = "featureFlags", key = "#featureName")
    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(String featureName) {
        log.debug("Checking feature flag: {}", featureName);

        Optional<FeatureFlag> featureFlag = featureFlagRepository.findByFeatureName(featureName);

        if (featureFlag.isEmpty()) {
            log.warn("Feature flag not found: {}. Defaulting to disabled.", featureName);
            return false;
        }

        boolean enabled = featureFlag.get().getEnabled();
        log.debug("Feature flag {} is {}", featureName, enabled ? "enabled" : "disabled");
        return enabled;
    }

    /**
     * Enables a feature and clears cache
     *
     * @param featureName the feature name
     * @return updated feature flag
     */
    @CacheEvict(value = "featureFlags", key = "#featureName")
    public FeatureFlag enableFeature(String featureName) {
        log.info("Enabling feature: {}", featureName);

        FeatureFlag featureFlag = featureFlagRepository.findByFeatureName(featureName)
            .orElseThrow(() -> new IllegalArgumentException("Feature flag not found: " + featureName));

        if (featureFlag.getEnabled()) {
            log.info("Feature {} is already enabled", featureName);
            return featureFlag;
        }

        featureFlag.enable();
        FeatureFlag saved = featureFlagRepository.save(featureFlag);
        log.info("Feature {} enabled successfully at {}", featureName, saved.getEnabledAt());

        return saved;
    }

    /**
     * Disables a feature and clears cache
     *
     * @param featureName the feature name
     * @return updated feature flag
     */
    @CacheEvict(value = "featureFlags", key = "#featureName")
    public FeatureFlag disableFeature(String featureName) {
        log.info("Disabling feature: {}", featureName);

        FeatureFlag featureFlag = featureFlagRepository.findByFeatureName(featureName)
            .orElseThrow(() -> new IllegalArgumentException("Feature flag not found: " + featureName));

        if (!featureFlag.getEnabled()) {
            log.info("Feature {} is already disabled", featureName);
            return featureFlag;
        }

        featureFlag.disable();
        FeatureFlag saved = featureFlagRepository.save(featureFlag);
        log.info("Feature {} disabled successfully at {}", featureName, saved.getDisabledAt());

        return saved;
    }

    /**
     * Toggles a feature's state
     *
     * @param featureName the feature name
     * @return updated feature flag
     */
    @CacheEvict(value = "featureFlags", key = "#featureName")
    public FeatureFlag toggleFeature(String featureName) {
        log.info("Toggling feature: {}", featureName);

        FeatureFlag featureFlag = featureFlagRepository.findByFeatureName(featureName)
            .orElseThrow(() -> new IllegalArgumentException("Feature flag not found: " + featureName));

        featureFlag.toggle();
        FeatureFlag saved = featureFlagRepository.save(featureFlag);
        log.info("Feature {} toggled to {}", featureName, saved.getEnabled() ? "enabled" : "disabled");

        return saved;
    }

    /**
     * Creates a new feature flag
     *
     * @param featureName the feature name (kebab-case)
     * @param description the feature description
     * @param enabled initial enabled state
     * @return created feature flag
     */
    public FeatureFlag createFeatureFlag(String featureName, String description, boolean enabled) {
        log.info("Creating new feature flag: {} (enabled: {})", featureName, enabled);

        if (featureFlagRepository.existsByFeatureName(featureName)) {
            throw new IllegalArgumentException("Feature flag already exists: " + featureName);
        }

        FeatureFlag featureFlag = FeatureFlag.builder()
            .featureName(featureName)
            .description(description)
            .enabled(enabled)
            .enabledAt(enabled ? LocalDateTime.now() : null)
            .build();

        FeatureFlag saved = featureFlagRepository.save(featureFlag);
        log.info("Feature flag created: {}", featureName);

        return saved;
    }

    /**
     * Updates feature flag description
     *
     * @param featureName the feature name
     * @param description the new description
     * @return updated feature flag
     */
    public FeatureFlag updateFeatureDescription(String featureName, String description) {
        log.info("Updating feature flag description: {}", featureName);

        FeatureFlag featureFlag = featureFlagRepository.findByFeatureName(featureName)
            .orElseThrow(() -> new IllegalArgumentException("Feature flag not found: " + featureName));

        featureFlag.setDescription(description);
        FeatureFlag saved = featureFlagRepository.save(featureFlag);
        log.info("Feature flag description updated: {}", featureName);

        return saved;
    }

    /**
     * Gets a feature flag by name
     *
     * @param featureName the feature name
     * @return Optional containing feature flag
     */
    @Transactional(readOnly = true)
    public Optional<FeatureFlag> getFeatureFlag(String featureName) {
        return featureFlagRepository.findByFeatureName(featureName);
    }

    /**
     * Gets all feature flags
     *
     * @return list of all feature flags
     */
    @Transactional(readOnly = true)
    public List<FeatureFlag> getAllFeatureFlags() {
        return featureFlagRepository.findAllOrderedByName();
    }

    /**
     * Gets all features (alias for getAllFeatureFlags)
     *
     * @return list of all feature flags
     */
    @Transactional(readOnly = true)
    public List<FeatureFlag> getAllFeatures() {
        return getAllFeatureFlags();
    }

    /**
     * Gets a feature by name (alias for getFeatureFlag)
     *
     * @param featureName the feature name
     * @return Optional containing feature flag
     */
    @Transactional(readOnly = true)
    public Optional<FeatureFlag> getFeature(String featureName) {
        return getFeatureFlag(featureName);
    }

    /**
     * Gets all enabled features
     *
     * @return list of enabled feature flags
     */
    @Transactional(readOnly = true)
    public List<FeatureFlag> getEnabledFeatures() {
        return featureFlagRepository.findByEnabledTrue();
    }

    /**
     * Gets all disabled features
     *
     * @return list of disabled feature flags
     */
    @Transactional(readOnly = true)
    public List<FeatureFlag> getDisabledFeatures() {
        return featureFlagRepository.findByEnabledFalse();
    }

    /**
     * Gets recently enabled features (within 7 days)
     *
     * @return list of recently enabled features
     */
    @Transactional(readOnly = true)
    public List<FeatureFlag> getRecentlyEnabledFeatures() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return featureFlagRepository.findRecentlyEnabledFeatures(sevenDaysAgo);
    }

    /**
     * Gets recently disabled features (within 7 days)
     *
     * @return list of recently disabled features
     */
    @Transactional(readOnly = true)
    public List<FeatureFlag> getRecentlyDisabledFeatures() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return featureFlagRepository.findRecentlyDisabledFeatures(sevenDaysAgo);
    }

    /**
     * Searches features by name or description
     *
     * @param searchTerm the search term
     * @return list of matching feature flags
     */
    @Transactional(readOnly = true)
    public List<FeatureFlag> searchFeatures(String searchTerm) {
        return featureFlagRepository.searchFeatures(searchTerm);
    }

    /**
     * Checks if feature flag exists
     *
     * @param featureName the feature name
     * @return true if feature flag exists
     */
    @Transactional(readOnly = true)
    public boolean featureFlagExists(String featureName) {
        return featureFlagRepository.existsByFeatureName(featureName);
    }

    /**
     * Deletes a feature flag
     * Should be used with caution
     *
     * @param featureName the feature name
     */
    @CacheEvict(value = "featureFlags", key = "#featureName")
    public void deleteFeatureFlag(String featureName) {
        log.warn("Deleting feature flag: {}", featureName);

        FeatureFlag featureFlag = featureFlagRepository.findByFeatureName(featureName)
            .orElseThrow(() -> new IllegalArgumentException("Feature flag not found: " + featureName));

        featureFlagRepository.delete(featureFlag);
        log.info("Feature flag deleted: {}", featureName);
    }

    /**
     * Gets feature flag statistics
     *
     * @return statistics map with counts
     */
    @Transactional(readOnly = true)
    public FeatureFlagStatistics getStatistics() {
        long totalFlags = featureFlagRepository.count();
        long enabledCount = featureFlagRepository.countByEnabledTrue();
        long disabledCount = featureFlagRepository.countByEnabledFalse();

        return new FeatureFlagStatistics(totalFlags, enabledCount, disabledCount);
    }

    /**
     * Clears all feature flag caches
     */
    @CacheEvict(value = "featureFlags", allEntries = true)
    public void clearAllCaches() {
        log.info("Clearing all feature flag caches");
    }

    // ========================================================================
    // Statistics DTO
    // ========================================================================

    /**
     * Feature flag statistics
     */
    public record FeatureFlagStatistics(
        long totalFlags,
        long enabledCount,
        long disabledCount
    ) {
        public double enabledPercentage() {
            return totalFlags > 0 ? (enabledCount * 100.0 / totalFlags) : 0.0;
        }

        public double disabledPercentage() {
            return totalFlags > 0 ? (disabledCount * 100.0 / totalFlags) : 0.0;
        }
    }
}