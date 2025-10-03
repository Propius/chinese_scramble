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

    @Test
    void testEnableFeature_AlreadyEnabled() {
        // Given: Feature is already enabled
        String featureName = "already_enabled";
        FeatureFlag flag = createFeatureFlag(featureName, "Already enabled", true);

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.of(flag));

        // When
        FeatureFlag result = featureFlagService.enableFeature(featureName);

        // Then: Should return the flag without saving again
        assertThat(result.getEnabled()).isTrue();
        verify(featureFlagRepository, never()).save(any(FeatureFlag.class));
    }

    @Test
    void testDisableFeature_AlreadyDisabled() {
        // Given: Feature is already disabled
        String featureName = "already_disabled";
        FeatureFlag flag = createFeatureFlag(featureName, "Already disabled", false);

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.of(flag));

        // When
        FeatureFlag result = featureFlagService.disableFeature(featureName);

        // Then: Should return the flag without saving again
        assertThat(result.getEnabled()).isFalse();
        verify(featureFlagRepository, never()).save(any(FeatureFlag.class));
    }

    @Test
    void testToggleFeature_FromEnabledToDisabled() {
        // Given
        String featureName = "toggle_feature";
        FeatureFlag flag = createFeatureFlag(featureName, "Toggle test", true);

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.of(flag));
        when(featureFlagRepository.save(any(FeatureFlag.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        FeatureFlag result = featureFlagService.toggleFeature(featureName);

        // Then
        verify(featureFlagRepository).save(any(FeatureFlag.class));
    }

    @Test
    void testToggleFeature_NotFound() {
        // Given
        String featureName = "nonexistent";

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> featureFlagService.toggleFeature(featureName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Feature flag not found");
    }

    @Test
    void testUpdateFeatureDescription() {
        // Given
        String featureName = "update_desc_feature";
        String newDescription = "Updated description";
        FeatureFlag flag = createFeatureFlag(featureName, "Old description", true);

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.of(flag));
        when(featureFlagRepository.save(any(FeatureFlag.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        FeatureFlag result = featureFlagService.updateFeatureDescription(featureName, newDescription);

        // Then
        assertThat(result.getDescription()).isEqualTo(newDescription);
        verify(featureFlagRepository).save(any(FeatureFlag.class));
    }

    @Test
    void testUpdateFeatureDescription_NotFound() {
        // Given
        String featureName = "nonexistent";

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> featureFlagService.updateFeatureDescription(featureName, "new desc"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Feature flag not found");
    }

    @Test
    void testGetFeatureFlag() {
        // Given
        String featureName = "test_feature";
        FeatureFlag flag = createFeatureFlag(featureName, "Test", true);

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.of(flag));

        // When
        Optional<FeatureFlag> result = featureFlagService.getFeatureFlag(featureName);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFeatureName()).isEqualTo(featureName);
    }

    @Test
    void testGetAllFeatureFlags() {
        // Given
        List<FeatureFlag> flags = List.of(
            createFeatureFlag("feature1", "Feature 1", true),
            createFeatureFlag("feature2", "Feature 2", false)
        );

        when(featureFlagRepository.findAllOrderedByName()).thenReturn(flags);

        // When
        List<FeatureFlag> result = featureFlagService.getAllFeatureFlags();

        // Then
        assertThat(result).hasSize(2);
        verify(featureFlagRepository).findAllOrderedByName();
    }

    @Test
    void testGetDisabledFeatures() {
        // Given
        List<FeatureFlag> disabledFlags = List.of(
            createFeatureFlag("feature1", "Feature 1", false),
            createFeatureFlag("feature2", "Feature 2", false)
        );

        when(featureFlagRepository.findByEnabledFalse())
            .thenReturn(disabledFlags);

        // When
        List<FeatureFlag> result = featureFlagService.getDisabledFeatures();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(f -> !f.getEnabled());
    }

    @Test
    void testGetRecentlyEnabledFeatures() {
        // Given
        List<FeatureFlag> recentlyEnabled = List.of(
            createFeatureFlag("feature1", "Recent 1", true)
        );

        when(featureFlagRepository.findRecentlyEnabledFeatures(any(LocalDateTime.class)))
            .thenReturn(recentlyEnabled);

        // When
        List<FeatureFlag> result = featureFlagService.getRecentlyEnabledFeatures();

        // Then
        assertThat(result).hasSize(1);
        verify(featureFlagRepository).findRecentlyEnabledFeatures(any(LocalDateTime.class));
    }

    @Test
    void testGetRecentlyDisabledFeatures() {
        // Given
        List<FeatureFlag> recentlyDisabled = List.of(
            createFeatureFlag("feature1", "Recent 1", false)
        );

        when(featureFlagRepository.findRecentlyDisabledFeatures(any(LocalDateTime.class)))
            .thenReturn(recentlyDisabled);

        // When
        List<FeatureFlag> result = featureFlagService.getRecentlyDisabledFeatures();

        // Then
        assertThat(result).hasSize(1);
        verify(featureFlagRepository).findRecentlyDisabledFeatures(any(LocalDateTime.class));
    }

    @Test
    void testSearchFeatures() {
        // Given
        String searchTerm = "game";
        List<FeatureFlag> searchResults = List.of(
            createFeatureFlag("idiom-game", "Idiom game feature", true),
            createFeatureFlag("sentence-game", "Sentence game feature", true)
        );

        when(featureFlagRepository.searchFeatures(searchTerm))
            .thenReturn(searchResults);

        // When
        List<FeatureFlag> result = featureFlagService.searchFeatures(searchTerm);

        // Then
        assertThat(result).hasSize(2);
        verify(featureFlagRepository).searchFeatures(searchTerm);
    }

    @Test
    void testFeatureFlagExists_True() {
        // Given
        String featureName = "existing_feature";

        when(featureFlagRepository.existsByFeatureName(featureName))
            .thenReturn(true);

        // When
        boolean exists = featureFlagService.featureFlagExists(featureName);

        // Then
        assertThat(exists).isTrue();
        verify(featureFlagRepository).existsByFeatureName(featureName);
    }

    @Test
    void testFeatureFlagExists_False() {
        // Given
        String featureName = "nonexistent_feature";

        when(featureFlagRepository.existsByFeatureName(featureName))
            .thenReturn(false);

        // When
        boolean exists = featureFlagService.featureFlagExists(featureName);

        // Then
        assertThat(exists).isFalse();
        verify(featureFlagRepository).existsByFeatureName(featureName);
    }

    @Test
    void testDeleteFeatureFlag_NotFound() {
        // Given
        String featureName = "nonexistent";

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> featureFlagService.deleteFeatureFlag(featureName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Feature flag not found");
    }

    @Test
    void testGetStatistics() {
        // Given
        when(featureFlagRepository.count()).thenReturn(10L);
        when(featureFlagRepository.countByEnabledTrue()).thenReturn(7L);
        when(featureFlagRepository.countByEnabledFalse()).thenReturn(3L);

        // When
        FeatureFlagService.FeatureFlagStatistics stats = featureFlagService.getStatistics();

        // Then
        assertThat(stats.totalFlags()).isEqualTo(10L);
        assertThat(stats.enabledCount()).isEqualTo(7L);
        assertThat(stats.disabledCount()).isEqualTo(3L);
        assertThat(stats.enabledPercentage()).isEqualTo(70.0);
        assertThat(stats.disabledPercentage()).isEqualTo(30.0);
    }

    @Test
    void testGetStatistics_Empty() {
        // Given
        when(featureFlagRepository.count()).thenReturn(0L);
        when(featureFlagRepository.countByEnabledTrue()).thenReturn(0L);
        when(featureFlagRepository.countByEnabledFalse()).thenReturn(0L);

        // When
        FeatureFlagService.FeatureFlagStatistics stats = featureFlagService.getStatistics();

        // Then
        assertThat(stats.totalFlags()).isEqualTo(0L);
        assertThat(stats.enabledPercentage()).isEqualTo(0.0);
        assertThat(stats.disabledPercentage()).isEqualTo(0.0);
    }

    @Test
    void testClearAllCaches() {
        // When
        featureFlagService.clearAllCaches();

        // Then: Just verifies the method executes without error
        // Cache eviction is handled by Spring annotations
    }

    @Test
    void testDisableFeature_NotFound() {
        // Given
        String featureName = "nonexistent";

        when(featureFlagRepository.findByFeatureName(featureName))
            .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> featureFlagService.disableFeature(featureName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Feature flag not found");
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
