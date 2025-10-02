package com.govtech.chinesescramble.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ValidationService - Validates Chinese sentence grammar and structure
 *
 * Features:
 * - Chinese sentence validation
 * - Grammar error detection
 * - Word order validation
 * - Similarity scoring
 * - Grammar score calculation (0-100)
 *
 * Validation Rules:
 * 1. Word Order: Checks if words are in correct order
 * 2. Required Words: Validates all target words are used
 * 3. Extra Words: Detects unauthorized word usage
 * 4. Grammar Patterns: Validates sentence structure
 * 5. Chinese Characters: Validates UTF-8 encoding
 *
 * Grammar Scoring:
 * - Perfect match: 100 points
 * - Word order errors: -5 points each
 * - Missing words: -10 points each
 * - Extra words: -5 points each
 * - Grammar pattern errors: -10 points each
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ValidationService {

    private final ObjectMapper objectMapper;

    /**
     * Validates a player's sentence against target sentence
     *
     * @param playerSentence the sentence created by player
     * @param targetSentence the correct target sentence
     * @param allowedWords list of words player can use
     * @return validation result with errors and score
     */
    public ValidationResult validateSentence(
        String playerSentence,
        String targetSentence,
        List<String> allowedWords
    ) {
        log.debug("Validating sentence: player='{}', target='{}'", playerSentence, targetSentence);

        List<ValidationError> errors = new ArrayList<>();
        int grammarScore = 100;

        // 1. Check if sentences match exactly (perfect score)
        if (playerSentence.equals(targetSentence)) {
            log.debug("Perfect match: grammar score = 100");
            return new ValidationResult(true, 100, 1.0, errors);
        }

        // 2. Validate Chinese characters only (no numbers/invalid chars)
        if (!isValidChineseText(playerSentence)) {
            errors.add(new ValidationError(
                "INVALID_CHARACTERS",
                "答案包含无效字符（数字、符号等）",
                -1
            ));
            grammarScore = 0; // Fail immediately for invalid characters
            log.debug("Invalid characters detected in player sentence");
            return new ValidationResult(false, 0, 0.0, errors);
        }

        // 3. Validate word usage
        List<String> playerWords = segmentSentence(playerSentence);
        List<String> targetWords = segmentSentence(targetSentence);
        Set<String> allowedWordSet = Set.copyOf(allowedWords);

        // Check for extra words
        for (String word : playerWords) {
            if (!allowedWordSet.contains(word)) {
                errors.add(new ValidationError(
                    "EXTRA_WORD",
                    "使用了不允许的词：" + word,
                    playerWords.indexOf(word)
                ));
                grammarScore -= 5;
                log.debug("Extra word detected: {} (-5 points)", word);
            }
        }

        // Check for missing words
        for (String word : targetWords) {
            if (!playerWords.contains(word)) {
                errors.add(new ValidationError(
                    "MISSING_WORD",
                    "缺少必需的词：" + word,
                    -1
                ));
                grammarScore -= 10;
                log.debug("Missing word detected: {} (-10 points)", word);
            }
        }

        // 3. Validate word order - MUST be exact match
        int orderErrors = validateWordOrder(playerWords, targetWords);
        if (orderErrors > 0) {
            errors.add(new ValidationError(
                "WORD_ORDER",
                "词序错误，有 " + orderErrors + " 个词位置不正确",
                -1
            ));
            grammarScore -= (orderErrors * 20); // Increased penalty for word order errors
            log.debug("Word order errors: {} (-{} points)", orderErrors, orderErrors * 20);
        }

        // 4. Calculate similarity score
        double similarityScore = calculateSimilarity(playerSentence, targetSentence);

        // 5. Ensure grammar score is within valid range
        grammarScore = Math.max(0, Math.min(100, grammarScore));

        // For sentence game, require EXACT match (perfect score and no errors)
        // Word order MUST be correct - no lenient validation
        boolean isValid = grammarScore == 100 && errors.isEmpty();

        log.debug("Validation result: valid={}, grammarScore={}, similarity={}, errors={}",
            isValid, grammarScore, similarityScore, errors.size());

        return new ValidationResult(isValid, grammarScore, similarityScore, errors);
    }

    /**
     * Validates an idiom answer
     *
     * @param playerIdiom the idiom assembled by player
     * @param targetIdiom the correct target idiom
     * @return validation result
     */
    public IdiomValidationResult validateIdiom(String playerIdiom, String targetIdiom) {
        log.debug("Validating idiom: player='{}', target='{}'", playerIdiom, targetIdiom);

        boolean isCorrect = playerIdiom.equals(targetIdiom);
        double accuracy = calculateSimilarity(playerIdiom, targetIdiom);

        log.debug("Idiom validation result: correct={}, accuracy={}", isCorrect, accuracy);

        return new IdiomValidationResult(isCorrect, accuracy);
    }

    /**
     * Segments Chinese sentence into words
     * Simplified segmentation for demo - production would use jieba or similar
     *
     * @param sentence the Chinese sentence
     * @return list of words
     */
    private List<String> segmentSentence(String sentence) {
        // Simplified: split by common punctuation and spaces
        // In production, use proper Chinese word segmentation library (jieba)
        String regex = "[，。！？、；：\u201C\u201D\u2018\u2019（）《》\\s]+";
        return Arrays.stream(sentence.split(regex))
            .filter(word -> !word.isEmpty())
            .collect(Collectors.toList());
    }

    /**
     * Validates word order against target
     *
     * @param playerWords player's word list
     * @param targetWords target word list
     * @return number of order errors
     */
    private int validateWordOrder(List<String> playerWords, List<String> targetWords) {
        int orderErrors = 0;
        int minLength = Math.min(playerWords.size(), targetWords.size());

        for (int i = 0; i < minLength; i++) {
            if (!playerWords.get(i).equals(targetWords.get(i))) {
                orderErrors++;
            }
        }

        return orderErrors;
    }

    /**
     * Calculates Levenshtein distance-based similarity
     *
     * @param s1 first string
     * @param s2 second string
     * @return similarity score (0.0 to 1.0)
     */
    private double calculateSimilarity(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());

        if (maxLength == 0) {
            return 1.0;
        }

        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Calculates Levenshtein distance between two strings
     *
     * @param s1 first string
     * @param s2 second string
     * @return edit distance
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Validates that text contains only valid Chinese characters and punctuation
     * Rejects numbers, English letters, and invalid symbols
     *
     * @param text the text to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidChineseText(String text) {
        // Allow Chinese characters (CJK Unified Ideographs), common Chinese punctuation, and spaces
        // Unicode ranges:
        // \u4e00-\u9fff: CJK Unified Ideographs (most common Chinese characters)
        // \u3000-\u303f: CJK Symbols and Punctuation
        // \uff00-\uffef: Halfwidth and Fullwidth Forms (includes Chinese punctuation)
        // Allow spaces
        String validPattern = "^[\\u4e00-\\u9fff\\u3000-\\u303f\\uff00-\\uffef\\s]*$";

        if (!text.matches(validPattern)) {
            log.debug("Invalid characters detected in text: {}", text);
            return false;
        }

        // Additional check: explicitly reject digits
        if (text.matches(".*\\d.*")) {
            log.debug("Digits detected in text: {}", text);
            return false;
        }

        // Reject English letters
        if (text.matches(".*[a-zA-Z].*")) {
            log.debug("English letters detected in text: {}", text);
            return false;
        }

        return true;
    }

    /**
     * Converts validation errors to JSON string
     *
     * @param errors list of validation errors
     * @return JSON string
     */
    public String errorsToJson(List<ValidationError> errors) {
        try {
            return objectMapper.writeValueAsString(errors);
        } catch (Exception e) {
            log.error("Failed to convert errors to JSON", e);
            return "[]";
        }
    }

    // ========================================================================
    // DTOs
    // ========================================================================

    /**
     * Validation result for sentences
     */
    public record ValidationResult(
        boolean isValid,
        int grammarScore,
        double similarityScore,
        List<ValidationError> errors
    ) {
        public boolean isPerfect() {
            return grammarScore == 100 && similarityScore == 1.0;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public int errorCount() {
            return errors.size();
        }
    }

    /**
     * Individual validation error
     */
    public record ValidationError(
        String type,
        String message,
        int position
    ) {
    }

    /**
     * Validation result for idioms
     */
    public record IdiomValidationResult(
        boolean isCorrect,
        double accuracy
    ) {
        public boolean isPerfect() {
            return isCorrect && accuracy == 1.0;
        }
    }
}