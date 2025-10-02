package com.govtech.chinesescramble.entity;

import com.govtech.chinesescramble.entity.enums.ConfigType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ConfigCache Entity - Caches configuration data loaded from JSON files
 *
 * Purpose:
 * - Reduces file I/O by caching configuration in database
 * - Supports hot-reload: detects file changes via checksum
 * - Enables configuration versioning and rollback
 * - Improves application startup time
 *
 * Cached Configuration Types:
 * - IDIOM: idioms.json content (成语 data with definitions)
 * - SENTENCE: sentences.json content (句子 data with patterns)
 * - FEATURE_FLAG: Feature flag defaults
 * - GAME_SETTING: Game settings and rules
 *
 * Hot-Reload Mechanism:
 * 1. ConfigurationService calculates file checksum (SHA-256)
 * 2. Compares with database checksum
 * 3. If different: reload file, update cache, publish reload event
 * 4. Scheduled job checks every 5 minutes
 *
 * Checksum Algorithm:
 * - SHA-256 hash of file contents
 * - Stored as hexadecimal string (64 characters)
 * - Example: "a3f5b8c9d1e2f4a6b8c0d2e4f6a8b0c2d4e6f8a0b2c4d6e8f0a2b4c6d8e0f2a4"
 *
 * Data Format:
 * - configValue stored as TEXT (JSON string)
 * - Large configurations supported (up to 16MB in MySQL TEXT)
 * - Example JSON structure:
 *   {
 *     "idioms": [
 *       {"idiom": "一马当先", "definition": "比喻走在前面，带头", "difficulty": "EASY"},
 *       {"idiom": "井底之蛙", "definition": "比喻见识短浅的人", "difficulty": "MEDIUM"}
 *     ]
 *   }
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Entity
@Table(
    name = "config_cache",
    indexes = {
        @Index(name = "idx_config_cache_key", columnList = "config_key"),
        @Index(name = "idx_config_cache_type", columnList = "config_type"),
        @Index(name = "idx_config_cache_loaded", columnList = "last_loaded_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_config_cache_key", columnNames = {"config_key"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConfigCache extends BaseEntity {

    /**
     * Unique configuration key identifier
     * Examples: "idioms.json", "sentences.json", "feature-flags.json"
     * Kebab-case naming convention
     */
    @Column(nullable = false, unique = true, length = 100, name = "config_key")
    private String configKey;

    /**
     * Configuration data stored as JSON string
     * Can contain large datasets (idiom lists, sentence patterns, etc.)
     * Parsed by ConfigurationService into Java objects
     */
    @Column(nullable = false, columnDefinition = "TEXT", name = "config_value")
    private String configValue;

    /**
     * Type of configuration data
     * Used for routing and validation
     * Determines which service component processes this config
     */
    @Column(nullable = false, length = 20, name = "config_type")
    @Enumerated(EnumType.STRING)
    private ConfigType configType;

    /**
     * SHA-256 checksum of the source file
     * Used to detect file changes for hot-reload
     * 64-character hexadecimal string
     * Example: "a3f5b8c9d1e2f4a6b8c0d2e4f6a8b0c2d4e6f8a0b2c4d6e8f0a2b4c6d8e0f2a4"
     */
    @Column(nullable = false, length = 64)
    private String checksum;

    /**
     * Timestamp when this configuration was last loaded from file
     * Updated each time the file is reloaded
     * Used for audit trail and staleness detection
     */
    @Column(nullable = false, name = "last_loaded_at")
    private LocalDateTime lastLoadedAt;

    /**
     * Human-readable description of this configuration
     * Helps administrators understand what this config controls
     * Example: "Chinese idiom database with definitions and difficulty levels"
     */
    @Column(length = 500)
    private String description;

    /**
     * Updates this cache entry with new configuration data
     *
     * @param newValue the new configuration JSON
     * @param newChecksum the new file checksum
     */
    public void updateCache(String newValue, String newChecksum) {
        this.configValue = newValue;
        this.checksum = newChecksum;
        this.lastLoadedAt = LocalDateTime.now();
    }

    /**
     * Checks if this configuration matches the given checksum
     * Used to determine if reload is needed
     *
     * @param fileChecksum the current file checksum
     * @return true if checksums match (no reload needed)
     */
    public boolean isUpToDate(String fileChecksum) {
        return this.checksum != null && this.checksum.equals(fileChecksum);
    }

    /**
     * Checks if this configuration is stale (older than 24 hours)
     * Stale configs trigger a forced reload check
     *
     * @return true if last loaded more than 24 hours ago
     */
    public boolean isStale() {
        if (lastLoadedAt == null) return true;
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return lastLoadedAt.isBefore(twentyFourHoursAgo);
    }

    /**
     * Gets the size of the cached configuration in bytes
     *
     * @return configuration size in bytes
     */
    public int getConfigSizeBytes() {
        return configValue != null ? configValue.getBytes().length : 0;
    }

    /**
     * Gets the size of the cached configuration in kilobytes
     *
     * @return configuration size in KB
     */
    public double getConfigSizeKB() {
        return getConfigSizeBytes() / 1024.0;
    }

    /**
     * Checks if this is an idiom configuration
     *
     * @return true if config type is IDIOM
     */
    public boolean isIdiomConfig() {
        return configType == ConfigType.IDIOM;
    }

    /**
     * Checks if this is a sentence configuration
     *
     * @return true if config type is SENTENCE
     */
    public boolean isSentenceConfig() {
        return configType == ConfigType.SENTENCE;
    }

    /**
     * Checks if this is a feature flag configuration
     *
     * @return true if config type is FEATURE_FLAG
     */
    public boolean isFeatureFlagConfig() {
        return configType == ConfigType.FEATURE_FLAG;
    }

    /**
     * Gets a display string for this configuration
     *
     * @return formatted configuration summary
     */
    public String getDisplayString() {
        return String.format("%s (%s) - %.2f KB - Loaded: %s",
            configKey,
            configType,
            getConfigSizeKB(),
            lastLoadedAt
        );
    }

    /**
     * Lifecycle callback executed before persisting entity
     * Sets timestamps and validates fields
     */
    @PrePersist
    private void onPrePersist() {
        // Set last loaded timestamp if null
        if (lastLoadedAt == null) {
            lastLoadedAt = LocalDateTime.now();
        }
        // Run validations
        validateFields();
    }

    /**
     * Lifecycle callback executed before updating entity
     * Validates all fields
     */
    @PreUpdate
    private void onPreUpdate() {
        validateFields();
    }

    /**
     * Validates all configuration fields
     */
    private void validateFields() {
        // Validate config key format (kebab-case with file extension)
        if (configKey != null && !configKey.matches("^[a-z0-9]+(-[a-z0-9]+)*\\.(json|yml|yaml|properties)$")) {
            throw new IllegalArgumentException(
                "Config key must be kebab-case with file extension (e.g., 'idioms.json')"
            );
        }

        // Validate checksum format (64-character hex string)
        if (checksum != null && !checksum.matches("^[a-f0-9]{64}$")) {
            throw new IllegalArgumentException(
                "Checksum must be a 64-character SHA-256 hex string"
            );
        }

        // Validate config value is not empty
        if (configValue == null || configValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Config value cannot be empty");
        }
    }
}