package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.Achievement;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.SentenceScore;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.exception.AllQuestionsCompletedException;
import com.govtech.chinesescramble.repository.PlayerRepository;
import com.govtech.chinesescramble.repository.SentenceScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SentenceGameService - Manages Chinese sentence crafting game logic
 *
 * Game Flow:
 * 1. Player selects difficulty level
 * 2. System selects random sentence from configuration
 * 3. Words are scrambled randomly
 * 4. Player arranges words in correct order
 * 5. Answer is validated (grammar + similarity)
 * 6. Score is calculated based on grammar, accuracy, time, hints
 * 7. Achievements and leaderboard are updated
 *
 * Scrambling Algorithm:
 * - Fisher-Yates shuffle for random word order
 * - Ensures scrambled order is different from original
 * - Maintains word integrity (不分割词语)
 *
 * Validation:
 * - Grammar scoring: 0-100 points
 * - Similarity: Levenshtein distance-based
 * - Word order validation
 * - Missing/extra word detection
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SentenceGameService {

    private final PlayerRepository playerRepository;
    private final SentenceScoreRepository sentenceScoreRepository;
    private final ConfigurationService configurationService;
    private final ValidationService validationService;
    private final ScoringService scoringService;
    private final AchievementService achievementService;
    private final LeaderboardService leaderboardService;
    private final GameSessionService gameSessionService;
    private final QuestionHistoryService questionHistoryService;

    @Value("${app.features.no-repeat-questions:false}")
    private boolean enableNoRepeatQuestions;

    private final Random random = new Random();

    /**
     * Starts a new sentence game
     *
     * @param playerId player ID
     * @param difficulty difficulty level
     * @return game state with scrambled words
     */
    public SentenceGameState startGame(Long playerId, DifficultyLevel difficulty) {
        log.info("Starting sentence game: player={}, difficulty={}", playerId, difficulty);

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        // Load sentences configuration
        Map<String, Object> config = configurationService.loadConfigurationAsMap("sentences.json");
        List<Map<String, Object>> sentences = (List<Map<String, Object>>) config.get("sentences");

        // Filter by difficulty
        List<Map<String, Object>> difficultySentences = sentences.stream()
            .filter(sentence -> difficulty.name().equals(sentence.get("difficulty")))
            .collect(Collectors.toList());

        if (difficultySentences.isEmpty()) {
            throw new IllegalStateException("No sentences found for difficulty: " + difficulty);
        }

        // Determine available sentences based on feature flag
        List<Map<String, Object>> availableSentences;

        if (enableNoRepeatQuestions) {
            // NO-REPEAT ENABLED: Filter out previously shown questions
            log.debug("No-repeat feature ENABLED - checking question history for player: {}", playerId);

            Set<String> excludedSentences = questionHistoryService.getExcludedQuestions(playerId, "SENTENCE");

            availableSentences = difficultySentences.stream()
                .filter(sentence -> !excludedSentences.contains((String) sentence.get("targetSentence")))
                .collect(Collectors.toList());

            // Check if all questions have been completed
            if (availableSentences.isEmpty()) {
                log.info("All sentences completed for player {}, difficulty={}, total={}",
                    playerId, difficulty, difficultySentences.size());
                throw new AllQuestionsCompletedException(
                    String.format("恭喜！您已完成所有 %s 难度的造句题目！", difficulty.getLabel())
                );
            }
        } else {
            // NO-REPEAT DISABLED: Use all questions (questions can repeat)
            log.debug("No-repeat feature DISABLED - all questions available for player: {}", playerId);
            availableSentences = difficultySentences;
        }

        // Select random sentence from available pool
        Map<String, Object> selectedSentence = availableSentences.get(random.nextInt(availableSentences.size()));
        String targetSentence = (String) selectedSentence.get("targetSentence");

        // Only track question history if feature is enabled
        if (enableNoRepeatQuestions) {
            questionHistoryService.addQuestion(playerId, "SENTENCE", targetSentence);
        }
        String meaning = (String) selectedSentence.get("meaning");
        String pinyin = (String) selectedSentence.get("pinyin");
        List<String> words = (List<String>) selectedSentence.get("words");
        Map<String, String> hints = (Map<String, String>) selectedSentence.get("hints");

        // Scramble words
        List<String> scrambledWords = scrambleWords(words);

        // Create game session
        Map<String, Object> sessionData = Map.of(
            "targetSentence", targetSentence,
            "scrambledWords", scrambledWords,
            "allowedWords", words,
            "difficulty", difficulty.name(),
            "timeLimit", difficulty.getTimeLimitSeconds(),
            "hints", hints,
            "startedAt", LocalDateTime.now().toString()
        );

        String sessionDataJson = gameSessionService.sessionDataToJson(sessionData);
        gameSessionService.createSession(playerId, GameType.SENTENCE, difficulty, sessionDataJson);

        log.info("Sentence game started: target length={} words", words.size());

        return new SentenceGameState(
            scrambledWords,
            words.size(),
            difficulty.getTimeLimitSeconds(),
            difficulty,
            meaning,
            pinyin
        );
    }

    /**
     * Submits answer and calculates score
     *
     * @param playerId player ID
     * @param playerSentence player's crafted sentence
     * @param timeTaken time taken in seconds
     * @param hintsUsed number of hints used (0-3)
     * @return game result with score
     */
    public SentenceGameResult submitAnswer(
        Long playerId,
        String playerSentence,
        Integer timeTaken,
        Integer hintsUsed
    ) {
        log.info("Submitting sentence answer: player={}, answer={}, time={}s, hints={}",
            playerId, playerSentence, timeTaken, hintsUsed);

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        // Get active session
        var sessionOpt = gameSessionService.getActiveSession(playerId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalStateException("No active game session found for player: " + playerId);
        }

        var session = sessionOpt.get();
        Map<String, Object> sessionData = gameSessionService.parseSessionData(session.getSessionData());

        String targetSentence = (String) sessionData.get("targetSentence");
        List<String> allowedWords = (List<String>) sessionData.get("allowedWords");
        DifficultyLevel difficulty = DifficultyLevel.valueOf((String) sessionData.get("difficulty"));

        // Validate answer
        var validationResult = validationService.validateSentence(
            playerSentence, targetSentence, allowedWords
        );

        boolean isValid = validationResult.isValid();
        int grammarScore = validationResult.grammarScore();
        double similarityScore = validationResult.similarityScore();
        double accuracyRate = (grammarScore / 100.0 + similarityScore) / 2.0;

        // Calculate score
        int score = 0;
        if (isValid) {
            score = scoringService.calculateSentenceScore(
                difficulty, timeTaken, accuracyRate, grammarScore, hintsUsed
            );
        }

        // Save sentence score
        String validationErrorsJson = validationService.errorsToJson(validationResult.errors());

        SentenceScore sentenceScore = SentenceScore.builder()
            .player(player)
            .targetSentence(targetSentence)
            .playerSentence(playerSentence)
            .score(score)
            .difficulty(difficulty)
            .timeTaken(timeTaken)
            .hintsUsed(hintsUsed)
            .accuracyRate(accuracyRate)
            .grammarScore(grammarScore)
            .similarityScore(similarityScore)
            .validationErrors(validationErrorsJson)
            .completed(isValid)
            .build();

        sentenceScoreRepository.save(sentenceScore);

        // Complete session
        gameSessionService.completeSession(session.getId(), score);

        // Update leaderboard if valid
        if (isValid) {
            leaderboardService.updateLeaderboardAfterGame(
                playerId, GameType.SENTENCE, difficulty, score, accuracyRate
            );
        }

        // Check achievements
        List<Achievement> newAchievements = achievementService.checkAndUnlockAchievements(
            playerId, score, timeTaken, accuracyRate, hintsUsed
        );

        // Load sentence details for result display
        Map<String, Object> sentenceDetails = loadSentenceDetails(targetSentence);
        String translation = (String) sentenceDetails.getOrDefault("meaning", "");
        List<String> grammarPoints = (List<String>) sentenceDetails.getOrDefault("grammarPoints", List.of());
        String grammarExplanation = grammarPoints.isEmpty() ? "" : String.join(", ", grammarPoints);
        String pinyin = (String) sentenceDetails.getOrDefault("pinyin", "");

        log.info("Sentence answer submitted: valid={}, score={}, grammar={}, achievements={}",
            isValid, score, grammarScore, newAchievements.size());

        return new SentenceGameResult(
            isValid,
            targetSentence,
            playerSentence,
            score,
            accuracyRate,
            grammarScore,
            similarityScore,
            timeTaken,
            hintsUsed,
            validationResult.errors().stream()
                .map(e -> e.message())
                .collect(Collectors.toList()),
            newAchievements.stream()
                .map(a -> a.getTitle())
                .collect(Collectors.toList()),
            translation,
            grammarExplanation,
            pinyin
        );
    }

    /**
     * Gets a hint for current game
     *
     * @param playerId player ID
     * @param hintLevel hint level (1-3)
     * @return hint content
     */
    public SentenceHint getHint(Long playerId, Integer hintLevel) {
        log.info("Getting hint: player={}, level={}", playerId, hintLevel);

        // Validate hint level
        if (hintLevel < 1 || hintLevel > 3) {
            throw new IllegalArgumentException("Invalid hint level: " + hintLevel);
        }

        // Get active session
        var sessionOpt = gameSessionService.getActiveSession(playerId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalStateException("No active game session found");
        }

        var session = sessionOpt.get();
        int currentHintCount = gameSessionService.getHintCount(session.getId());

        if (currentHintCount >= 3) {
            throw new IllegalStateException("Maximum hints (3) already used");
        }

        Map<String, Object> sessionData = gameSessionService.parseSessionData(session.getSessionData());
        String targetSentence = (String) sessionData.get("targetSentence");

        // Load sentence details for rich hints
        Map<String, Object> sentenceDetails = loadSentenceDetails(targetSentence);

        // Generate enhanced hint content
        String hintContent = generateEnhancedHintContent(hintLevel, sentenceDetails, targetSentence);
        int penalty = scoringService.getHintPenalty(hintLevel);

        // Record hint usage
        gameSessionService.addHintUsage(session.getId(), hintLevel, penalty, hintContent);

        log.info("Hint provided: level={}, penalty={}", hintLevel, penalty);

        return new SentenceHint(hintLevel, hintContent, penalty);
    }

    /**
     * Gets player's sentence game history
     *
     * @param playerId player ID
     * @param limit maximum results
     * @return list of recent scores
     */
    @Transactional(readOnly = true)
    public List<SentenceScore> getPlayerHistory(Long playerId, int limit) {
        return sentenceScoreRepository.findRecentScores(playerId);
    }

    /**
     * Gets player's personal best for difficulty
     *
     * @param playerId player ID
     * @param difficulty difficulty level
     * @return Optional containing personal best
     */
    @Transactional(readOnly = true)
    public Optional<SentenceScore> getPersonalBest(Long playerId, DifficultyLevel difficulty) {
        return sentenceScoreRepository.findPersonalBest(playerId, difficulty);
    }

    /**
     * Restarts quiz by clearing question history
     * This allows player to replay all questions from the beginning
     *
     * @param playerId player ID
     */
    public void restartQuiz(Long playerId) {
        log.info("Restarting sentence quiz for player: {}", playerId);
        questionHistoryService.clearHistory(playerId, "SENTENCE");
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    /**
     * Scrambles words using Fisher-Yates shuffle
     * Ensures result is different from original
     */
    private List<String> scrambleWords(List<String> original) {
        List<String> scrambled = new ArrayList<>(original);
        int attempts = 0;
        final int maxAttempts = 3;

        do {
            // Fisher-Yates shuffle
            for (int i = scrambled.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                String temp = scrambled.get(i);
                scrambled.set(i, scrambled.get(j));
                scrambled.set(j, temp);
            }
            attempts++;
        } while (scrambled.equals(original) && attempts < maxAttempts);

        return scrambled;
    }

    /**
     * Loads sentence details from configuration file
     */
    private Map<String, Object> loadSentenceDetails(String targetSentence) {
        try {
            Map<String, Object> config = configurationService.loadConfigurationAsMap("sentences.json");
            List<Map<String, Object>> sentences = (List<Map<String, Object>>) config.get("sentences");

            return sentences.stream()
                .filter(s -> targetSentence.equals(s.get("targetSentence")))
                .findFirst()
                .orElse(Map.of());
        } catch (Exception e) {
            log.warn("Failed to load sentence details for: {}", targetSentence, e);
            return Map.of();
        }
    }

    /**
     * Generates enhanced hint content based on level
     * Level 1: Show translation and grammar explanation (educational, least penalty)
     * Level 2: Reveal first word and sentence structure (medium penalty)
     * Level 3: Show first half of the sentence (highest penalty)
     */
    private String generateEnhancedHintContent(int hintLevel, Map<String, Object> sentenceDetails, String targetSentence) {
        Map<String, String> hints = (Map<String, String>) sentenceDetails.getOrDefault("hints", Map.of());
        String meaning = (String) sentenceDetails.getOrDefault("meaning", "");
        List<String> grammarPoints = (List<String>) sentenceDetails.getOrDefault("grammarPoints", List.of());
        List<String> words = (List<String>) sentenceDetails.getOrDefault("words", List.of());

        return switch (hintLevel) {
            case 1 -> {
                // Level 1: Educational hint - meaning and grammar
                StringBuilder hint = new StringBuilder();
                if (!meaning.isEmpty()) {
                    hint.append("💡 句子含义：").append(meaning);
                }
                if (!grammarPoints.isEmpty()) {
                    hint.append("\n📚 语法要点：").append(String.join(", ", grammarPoints));
                }
                if (hint.length() == 0) {
                    hint.append("💡 提示：这是一个").append(words.size()).append("个词的句子");
                }
                yield hint.toString();
            }
            case 2 -> {
                // Level 2: First word and structure
                String baseHint = hints.getOrDefault("level2", "");
                if (!baseHint.isEmpty()) {
                    yield "🔤 " + baseHint;
                }
                if (!words.isEmpty()) {
                    yield "🔤 第一个词：" + words.get(0);
                }
                yield "🔤 提示：句子以主语开头";
            }
            case 3 -> {
                // Level 3: First half or substantial part of sentence
                String baseHint = hints.getOrDefault("level3", "");
                if (!baseHint.isEmpty()) {
                    yield "📝 " + baseHint;
                }
                // Fallback: show first half of words
                int halfSize = Math.max(1, words.size() / 2);
                if (words.size() >= 2) {
                    List<String> firstHalf = words.subList(0, halfSize);
                    yield "📝 前半部分：" + String.join("", firstHalf);
                }
                yield "📝 提示：第一个词是 " + (words.isEmpty() ? "..." : words.get(0));
            }
            default -> throw new IllegalArgumentException("Invalid hint level: " + hintLevel);
        };
    }

    // ========================================================================
    // DTOs
    // ========================================================================

    /**
     * Game state with scrambled words
     */
    public record SentenceGameState(
        List<String> scrambledWords,
        int wordCount,
        int timeLimitSeconds,
        DifficultyLevel difficulty,
        String meaning,
        String pinyin
    ) {
    }

    /**
     * Game result after submission
     */
    public record SentenceGameResult(
        boolean isValid,
        String targetSentence,
        String playerSentence,
        int score,
        double accuracyRate,
        int grammarScore,
        double similarityScore,
        int timeTaken,
        int hintsUsed,
        List<String> errors,
        List<String> newAchievements,
        String translation,
        String grammarExplanation,
        String pinyin
    ) {
        public boolean isPerfectScore() {
            return isValid && grammarScore == 100 && hintsUsed == 0;
        }
    }

    /**
     * Hint information
     */
    public record SentenceHint(
        int level,
        String content,
        int penalty
    ) {
    }
}