package com.govtech.chinesescramble.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test suite for ValidationService
 * Focus: Coverage improvement for low-coverage methods
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    private ValidationService validationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        validationService = new ValidationService(objectMapper);
    }

    // ========================================================================
    // validateIdiom() Tests - Already 100% but add edge cases
    // ========================================================================

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
        assertThat(result.isPerfect()).isTrue();
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
        assertThat(result.isPerfect()).isFalse();
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

    // ========================================================================
    // validateSentence() Tests - Target: 87% → 95%+
    // ========================================================================

    @Test
    void testValidateSentence_PerfectMatch() {
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
        assertThat(result.isPerfect()).isTrue();
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.errorCount()).isZero();
    }

    @Test
    void testValidateSentence_WithMissingWords() {
        // Given
        String playerSentence = "我学习中文"; // Missing "喜欢"
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.grammarScore()).isLessThan(100);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors()).anyMatch(e -> e.type().equals("MISSING_WORD"));
        assertThat(result.errors()).anyMatch(e -> e.message().contains("喜欢"));
    }

    @Test
    void testValidateSentence_WithExtraWords() {
        // Given
        String playerSentence = "我非常喜欢学习中文"; // Extra word "非常"
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.grammarScore()).isLessThan(100);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors()).anyMatch(e -> e.type().equals("EXTRA_WORD"));
        assertThat(result.errors()).anyMatch(e -> e.message().contains("非常"));
    }

    @Test
    void testValidateSentence_WithWordOrderErrors() {
        // Given
        String playerSentence = "中文学习喜欢我"; // Completely wrong order
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.grammarScore()).isLessThan(100);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors()).anyMatch(e -> e.type().equals("WORD_ORDER"));
        assertThat(result.similarityScore()).isLessThan(1.0);
    }

    @Test
    void testValidateSentence_WithInvalidCharacters_Numbers() {
        // Given
        String playerSentence = "我喜欢学习123中文"; // Contains numbers
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.grammarScore()).isZero();
        assertThat(result.similarityScore()).isZero();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors()).anyMatch(e -> e.type().equals("INVALID_CHARACTERS"));
        assertThat(result.errors()).anyMatch(e -> e.message().contains("无效字符"));
    }

    @Test
    void testValidateSentence_WithInvalidCharacters_EnglishLetters() {
        // Given
        String playerSentence = "我喜欢学习ABC中文"; // Contains English letters
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.grammarScore()).isZero();
        assertThat(result.errors()).anyMatch(e -> e.type().equals("INVALID_CHARACTERS"));
    }

    @Test
    void testValidateSentence_WithInvalidCharacters_SpecialSymbols() {
        // Given
        String playerSentence = "我喜欢学习@#$中文"; // Contains special symbols
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.grammarScore()).isZero();
        assertThat(result.errors()).anyMatch(e -> e.type().equals("INVALID_CHARACTERS"));
    }

    @Test
    void testValidateSentence_WithValidChinesePunctuation() {
        // Given - Chinese punctuation should be valid
        String playerSentence = "我喜欢学习中文。";
        String targetSentence = "我喜欢学习中文。";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.grammarScore()).isEqualTo(100);
    }

    @Test
    void testValidateSentence_MultipleErrors() {
        // Given - Missing words + extra words + wrong order
        String playerSentence = "非常学习中文"; // Missing "我", "喜欢", extra "非常"
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errorCount()).isGreaterThan(1);
        assertThat(result.grammarScore()).isLessThan(100);
    }

    @Test
    void testValidateSentence_GrammarScoreClampedToZero() {
        // Given - So many errors that score would be negative
        String playerSentence = "错误的很多词"; // All wrong words
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.grammarScore()).isGreaterThanOrEqualTo(0); // Clamped to 0
        assertThat(result.grammarScore()).isLessThanOrEqualTo(100); // Clamped to 100
    }

    @Test
    void testValidateSentence_EmptyPlayerSentence() {
        // Given
        String playerSentence = "";
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
    }

    // ========================================================================
    // errorsToJson() Tests - Target: 41% → 100%
    // ========================================================================

    @Test
    void testErrorsToJson_EmptyList() {
        // Given
        List<ValidationService.ValidationError> errors = List.of();

        // When
        String json = validationService.errorsToJson(errors);

        // Then
        assertThat(json).isEqualTo("[]");
    }

    @Test
    void testErrorsToJson_SingleError() {
        // Given
        List<ValidationService.ValidationError> errors = List.of(
            new ValidationService.ValidationError("WORD_ORDER", "词序错误", 0)
        );

        // When
        String json = validationService.errorsToJson(errors);

        // Then
        assertThat(json).contains("WORD_ORDER");
        assertThat(json).contains("词序错误");
        assertThat(json).contains("0");
    }

    @Test
    void testErrorsToJson_MultipleErrors() {
        // Given
        List<ValidationService.ValidationError> errors = List.of(
            new ValidationService.ValidationError("MISSING_WORD", "缺少必需的词：喜欢", -1),
            new ValidationService.ValidationError("EXTRA_WORD", "使用了不允许的词：非常", 2),
            new ValidationService.ValidationError("WORD_ORDER", "词序错误", -1)
        );

        // When
        String json = validationService.errorsToJson(errors);

        // Then
        assertThat(json).contains("MISSING_WORD");
        assertThat(json).contains("EXTRA_WORD");
        assertThat(json).contains("WORD_ORDER");
        assertThat(json).contains("喜欢");
        assertThat(json).contains("非常");
    }

    // ========================================================================
    // ValidationResult Record Helper Method Tests
    // ========================================================================

    @Test
    void testValidationResult_IsPerfect_True() {
        // Arrange
        ValidationService.ValidationResult result = new ValidationService.ValidationResult(
            true, 100, 1.0, List.of()
        );

        // Act & Assert
        assertThat(result.isPerfect()).isTrue();
    }

    @Test
    void testValidationResult_IsPerfect_False_LowGrammarScore() {
        // Arrange
        ValidationService.ValidationResult result = new ValidationService.ValidationResult(
            false, 90, 1.0, List.of()
        );

        // Act & Assert
        assertThat(result.isPerfect()).isFalse();
    }

    @Test
    void testValidationResult_IsPerfect_False_LowSimilarity() {
        // Arrange
        ValidationService.ValidationResult result = new ValidationService.ValidationResult(
            false, 100, 0.95, List.of()
        );

        // Act & Assert
        assertThat(result.isPerfect()).isFalse();
    }

    @Test
    void testValidationResult_HasErrors_True() {
        // Arrange
        ValidationService.ValidationResult result = new ValidationService.ValidationResult(
            false, 80, 0.85, List.of(
                new ValidationService.ValidationError("WORD_ORDER", "词序错误", -1)
            )
        );

        // Act & Assert
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errorCount()).isEqualTo(1);
    }

    @Test
    void testValidationResult_HasErrors_False() {
        // Arrange
        ValidationService.ValidationResult result = new ValidationService.ValidationResult(
            true, 100, 1.0, List.of()
        );

        // Act & Assert
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.errorCount()).isZero();
    }

    @Test
    void testValidationResult_ErrorCount_Multiple() {
        // Arrange
        ValidationService.ValidationResult result = new ValidationService.ValidationResult(
            false, 70, 0.80, List.of(
                new ValidationService.ValidationError("MISSING_WORD", "缺少词", -1),
                new ValidationService.ValidationError("EXTRA_WORD", "多余词", 2),
                new ValidationService.ValidationError("WORD_ORDER", "词序错误", -1)
            )
        );

        // Act & Assert
        assertThat(result.errorCount()).isEqualTo(3);
    }

    // ========================================================================
    // IdiomValidationResult Record Helper Method Tests
    // ========================================================================

    @Test
    void testIdiomValidationResult_IsPerfect_True() {
        // Arrange
        ValidationService.IdiomValidationResult result = new ValidationService.IdiomValidationResult(
            true, 1.0
        );

        // Act & Assert
        assertThat(result.isPerfect()).isTrue();
    }

    @Test
    void testIdiomValidationResult_IsPerfect_False_NotCorrect() {
        // Arrange
        ValidationService.IdiomValidationResult result = new ValidationService.IdiomValidationResult(
            false, 1.0
        );

        // Act & Assert
        assertThat(result.isPerfect()).isFalse();
    }

    @Test
    void testIdiomValidationResult_IsPerfect_False_LowAccuracy() {
        // Arrange
        ValidationService.IdiomValidationResult result = new ValidationService.IdiomValidationResult(
            true, 0.75
        );

        // Act & Assert
        assertThat(result.isPerfect()).isFalse();
    }

    // ========================================================================
    // Edge Cases for Similarity Calculation
    // ========================================================================

    @Test
    void testValidateSentence_HighSimilarity_SlightDifference() {
        // Given - Very similar but not exact
        String playerSentence = "我喜欢学中文";
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("我", "喜欢", "学习", "中文", "学");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.similarityScore()).isGreaterThan(0.8);
        assertThat(result.similarityScore()).isLessThan(1.0);
    }

    @Test
    void testValidateSentence_LowSimilarity_CompletelyDifferent() {
        // Given - Completely different sentences
        String playerSentence = "今天天气很好";
        String targetSentence = "我喜欢学习中文";
        List<String> allowedWords = List.of("今天", "天气", "很", "好");

        // When
        ValidationService.ValidationResult result = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        // Then
        assertThat(result.similarityScore()).isLessThan(0.5);
    }
}
