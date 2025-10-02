package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FeatureFlagRepository - Data access layer for FeatureFlag entity
 *
 * Provides:
 * - Feature flag retrieval by name
 * - Enabled/disabled feature queries
 * - Feature flag status tracking
 * - Admin management queries
 *
 * Used by:
 * - FeatureFlagService: Check and toggle features
 * - ConfigurationService: Feature availability
 * - AdminService: Feature management
 *
 * Supported Features:
 * - idiom-scramble: Idiom game mode
 * - sentence-crafting: Sentence game mode
 * - leaderboard: Leaderboard display
 * - audio-pronunciation: Audio feature
 * - hints: Hint system
 * - practice-mode: Practice mode
 * - achievements: Achievement system
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {

    /**
     * Find feature flag by name
     * Used for feature availability checks
     *
     * @param featureName the feature name (kebab-case)
     * @return Optional containing feature flag
     */
    Optional<FeatureFlag> findByFeatureName(String featureName);

    /**
     * Check if feature exists by name
     * Used for validation
     *
     * @param featureName the feature name
     * @return true if feature exists
     */
    boolean existsByFeatureName(String featureName);

    /**
     * Find all enabled features
     * Used for feature availability listing
     *
     * @return list of enabled feature flags
     */
    List<FeatureFlag> findByEnabledTrue();

    /**
     * Find all disabled features
     * Used for admin management
     *
     * @return list of disabled feature flags
     */
    List<FeatureFlag> findByEnabledFalse();

    /**
     * Check if a feature is enabled
     * Used for quick feature checks
     *
     * @param featureName the feature name
     * @return true if feature is enabled
     */
    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
        FROM FeatureFlag f
        WHERE f.featureName = :featureName
          AND f.enabled = true
        """)
    boolean isFeatureEnabled(@Param("featureName") String featureName);

    /**
     * Find recently enabled features (within 7 days)
     * Used for new feature notifications
     *
     * @param cutoffDate the date threshold (7 days ago)
     * @return list of recently enabled features
     */
    @Query("""
        SELECT f FROM FeatureFlag f
        WHERE f.enabled = true
          AND f.enabledAt >= :cutoffDate
        ORDER BY f.enabledAt DESC
        """)
    List<FeatureFlag> findRecentlyEnabledFeatures(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find recently disabled features (within 7 days)
     * Used for admin monitoring
     *
     * @param cutoffDate the date threshold (7 days ago)
     * @return list of recently disabled features
     */
    @Query("""
        SELECT f FROM FeatureFlag f
        WHERE f.enabled = false
          AND f.disabledAt >= :cutoffDate
        ORDER BY f.disabledAt DESC
        """)
    List<FeatureFlag> findRecentlyDisabledFeatures(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find all feature flags ordered by name
     * Used for admin dashboard
     *
     * @return list of all feature flags
     */
    @Query("SELECT f FROM FeatureFlag f ORDER BY f.featureName ASC")
    List<FeatureFlag> findAllOrderedByName();

    /**
     * Count enabled features
     * Used for analytics
     *
     * @return count of enabled features
     */
    long countByEnabledTrue();

    /**
     * Count disabled features
     * Used for analytics
     *
     * @return count of disabled features
     */
    long countByEnabledFalse();

    /**
     * Find features by partial name match
     * Used for admin search
     *
     * @param searchTerm the search term
     * @return list of matching feature flags
     */
    @Query("""
        SELECT f FROM FeatureFlag f
        WHERE LOWER(f.featureName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           OR LOWER(f.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        ORDER BY f.featureName ASC
        """)
    List<FeatureFlag> searchFeatures(@Param("searchTerm") String searchTerm);

    /**
     * Get feature flag statistics
     * Used for analytics dashboard
     *
     * @return object array [totalFeatures, enabledCount, disabledCount]
     */
    @Query("""
        SELECT COUNT(f.id),
               SUM(CASE WHEN f.enabled = true THEN 1 ELSE 0 END),
               SUM(CASE WHEN f.enabled = false THEN 1 ELSE 0 END)
        FROM FeatureFlag f
        """)
    Optional<Object[]> getFeatureFlagStatistics();

    /**
     * Find features that have never been enabled
     * Used for unused feature cleanup
     *
     * @return list of never-enabled features
     */
    @Query("""
        SELECT f FROM FeatureFlag f
        WHERE f.enabled = false
          AND f.enabledAt IS NULL
        ORDER BY f.createdAt ASC
        """)
    List<FeatureFlag> findNeverEnabledFeatures();

    /**
     * Find features enabled for a specific duration
     * Used for A/B testing analysis
     *
     * @param minDays minimum days enabled
     * @return list of long-running features
     */
    @Query("""
        SELECT f FROM FeatureFlag f
        WHERE f.enabled = true
          AND f.enabledAt IS NOT NULL
          AND f.enabledAt <= :cutoffDate
        ORDER BY f.enabledAt ASC
        """)
    List<FeatureFlag> findLongRunningFeatures(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find features that were toggled recently
     * Used for admin audit trail
     *
     * @param cutoffDate the date threshold
     * @return list of recently toggled features
     */
    @Query("""
        SELECT f FROM FeatureFlag f
        WHERE (f.enabledAt >= :cutoffDate OR f.disabledAt >= :cutoffDate)
        ORDER BY GREATEST(COALESCE(f.enabledAt, f.createdAt), COALESCE(f.disabledAt, f.createdAt)) DESC
        """)
    List<FeatureFlag> findRecentlyToggledFeatures(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get feature toggle history summary
     * Used for change tracking
     *
     * @param featureName the feature name
     * @return object array [featureName, enabledAt, disabledAt, currentStatus]
     */
    @Query("""
        SELECT f.featureName, f.enabledAt, f.disabledAt, f.enabled
        FROM FeatureFlag f
        WHERE f.featureName = :featureName
        """)
    Optional<Object[]> getFeatureToggleHistory(@Param("featureName") String featureName);
}