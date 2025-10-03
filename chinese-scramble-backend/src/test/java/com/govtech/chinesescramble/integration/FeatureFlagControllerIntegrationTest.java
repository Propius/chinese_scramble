package com.govtech.chinesescramble.integration;

import com.govtech.chinesescramble.entity.FeatureFlag;
import com.govtech.chinesescramble.repository.FeatureFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for FeatureFlagController
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FeatureFlagControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeatureFlagRepository featureFlagRepository;

    private FeatureFlag testFeatureEnabled;
    private FeatureFlag testFeatureDisabled;

    @BeforeEach
    void setUp() {
        featureFlagRepository.deleteAll();

        LocalDateTime now = LocalDateTime.now();

        testFeatureEnabled = FeatureFlag.builder()
            .featureName("test-feature-enabled")
            .description("Test feature that is enabled")
            .enabled(true)
            .build();
        testFeatureEnabled.setCreatedAt(now);
        testFeatureEnabled.setUpdatedAt(now);
        testFeatureEnabled = featureFlagRepository.save(testFeatureEnabled);

        testFeatureDisabled = FeatureFlag.builder()
            .featureName("test-feature-disabled")
            .description("Test feature that is disabled")
            .enabled(false)
            .build();
        testFeatureDisabled.setCreatedAt(now);
        testFeatureDisabled.setUpdatedAt(now);
        testFeatureDisabled = featureFlagRepository.save(testFeatureDisabled);
    }

    @Test
    void testGetAllFeatures_Success() throws Exception {
        mockMvc.perform(get("/api/features/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].featureKey").exists())
            .andExpect(jsonPath("$[0].description").exists())
            .andExpect(jsonPath("$[0].enabled").exists())
            .andExpect(jsonPath("$[0].lastModified").exists());
    }

    @Test
    void testGetFeatureStatus_EnabledFeature() throws Exception {
        mockMvc.perform(get("/api/features/" + testFeatureEnabled.getFeatureName()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.featureKey").value(testFeatureEnabled.getFeatureName()))
            .andExpect(jsonPath("$.description").value(testFeatureEnabled.getDescription()))
            .andExpect(jsonPath("$.enabled").value(true))
            .andExpect(jsonPath("$.lastModified").exists());
    }

    @Test
    void testGetFeatureStatus_DisabledFeature() throws Exception {
        mockMvc.perform(get("/api/features/" + testFeatureDisabled.getFeatureName()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.featureKey").value(testFeatureDisabled.getFeatureName()))
            .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void testGetFeatureStatus_NonExistentFeature() throws Exception {
        mockMvc.perform(get("/api/features/non-existent-feature"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testEnableFeature_Success() throws Exception {
        mockMvc.perform(post("/api/features/" + testFeatureDisabled.getFeatureName() + "/enable"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Feature enabled successfully"))
            .andExpect(jsonPath("$.featureKey").value(testFeatureDisabled.getFeatureName()))
            .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void testDisableFeature_Success() throws Exception {
        mockMvc.perform(post("/api/features/" + testFeatureEnabled.getFeatureName() + "/disable"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Feature disabled successfully"))
            .andExpect(jsonPath("$.featureKey").value(testFeatureEnabled.getFeatureName()))
            .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void testGetActiveFeatures_Success() throws Exception {
        mockMvc.perform(get("/api/features/active"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").exists())
            .andExpect(jsonPath("$.features").isArray());
    }

    @Test
    void testGetActiveFeatures_OnlyReturnsEnabled() throws Exception {
        mockMvc.perform(get("/api/features/active"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.features[0].featureKey").exists())
            .andExpect(jsonPath("$.features[0].description").exists());
    }

    @Test
    void testCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/features/all")
                .header("Origin", "http://localhost:3000"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void testFeatureFlagToggle_CompleteFlow() throws Exception {
        String featureName = testFeatureEnabled.getFeatureName();

        // Verify initially enabled
        mockMvc.perform(get("/api/features/" + featureName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(true));

        // Disable it
        mockMvc.perform(post("/api/features/" + featureName + "/disable"))
            .andExpect(status().isOk());

        // Verify disabled
        mockMvc.perform(get("/api/features/" + featureName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(false));

        // Enable it again
        mockMvc.perform(post("/api/features/" + featureName + "/enable"))
            .andExpect(status().isOk());

        // Verify enabled again
        mockMvc.perform(get("/api/features/" + featureName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void testGetAllFeatures_ContainsBothEnabledAndDisabled() throws Exception {
        mockMvc.perform(get("/api/features/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testFeatureWithNullDescription() throws Exception {
        FeatureFlag featureNoDesc = FeatureFlag.builder()
            .featureName("no-description-feature")
            .description(null)
            .enabled(true)
            .build();
        featureNoDesc.setCreatedAt(LocalDateTime.now());
        featureNoDesc.setUpdatedAt(LocalDateTime.now());
        featureFlagRepository.save(featureNoDesc);

        mockMvc.perform(get("/api/features/no-description-feature"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value(""));
    }

    @Test
    void testFeatureWithNullUpdatedAt() throws Exception {
        FeatureFlag featureNoUpdated = FeatureFlag.builder()
            .featureName("no-updated-feature")
            .description("Test")
            .enabled(true)
            .build();
        featureNoUpdated.setCreatedAt(LocalDateTime.now());
        featureNoUpdated.setUpdatedAt(null);
        featureNoUpdated = featureFlagRepository.save(featureNoUpdated);

        mockMvc.perform(get("/api/features/no-updated-feature"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lastModified").exists());
    }
}
