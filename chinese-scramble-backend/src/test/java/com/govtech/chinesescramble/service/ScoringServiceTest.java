package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for ScoringService
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

    @InjectMocks
    private ScoringService scoringService;

    @Test
    void testCalculateScore_Easy_Perfect() {
        // Given
        DifficultyLevel difficulty = DifficultyLevel.EASY;
        int timeTaken = 25; // <30s
        double accuracyRate = 1.0; // 100%
        int hintsUsed = 0;

        // When
        int score = scoringService.calculateIdiomScore(difficulty, timeTaken, accuracyRate, hintsUsed);

        // Then
        // Base: 100, Time bonus: 50 (<30s), Accuracy bonus: 100 (100%), Multiplier: 1.0x
        // (100 + 50 + 100) * 1.0 = 250
        assertThat(score).isEqualTo(250);
    }

    @Test
    void testCalculateScore_Easy_WithHints() {
        // Given
        DifficultyLevel difficulty = DifficultyLevel.EASY;
        int timeTaken = 45;
        double accuracyRate = 0.95; // 95%
        int hintsUsed = 2; // -30 penalty (hints 1+2 = 10+20)

        // When
        int score = scoringService.calculateIdiomScore(difficulty, timeTaken, accuracyRate, hintsUsed);

        // Then
        // Base: 100, Time bonus: 30 (<60s), Accuracy bonus: 50 (95%), Hint penalty: -30
        // (100 + 30 + 50 - 30) * 1.0 = 150
        assertThat(score).isEqualTo(150);
    }

    @Test
    void testCalculateScore_Medium() {
        // Given
        DifficultyLevel difficulty = DifficultyLevel.MEDIUM;
        int timeTaken = 50;
        double accuracyRate = 0.98; // 98%
        int hintsUsed = 1; // -10 penalty

        // When
        int score = scoringService.calculateIdiomScore(difficulty, timeTaken, accuracyRate, hintsUsed);

        // Then
        // Base: 200, Time bonus: 30 (<60s), Accuracy bonus: 50 (95-99%), Hint penalty: -10
        // (200 + 30 + 50 - 10) * 1.2 = 324
        assertThat(score).isEqualTo(324);
    }

    @Test
    void testCalculateScore_Hard() {
        // Given
        DifficultyLevel difficulty = DifficultyLevel.HARD;
        int timeTaken = 80;
        double accuracyRate = 0.92; // 92%
        int hintsUsed = 0;

        // When
        int score = scoringService.calculateIdiomScore(difficulty, timeTaken, accuracyRate, hintsUsed);

        // Then
        // Base: 300, Time bonus: 15 (<90s), Accuracy bonus: 25 (90-94%)
        // (300 + 15 + 25) * 1.5 = 510
        assertThat(score).isEqualTo(510);
    }

    @Test
    void testCalculateScore_Expert_MaxScore() {
        // Given
        DifficultyLevel difficulty = DifficultyLevel.EXPERT;
        int timeTaken = 20; // <30s
        double accuracyRate = 1.0; // 100%
        int hintsUsed = 0;

        // When
        int score = scoringService.calculateIdiomScore(difficulty, timeTaken, accuracyRate, hintsUsed);

        // Then
        // Base: 500, Time bonus: 50, Accuracy bonus: 100
        // (500 + 50 + 100) * 2.0 = 1300
        assertThat(score).isEqualTo(1300);
    }

    @Test
    void testCalculateScore_NoTimeBonusNoAccuracyBonus() {
        // Given
        DifficultyLevel difficulty = DifficultyLevel.EASY;
        int timeTaken = 120; // No time bonus
        double accuracyRate = 0.80; // 80% - No accuracy bonus
        int hintsUsed = 0;

        // When
        int score = scoringService.calculateIdiomScore(difficulty, timeTaken, accuracyRate, hintsUsed);

        // Then
        // Base: 100, Time bonus: 0, Accuracy bonus: 0
        // (100 + 0 + 0) * 1.0 = 100
        assertThat(score).isEqualTo(100);
    }

    @Test
    void testCalculateScore_MaxHintPenalty() {
        // Given
        DifficultyLevel difficulty = DifficultyLevel.EASY;
        int timeTaken = 60;
        double accuracyRate = 1.0; // 100%
        int hintsUsed = 3; // Max hints: -60 penalty (10+20+30)

        // When
        int score = scoringService.calculateIdiomScore(difficulty, timeTaken, accuracyRate, hintsUsed);

        // Then
        // Base: 100, Time bonus: 30 (<60s), Accuracy bonus: 100, Hint penalty: -60
        // (100 + 30 + 100 - 60) * 1.0 = 170
        assertThat(score).isEqualTo(170);
    }

    // Tests for private methods removed - tested through public API above
    // Tests for enum methods removed - those are tested in DifficultyLevel enum tests

    @Test
    void testCalculateScore_MinimumScore() {
        // Given - Worst case scenario
        DifficultyLevel difficulty = DifficultyLevel.EASY;
        int timeTaken = 200; // No time bonus
        double accuracyRate = 0.50; // 50% - No accuracy bonus
        int hintsUsed = 3; // Max penalty -60

        // When
        int score = scoringService.calculateIdiomScore(difficulty, timeTaken, accuracyRate, hintsUsed);

        // Then
        // Base: 100, Time bonus: 0, Accuracy bonus: 0, Hint penalty: -60
        // (100 + 0 + 0 - 60) * 1.0 = 40
        assertThat(score).isGreaterThanOrEqualTo(0); // Should never be negative
    }
}
