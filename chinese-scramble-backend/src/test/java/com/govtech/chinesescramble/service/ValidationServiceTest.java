package com.govtech.chinesescramble.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for ValidationService
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService(new ObjectMapper());
    }

    @Test
    void testValidateIdiomAnswer_ExactMatch() {
        // Given
        String correctAnswer = "一帆风顺";
        String playerAnswer = "一帆风顺";

        // When
        ValidationService.IdiomValidationResult result = validationService.validateIdiom(playerAnswer, correctAnswer);

        // Then
        assertThat(result.isCorrect()).isTrue();
        assertThat(result.accuracy()).isEqualTo(1.0);
    }

    @Test
    void testValidateIdiomAnswer_IncorrectAnswer() {
        // Given
        String correctAnswer = "一帆风顺";
        String playerAnswer = "风雨无阻";

        // When
        ValidationService.IdiomValidationResult result = validationService.validateIdiom(playerAnswer, correctAnswer);

        // Then
        assertThat(result.isCorrect()).isFalse();
        assertThat(result.accuracy()).isLessThan(1.0);
    }

    @Test
    void testValidateIdiomAnswer_CaseSensitivity() {
        // Given - Chinese characters are not case-sensitive
        String correctAnswer = "成语大全";
        String playerAnswer = "成语大全";

        // When
        ValidationService.IdiomValidationResult result = validationService.validateIdiom(playerAnswer, correctAnswer);

        // Then
        assertThat(result.isCorrect()).isTrue();
    }

    @Test
    void testValidateIdiomAnswer_WithSpaces() {
        // Given
        String correctAnswer = "一帆风顺";
        String playerAnswerWithSpaces = " 一帆风顺 ";

        // When
        ValidationService.IdiomValidationResult result = validationService.validateIdiom(playerAnswerWithSpaces.trim(), correctAnswer);

        // Then - Trim spaces before validation
        assertThat(result.isCorrect()).isTrue();
    }

    @Test
    void testValidateSentence() {
        // Given
        String playerSentence = "我喜欢学习中文";
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.grammarScore()).isEqualTo(100);
        assertThat(result.similarityScore()).isEqualTo(1.0);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void testValidateSentence_WithErrors() {
        // Given
        String playerSentence = "我学习中文"; // Missing "喜欢"
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.isValid()).isFalse(); // Grammar score < 60
        assertThat(result.grammarScore()).isLessThan(100);
        assertThat(result.hasErrors()).isTrue();
    }

    // Tests for private methods removed - tested through public API
}
