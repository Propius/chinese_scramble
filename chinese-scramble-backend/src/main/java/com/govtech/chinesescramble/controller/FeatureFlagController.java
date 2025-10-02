package com.govtech.chinesescramble.controller;

import com.govtech.chinesescramble.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * FeatureFlagController - REST API for feature flag management
 *
 * Endpoints:
 * - GET /api/features/all - Get all feature flags
 * - GET /api/features/{key} - Get specific feature flag status
 * - POST /api/features/{key}/enable - Enable feature (admin)
 * - POST /api/features/{key}/disable - Disable feature (admin)
 * - GET /api/features/active - Get all active features
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    /**
     * Gets all feature flags with their status
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllFeatures() {
        try {
            var features = featureFlagService.getAllFeatures();

            var response = features.stream()
                .map(f -> Map.of(
                    "id", f.getId(),
                    "featureKey", f.getFeatureName(),
                    "description", f.getDescription() != null ? f.getDescription() : "",
                    "enabled", f.getEnabled(),
                    "lastModified", f.getUpdatedAt() != null ? f.getUpdatedAt().toString() : f.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch all features", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets specific feature flag status
     */
    @GetMapping("/{key}")
    public ResponseEntity<?> getFeatureStatus(@PathVariable String key) {
        try {
            boolean enabled = featureFlagService.isFeatureEnabled(key);
            var feature = featureFlagService.getFeature(key);

            if (feature.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var f = feature.get();
            return ResponseEntity.ok(Map.of(
                "featureKey", f.getFeatureName(),
                "description", f.getDescription() != null ? f.getDescription() : "",
                "enabled", enabled,
                "lastModified", f.getUpdatedAt() != null ? f.getUpdatedAt().toString() : f.getCreatedAt().toString()
            ));
        } catch (Exception e) {
            log.error("Failed to fetch feature status", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Enables a feature flag (admin only)
     */
    @PostMapping("/{key}/enable")
    public ResponseEntity<?> enableFeature(@PathVariable String key) {
        try {
            featureFlagService.enableFeature(key);
            return ResponseEntity.ok(Map.of(
                "message", "Feature enabled successfully",
                "featureKey", key,
                "enabled", true
            ));
        } catch (Exception e) {
            log.error("Failed to enable feature", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Disables a feature flag (admin only)
     */
    @PostMapping("/{key}/disable")
    public ResponseEntity<?> disableFeature(@PathVariable String key) {
        try {
            featureFlagService.disableFeature(key);
            return ResponseEntity.ok(Map.of(
                "message", "Feature disabled successfully",
                "featureKey", key,
                "enabled", false
            ));
        } catch (Exception e) {
            log.error("Failed to disable feature", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets all currently active features
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveFeatures() {
        try {
            var features = featureFlagService.getAllFeatures();

            var activeFeatures = features.stream()
                .filter(f -> f.getEnabled())
                .map(f -> Map.of(
                    "featureKey", f.getFeatureName(),
                    "description", f.getDescription() != null ? f.getDescription() : ""
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "count", activeFeatures.size(),
                "features", activeFeatures
            ));
        } catch (Exception e) {
            log.error("Failed to fetch active features", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
