package com.govtech.chinesescramble.exception;

/**
 * Exception thrown when a feature flag is not found
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public class FeatureFlagNotFoundException extends RuntimeException {

    private final String featureKey;

    public FeatureFlagNotFoundException(String featureKey) {
        super("Feature flag not found: " + featureKey);
        this.featureKey = featureKey;
    }

    public String getFeatureKey() {
        return featureKey;
    }
}
