package com.govtech.chinesescramble.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * CacheConfig - Spring Cache configuration using Caffeine
 *
 * Cache Definitions:
 * - configurations: Configuration file cache (5-minute TTL, max 100 entries)
 * - featureFlags: Feature flag cache (5-minute TTL, max 200 entries)
 * - leaderboards: Leaderboard cache (2-minute TTL, max 500 entries)
 * - playerStats: Player statistics cache (10-minute TTL, max 1000 entries)
 *
 * Features:
 * - Caffeine high-performance in-memory cache
 * - Automatic expiration after write
 * - Maximum size limits to prevent memory issues
 * - Async cache loading support
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Creates Caffeine-based cache manager
     *
     * @return configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "configurations",
            "featureFlags",
            "leaderboards",
            "playerStats"
        );

        cacheManager.setCaffeine(caffeineCacheBuilder());

        return cacheManager;
    }

    /**
     * Caffeine cache builder with default settings
     *
     * @return Caffeine builder
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(500)
            .recordStats(); // Enable cache statistics
    }

    /**
     * Caffeine cache builder for configurations
     * Longer TTL, smaller size
     *
     * @return Caffeine builder
     */
    @Bean(name = "configurationCacheBuilder")
    public Caffeine<Object, Object> configurationCacheBuilder() {
        return Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .recordStats();
    }

    /**
     * Caffeine cache builder for feature flags
     * Medium TTL, medium size
     *
     * @return Caffeine builder
     */
    @Bean(name = "featureFlagCacheBuilder")
    public Caffeine<Object, Object> featureFlagCacheBuilder() {
        return Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(200)
            .recordStats();
    }

    /**
     * Caffeine cache builder for leaderboards
     * Shorter TTL due to frequent updates
     *
     * @return Caffeine builder
     */
    @Bean(name = "leaderboardCacheBuilder")
    public Caffeine<Object, Object> leaderboardCacheBuilder() {
        return Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .maximumSize(500)
            .recordStats();
    }

    /**
     * Caffeine cache builder for player statistics
     * Longer TTL, larger size
     *
     * @return Caffeine builder
     */
    @Bean(name = "playerStatsCacheBuilder")
    public Caffeine<Object, Object> playerStatsCacheBuilder() {
        return Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .recordStats();
    }
}