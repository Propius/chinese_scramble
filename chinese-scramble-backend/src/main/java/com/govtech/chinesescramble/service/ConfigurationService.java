package com.govtech.chinesescramble.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govtech.chinesescramble.entity.ConfigCache;
import com.govtech.chinesescramble.entity.enums.ConfigType;
import com.govtech.chinesescramble.repository.ConfigCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ConfigurationService - Manages configuration loading and hot-reload
 *
 * Features:
 * - Loads configuration from JSON files
 * - Caches configuration in database
 * - Detects file changes via SHA-256 checksum
 * - Scheduled hot-reload every 5 minutes
 * - Spring Cache integration (5-minute TTL)
 * - UTF-8 encoding for Chinese characters
 *
 * Configuration Files:
 * - idioms.json: Chinese idiom database (成语数据库)
 * - sentences.json: Sentence patterns and examples
 * - feature-flags.json: Feature flag defaults
 * - game-settings.json: Game rules and settings
 *
 * Usage:
 * <pre>
 * {@code
 * // Load idiom configuration
 * String idiomsJson = configurationService.loadConfiguration("idioms.json");
 * Map<String, Object> idiomsData = configurationService.loadConfigurationAsMap("idioms.json");
 *
 * // Force reload
 * configurationService.reloadConfiguration("idioms.json");
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
public class ConfigurationService {

    private final ConfigCacheRepository configCacheRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    private static final String CONFIG_BASE_PATH = "classpath:config/";

    /**
     * Loads configuration from cache or file
     * Uses Spring Cache with 5-minute TTL
     *
     * @param configKey the configuration file name (e.g., "idioms.json")
     * @return configuration content as JSON string
     */
    @Cacheable(value = "configurations", key = "#configKey")
    @Transactional(readOnly = true)
    public String loadConfiguration(String configKey) {
        log.debug("Loading configuration: {}", configKey);

        // Check if configuration exists in database
        Optional<ConfigCache> cachedConfig = configCacheRepository.findByConfigKey(configKey);

        if (cachedConfig.isPresent() && !cachedConfig.get().isStale()) {
            // Verify checksum to ensure file hasn't changed
            String fileChecksum = calculateFileChecksum(configKey);
            if (cachedConfig.get().isUpToDate(fileChecksum)) {
                log.debug("Configuration loaded from cache: {}", configKey);
                return cachedConfig.get().getConfigValue();
            } else {
                log.info("Configuration file changed, reloading: {}", configKey);
            }
        }

        // Load from file and cache
        return reloadConfiguration(configKey);
    }

    /**
     * Loads configuration as Map for easy access
     *
     * @param configKey the configuration file name
     * @return configuration as Map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> loadConfigurationAsMap(String configKey) {
        String jsonContent = loadConfiguration(configKey);
        try {
            return objectMapper.readValue(jsonContent, Map.class);
        } catch (IOException e) {
            log.error("Failed to parse configuration as Map: {}", configKey, e);
            throw new RuntimeException("Failed to parse configuration: " + configKey, e);
        }
    }

    /**
     * Loads configuration as specific type
     *
     * @param configKey the configuration file name
     * @param valueType the target class type
     * @return configuration as specified type
     */
    @Transactional(readOnly = true)
    public <T> T loadConfigurationAs(String configKey, Class<T> valueType) {
        String jsonContent = loadConfiguration(configKey);
        try {
            return objectMapper.readValue(jsonContent, valueType);
        } catch (IOException e) {
            log.error("Failed to parse configuration as {}: {}", valueType.getSimpleName(), configKey, e);
            throw new RuntimeException("Failed to parse configuration: " + configKey, e);
        }
    }

    /**
     * Reloads configuration from file and updates cache
     * Evicts Spring Cache entry
     *
     * @param configKey the configuration file name
     * @return reloaded configuration content
     */
    @CacheEvict(value = "configurations", key = "#configKey")
    public String reloadConfiguration(String configKey) {
        log.info("Reloading configuration from file: {}", configKey);

        try {
            // Load file content
            String fileContent = loadFileContent(configKey);
            String checksum = calculateChecksum(fileContent);

            // Determine config type from filename
            ConfigType configType = determineConfigType(configKey);

            // Update or create cache entry
            Optional<ConfigCache> existingCache = configCacheRepository.findByConfigKey(configKey);

            if (existingCache.isPresent()) {
                ConfigCache cache = existingCache.get();
                cache.updateCache(fileContent, checksum);
                configCacheRepository.save(cache);
                log.info("Updated cached configuration: {}", configKey);
            } else {
                ConfigCache newCache = ConfigCache.builder()
                    .configKey(configKey)
                    .configValue(fileContent)
                    .configType(configType)
                    .checksum(checksum)
                    .lastLoadedAt(LocalDateTime.now())
                    .description(generateDescription(configKey, configType))
                    .build();
                configCacheRepository.save(newCache);
                log.info("Created new cached configuration: {}", configKey);
            }

            return fileContent;

        } catch (IOException e) {
            log.error("Failed to reload configuration: {}", configKey, e);
            throw new RuntimeException("Failed to reload configuration: " + configKey, e);
        }
    }

    /**
     * Reloads all configurations
     * Used for bulk reload operations
     */
    @CacheEvict(value = "configurations", allEntries = true)
    public void reloadAllConfigurations() {
        log.info("Reloading all configurations");

        List<ConfigCache> allConfigs = configCacheRepository.findAll();
        for (ConfigCache config : allConfigs) {
            try {
                reloadConfiguration(config.getConfigKey());
            } catch (Exception e) {
                log.error("Failed to reload configuration: {}", config.getConfigKey(), e);
            }
        }

        log.info("Completed reloading {} configurations", allConfigs.size());
    }

    /**
     * Scheduled task to check for configuration changes
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void scheduledConfigurationCheck() {
        log.debug("Running scheduled configuration check");

        List<ConfigCache> allConfigs = configCacheRepository.findAll();
        int reloadedCount = 0;

        for (ConfigCache config : allConfigs) {
            try {
                String fileChecksum = calculateFileChecksum(config.getConfigKey());
                if (!config.isUpToDate(fileChecksum)) {
                    log.info("Configuration file changed, reloading: {}", config.getConfigKey());
                    reloadConfiguration(config.getConfigKey());
                    reloadedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to check configuration: {}", config.getConfigKey(), e);
            }
        }

        if (reloadedCount > 0) {
            log.info("Scheduled reload completed: {} configurations reloaded", reloadedCount);
        }
    }

    /**
     * Checks if configuration exists
     *
     * @param configKey the configuration file name
     * @return true if configuration exists
     */
    @Transactional(readOnly = true)
    public boolean configurationExists(String configKey) {
        return configCacheRepository.existsByConfigKey(configKey);
    }

    /**
     * Gets configuration metadata
     *
     * @param configKey the configuration file name
     * @return Optional containing config cache metadata
     */
    @Transactional(readOnly = true)
    public Optional<ConfigCache> getConfigurationMetadata(String configKey) {
        return configCacheRepository.findByConfigKey(configKey);
    }

    /**
     * Gets all configuration keys
     *
     * @return list of all configuration keys
     */
    @Transactional(readOnly = true)
    public List<String> getAllConfigurationKeys() {
        return configCacheRepository.findAll()
            .stream()
            .map(ConfigCache::getConfigKey)
            .toList();
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    /**
     * Loads file content from classpath
     */
    private String loadFileContent(String configKey) throws IOException {
        String resourcePath = CONFIG_BASE_PATH + configKey;
        Resource resource = resourceLoader.getResource(resourcePath);

        if (!resource.exists()) {
            throw new IOException("Configuration file not found: " + resourcePath);
        }

        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Calculates SHA-256 checksum for file content
     */
    private String calculateChecksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }

    /**
     * Calculates checksum for file by loading it
     */
    private String calculateFileChecksum(String configKey) {
        try {
            String content = loadFileContent(configKey);
            return calculateChecksum(content);
        } catch (IOException e) {
            log.error("Failed to calculate file checksum: {}", configKey, e);
            return "";
        }
    }

    /**
     * Determines config type from filename
     */
    private ConfigType determineConfigType(String configKey) {
        String lowerKey = configKey.toLowerCase();
        if (lowerKey.contains("idiom")) {
            return ConfigType.IDIOM;
        } else if (lowerKey.contains("sentence")) {
            return ConfigType.SENTENCE;
        } else if (lowerKey.contains("feature")) {
            return ConfigType.FEATURE_FLAG;
        } else {
            return ConfigType.GAME_SETTING;
        }
    }

    /**
     * Generates description for configuration
     */
    private String generateDescription(String configKey, ConfigType configType) {
        return switch (configType) {
            case IDIOM -> "Chinese idiom database with definitions and difficulty levels";
            case SENTENCE -> "Sentence patterns and examples for sentence crafting game";
            case FEATURE_FLAG -> "Feature flag default configuration";
            case GAME_SETTING -> "Game rules and settings configuration";
        };
    }
}