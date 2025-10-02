package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.ConfigCache;
import com.govtech.chinesescramble.entity.enums.ConfigType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ConfigCacheRepository - Data access layer for ConfigCache entity
 *
 * Provides:
 * - Configuration retrieval by key and type
 * - Cache validation and freshness checks
 * - Configuration reload tracking
 * - Cache management queries
 *
 * Used by:
 * - ConfigurationService: Load and cache configurations
 * - ConfigReloadScheduler: Hot-reload configurations
 * - IdiomService: Load idiom data
 * - SentenceService: Load sentence data
 *
 * Cached Configurations:
 * - idioms.json: Chinese idiom database
 * - sentences.json: Sentence patterns and examples
 * - feature-flags.json: Feature flag defaults
 * - game-settings.json: Game rules and settings
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Repository
public interface ConfigCacheRepository extends JpaRepository<ConfigCache, Long> {

    /**
     * Find configuration by key
     * Used for configuration retrieval
     *
     * @param configKey the configuration key (e.g., "idioms.json")
     * @return Optional containing configuration
     */
    Optional<ConfigCache> findByConfigKey(String configKey);

    /**
     * Check if configuration exists by key
     * Used for cache hit validation
     *
     * @param configKey the configuration key
     * @return true if configuration exists
     */
    boolean existsByConfigKey(String configKey);

    /**
     * Find configurations by type
     * Used for type-specific cache retrieval
     *
     * @param configType the configuration type
     * @return list of configurations of type
     */
    List<ConfigCache> findByConfigType(ConfigType configType);

    /**
     * Find all configurations ordered by key
     * Used for admin dashboard
     *
     * @return list of all configurations
     */
    @Query("SELECT cc FROM ConfigCache cc ORDER BY cc.configKey ASC")
    List<ConfigCache> findAllOrderedByKey();

