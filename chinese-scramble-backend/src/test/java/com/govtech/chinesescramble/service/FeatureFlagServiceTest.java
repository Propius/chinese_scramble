package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.FeatureFlag;
import com.govtech.chinesescramble.repository.FeatureFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for FeatureFlagService
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock
    private FeatureFlagRepository featureFlagRepository;

    @InjectMocks
    private FeatureFlagService featureFlagService;

    @BeforeEach
    void setUp() {
        reset(featureFlagRepository);
    }

    @Test
    void testIsFeatureEnabled_True() {
        // Given
        String featureName = "idiom_game_enabled";
        FeatureFlag flag = createFeatureFlag(featureName, "Idiom game", true);

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.of(flag));

        // When
        boolean enabled = featureFlagService.isFeatureEnabled(featureName);

        // Then
        assertThat(enabled).isTrue();
        verify(featureFlagRepository).findByFeatureName(featureName);
    }

    @Test
    void testIsFeatureEnabled_False() {
        // Given
        String featureName = "disabled_feature";
        FeatureFlag flag = createFeatureFlag(featureName, "Disabled", false);

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.of(flag));

        // When
        boolean enabled = featureFlagService.isFeatureEnabled(featureName);

        // Then
        assertThat(enabled).isFalse();
    }

    @Test
    void testIsFeatureEnabled_NotFound() {
        // Given
        String featureName = "nonexistent_feature";

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.empty());

        // When
        boolean enabled = featureFlagService.isFeatureEnabled(featureName);

        // Then
        assertThat(enabled).isFalse(); // Default to false for missing features
    }

    @Test
    void testEnableFeature() {
        // Given
        String featureName = "new_feature";
        FeatureFlag flag = createFeatureFlag(featureName, "New feature", false);

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.of(flag));
        when(featureFlagRepository.save(any(FeatureFlag.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        featureFlagService.enableFeature(featureName);

        // Then
        verify(featureFlagRepository).findByFeatureName(featureName);
        verify(featureFlagRepository).save(argThat(f -> f.getEnabled()));
    }

    @Test
    void testEnableFeature_NotFound() {
        // Given
        String featureName = "nonexistent";

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> featureFlagService.enableFeature(featureName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Feature flag not found");
    }

    @Test
    void testDisableFeature() {
        // Given
        String featureName = "enabled_feature";
        FeatureFlag flag = createFeatureFlag(featureName, "Enabled feature", true);

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.of(flag));
        when(featureFlagRepository.save(any(FeatureFlag.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        featureFlagService.disableFeature(featureName);

        // Then
        verify(featureFlagRepository).findByFeatureName(featureName);
        verify(featureFlagRepository).save(argThat(f -> !f.getEnabled()));
    }

    @Test
    void testGetFeature() {
        // Given
        String featureName = "test_feature";
        FeatureFlag flag = createFeatureFlag(featureName, "Test feature", true);

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.of(flag));

        // When
        Optional<FeatureFlag> result = featureFlagService.getFeature(featureName);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFeatureName()).isEqualTo(featureName);
    }

    @Test
    void testGetAllFeatures() {
        // Given
        List<FeatureFlag> flags = List.of(
            createFeatureFlag("feature1", "Feature 1", true),
            createFeatureFlag("feature2", "Feature 2", false),
            createFeatureFlag("feature3", "Feature 3", true)
        );

        when(featureFlagRepository.findAllOrderedByName()).thenReturn(flags);

        // When
        List<FeatureFlag> result = featureFlagService.getAllFeatures();

        // Then
        assertThat(result).hasSize(3);
        verify(featureFlagRepository).findAllOrderedByName();
    }

    @Test
    void testGetEnabledFeatures() {
        // Given
        List<FeatureFlag> enabledFlags = List.of(
            createFeatureFlag("feature1", "Feature 1", true),
            createFeatureFlag("feature2", "Feature 2", true)
        );

        when(featureFlagRepository.findByEnabledTrue())
            .thenReturn(enabledFlags);

        // When
        List<FeatureFlag> result = featureFlagService.getEnabledFeatures();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(f -> f.getEnabled());
    }

    @Test
    void testCreateFeatureFlag() {
        // Given
        String featureName = "new_feature";
        String description = "New feature description";

        when(featureFlagRepository.existsByFeatureName(featureName))
            .thenReturn(false);
        when(featureFlagRepository.save(any(FeatureFlag.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        FeatureFlag result = featureFlagService
            .createFeatureFlag(featureName, description, true);

        // Then
        assertThat(result.getFeatureName()).isEqualTo(featureName);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getEnabled()).isTrue();
        verify(featureFlagRepository).save(any(FeatureFlag.class));
    }

    @Test
    void testCreateFeatureFlag_AlreadyExists() {
        // Given
        String featureName = "existing_feature";

        when(featureFlagRepository.existsByFeatureName(featureName))
            .thenReturn(true);

        // When/Then
        assertThatThrownBy(() ->
            featureFlagService.createFeatureFlag(featureName, "desc", true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void testDeleteFeatureFlag() {
        // Given
        String featureName = "deletable_feature";
        FeatureFlag flag = createFeatureFlag(featureName, "Deletable", true);

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.of(flag));

        // When
        featureFlagService.deleteFeatureFlag(featureName);

        // Then
        verify(featureFlagRepository).findByFeatureName(featureName);
        verify(featureFlagRepository).delete(flag);
    }

    // Helper method

    private FeatureFlag createFeatureFlag(String featureName, String description,
                                         boolean enabled) {
        FeatureFlag flag = FeatureFlag.builder()
            .featureName(featureName)
            .description(description)
            .enabled(enabled)
            .build();
        // Set id via reflection for test purposes
        try {
            java.lang.reflect.Field idField = flag.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(flag, 1L);
        } catch (Exception ignored) {
        }
        return flag;
    }
}