    /**
     * Find stale configurations (older than 24 hours)
     * Used by scheduled job for cache refresh
     *
     * @param cutoffTime the staleness threshold (24 hours ago)
     * @return list of stale configurations
     */
    @Query("""
        SELECT cc FROM ConfigCache cc
        WHERE cc.lastLoadedAt < :cutoffTime
        ORDER BY cc.lastLoadedAt ASC
        """)
    List<ConfigCache> findStaleConfigurations(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find recently updated configurations (within 1 hour)
     * Used for reload notifications
     *
     * @param cutoffTime the time threshold (1 hour ago)
     * @return list of recently updated configurations
     */
    @Query("""
        SELECT cc FROM ConfigCache cc
        WHERE cc.lastLoadedAt >= :cutoffTime
        ORDER BY cc.lastLoadedAt DESC
        """)
    List<ConfigCache> findRecentlyUpdatedConfigurations(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count configurations by type
     * Used for cache statistics
     *
     * @param configType the configuration type
     * @return count of configurations
     */
    long countByConfigType(ConfigType configType);

    /**
     * Get total cache size in bytes
     * Used for cache monitoring
     *
     * @return total size of all cached configurations
     */
    @Query("""
        SELECT SUM(LENGTH(cc.configValue))
        FROM ConfigCache cc
        """)
    Optional<Long> getTotalCacheSizeBytes();

    /**
     * Find largest configurations by size
     * Used for cache optimization
     *
     * @param limit the maximum number of results
     * @return list of largest configurations
     */
    @Query("""
        SELECT cc FROM ConfigCache cc
        ORDER BY LENGTH(cc.configValue) DESC
        LIMIT :limit
        """)
    List<ConfigCache> findLargestConfigurations(@Param("limit") int limit);

    /**
     * Find configurations by checksum
     * Used for duplicate detection
     *
     * @param checksum the SHA-256 checksum
     * @return list of configurations with checksum
     */
    List<ConfigCache> findByChecksum(String checksum);

    /**
     * Get cache statistics
     * Used for admin dashboard
     *
     * @return object array [totalConfigs, totalSize, avgSize, oldestLoad]
     */
    @Query("""
        SELECT COUNT(cc.id),
               SUM(LENGTH(cc.configValue)),
               AVG(LENGTH(cc.configValue)),
               MIN(cc.lastLoadedAt)
        FROM ConfigCache cc
        """)
    Optional<Object[]> getCacheStatistics();

    /**
     * Find configurations by partial key match
     * Used for admin search
     *
     * @param searchTerm the search term
     * @return list of matching configurations
     */
    @Query("""
        SELECT cc FROM ConfigCache cc
        WHERE LOWER(cc.configKey) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           OR LOWER(cc.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        ORDER BY cc.configKey ASC
        """)
    List<ConfigCache> searchConfigurations(@Param("searchTerm") String searchTerm);

    /**
     * Find configuration by type and key pattern
     * Used for typed configuration retrieval
     *
     * @param configType the configuration type
     * @param keyPattern the key pattern (e.g., "%.json")
     * @return list of matching configurations
     */
    @Query("""
        SELECT cc FROM ConfigCache cc
        WHERE cc.configType = :configType
          AND cc.configKey LIKE :keyPattern
        ORDER BY cc.configKey ASC
        """)
    List<ConfigCache> findByTypeAndKeyPattern(
        @Param("configType") ConfigType configType,
        @Param("keyPattern") String keyPattern
    );

    /**
     * Get load time statistics
     * Used for performance monitoring
     *
     * @return list of object arrays [configKey, lastLoadedAt, hoursSinceLoad]
     */
    @Query("""
        SELECT cc.configKey,
               cc.lastLoadedAt,
               TIMESTAMPDIFF(HOUR, cc.lastLoadedAt, CURRENT_TIMESTAMP)
        FROM ConfigCache cc
        ORDER BY cc.lastLoadedAt ASC
        """)
    List<Object[]> getLoadTimeStatistics();

    /**
     * Find configurations never reloaded
     * Used for cache freshness audit
     *
     * @param ageThreshold the age threshold in hours
     * @return list of old configurations
     */
    @Query("""
        SELECT cc FROM ConfigCache cc
        WHERE TIMESTAMPDIFF(HOUR, cc.createdAt, cc.lastLoadedAt) = 0
          AND TIMESTAMPDIFF(HOUR, cc.createdAt, CURRENT_TIMESTAMP) > :ageThreshold
        ORDER BY cc.createdAt ASC
        """)
    List<ConfigCache> findNeverReloadedConfigurations(@Param("ageThreshold") long ageThreshold);

    /**
     * Get configuration type distribution
     * Used for analytics
     *
     * @return list of object arrays [configType, count, totalSize]
     */
    @Query("""
        SELECT cc.configType,
               COUNT(cc.id),
               SUM(LENGTH(cc.configValue))
        FROM ConfigCache cc
        GROUP BY cc.configType
        ORDER BY COUNT(cc.id) DESC
        """)
    List<Object[]> getConfigTypeDistribution();

    /**
     * Find most frequently accessed configurations
     * Inferred from last loaded time (more recent = more accessed)
     *
     * @param limit the maximum number of results
     * @return list of frequently accessed configurations
     */
    @Query("""
        SELECT cc FROM ConfigCache cc
        ORDER BY cc.lastLoadedAt DESC
        LIMIT :limit
        """)
    List<ConfigCache> findMostRecentlyAccessed(@Param("limit") int limit);

    /**
     * Delete configurations older than threshold
     * Used for cache cleanup
     *
     * @param cutoffTime the deletion threshold
     * @return number of deleted configurations
     */
    @Query("""
        DELETE FROM ConfigCache cc
        WHERE cc.lastLoadedAt < :cutoffTime
        """)
    int deleteStaleConfigurations(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find configuration keys by type
     * Used for key listing without loading full content
     *
     * @param configType the configuration type
     * @return list of configuration keys
     */
    @Query("""
        SELECT cc.configKey FROM ConfigCache cc
        WHERE cc.configType = :configType
        ORDER BY cc.configKey ASC
        """)
    List<String> findConfigKeysByType(@Param("configType") ConfigType configType);

    /**
     * Get oldest configuration by load time
     * Used for prioritizing reloads
     *
     * @return Optional containing oldest configuration
     */
    @Query("""
        SELECT cc FROM ConfigCache cc
        ORDER BY cc.lastLoadedAt ASC
        LIMIT 1
        """)
    Optional<ConfigCache> findOldestConfiguration();

    /**
     * Count stale configurations
     * Used for monitoring dashboard
     *
     * @param cutoffTime the staleness threshold
     * @return count of stale configurations
     */
    @Query("""
        SELECT COUNT(cc) FROM ConfigCache cc
        WHERE cc.lastLoadedAt < :cutoffTime
        """)
    long countStaleConfigurations(@Param("cutoffTime") LocalDateTime cutoffTime);
}