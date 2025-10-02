package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.Achievement;
import com.govtech.chinesescramble.entity.IdiomScore;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.exception.AllQuestionsCompletedException;
import com.govtech.chinesescramble.repository.IdiomScoreRepository;
import com.govtech.chinesescramble.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * IdiomGameService - Manages Chinese idiom scramble game logic
 *
 * Game Flow:
 * 1. Player selects difficulty level
 * 2. System selects random idiom from configuration
 * 3. Characters are scrambled randomly
 * 4. Player arranges characters in correct order
 * 5. Answer is validated (exact match)
 * 6. Score is calculated based on time, accuracy, hints used
 * 7. Achievements and leaderboard are updated
 *
 * Scrambling Algorithm:
 * - Fisher-Yates shuffle for random character order
 * - Ensures scrambled order is different from original
 * - Maximum 3 attempts to find different arrangement
 *
 * Difficulty Levels:
 * - EASY: 180 seconds, 100 base points, 1.0x multiplier
 * - MEDIUM: 120 seconds, 200 base points, 1.2x multiplier
 * - HARD: 90 seconds, 300 base points, 1.5x multiplier
 * - EXPERT: 60 seconds, 500 base points, 2.0x multiplier
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class IdiomGameService {

    private final PlayerRepository playerRepository;
    private final IdiomScoreRepository idiomScoreRepository;
    private final ConfigurationService configurationService;
    private final ValidationService validationService;
    private final ScoringService scoringService;
    private final AchievementService achievementService;
    private final LeaderboardService leaderboardService;
    private final GameSessionService gameSessionService;
    private final QuestionHistoryService questionHistoryService;

    private final Random random = new Random();

    /**
     * Starts a new idiom game
     *
     * @param playerId player ID
     * @param difficulty difficulty level
     * @return game state with scrambled characters
     */
    public IdiomGameState startGame(Long playerId, DifficultyLevel difficulty) {
        log.info("Starting idiom game: player={}, difficulty={}", playerId, difficulty);

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        // Load idioms configuration
        Map<String, Object> config = configurationService.loadConfigurationAsMap("idioms.json");
        List<Map<String, Object>> idioms = (List<Map<String, Object>>) config.get("idioms");

        // Filter by difficulty
        List<Map<String, Object>> difficultyIdioms = idioms.stream()
            .filter(idiom -> difficulty.name().equals(idiom.get("difficulty")))
            .collect(Collectors.toList());

        if (difficultyIdioms.isEmpty()) {
            throw new IllegalStateException("No idioms found for difficulty: " + difficulty);
        }

        // Get excluded questions (recently shown)
        Set<String> excludedIdioms = questionHistoryService.getExcludedQuestions(playerId, "IDIOM");

        // Filter out recently shown idioms
        List<Map<String, Object>> availableIdioms = difficultyIdioms.stream()
            .filter(idiom -> !excludedIdioms.contains((String) idiom.get("idiom")))
            .collect(Collectors.toList());

        // Check if all questions have been completed
        boolean allQuestionsCompleted = availableIdioms.isEmpty();
        if (allQuestionsCompleted) {
            log.info("All idioms completed for player {}, difficulty={}, total={}",
                playerId, difficulty, difficultyIdioms.size());
            // Note: We do NOT clear history here - that should only happen on explicit restart
            // For now, throw exception to signal completion to frontend
            throw new AllQuestionsCompletedException(
                String.format("恭喜！您已完成所有 %s 难度的成语题目！", difficulty.getLabel())
            );
        }

        // Select random idiom from available pool
        Map<String, Object> selectedIdiom = availableIdioms.get(random.nextInt(availableIdioms.size()));
        String idiom = (String) selectedIdiom.get("idiom");

        // Add to question history
        questionHistoryService.addQuestion(playerId, "IDIOM", idiom);
        String definition = (String) selectedIdiom.get("definition");
        String pinyin = (String) selectedIdiom.get("pinyin");

        // Scramble characters
        List<String> characters = Arrays.stream(idiom.split(""))
            .collect(Collectors.toList());
        List<String> scrambledChars = scrambleCharacters(characters);

        // Create game session
        Map<String, Object> sessionData = Map.of(
            "idiom", idiom,
            "scrambled", scrambledChars,
            "difficulty", difficulty.name(),
            "timeLimit", difficulty.getTimeLimitSeconds(),
            "startedAt", LocalDateTime.now().toString()
        );

        String sessionDataJson = gameSessionService.sessionDataToJson(sessionData);
        gameSessionService.createSession(playerId, GameType.IDIOM, difficulty, sessionDataJson);

        log.info("Idiom game started: idiom={} (scrambled)", idiom);

        return new IdiomGameState(
            scrambledChars,
            characters.size(),
            difficulty.getTimeLimitSeconds(),
            difficulty,
            definition,
            pinyin
        );
    }

    /**
     * Submits answer and calculates score
     *
     * @param playerId player ID
     * @param playerAnswer player's arranged answer
     * @param timeTaken time taken in seconds
     * @param hintsUsed number of hints used (0-3)
     * @return game result with score
     */
    public IdiomGameResult submitAnswer(
        Long playerId,
        String playerAnswer,
        Integer timeTaken,
        Integer hintsUsed
    ) {
        log.info("Submitting idiom answer: player={}, answer={}, time={}s, hints={}",
            playerId, playerAnswer, timeTaken, hintsUsed);

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        // Get active session
        var sessionOpt = gameSessionService.getActiveSession(playerId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalStateException("No active game session found for player: " + playerId);
        }

        var session = sessionOpt.get();
        Map<String, Object> sessionData = gameSessionService.parseSessionData(session.getSessionData());

        String correctIdiom = (String) sessionData.get("idiom");
        DifficultyLevel difficulty = DifficultyLevel.valueOf((String) sessionData.get("difficulty"));

        // Validate answer
        var validationResult = validationService.validateIdiom(playerAnswer, correctIdiom);
        boolean isCorrect = validationResult.isCorrect();
        double accuracyRate = validationResult.accuracy();

        // Calculate score
        int score = 0;
        if (isCorrect) {
            score = scoringService.calculateIdiomScore(difficulty, timeTaken, accuracyRate, hintsUsed);
        }

        // Save idiom score
        IdiomScore idiomScore = IdiomScore.builder()
            .player(player)
            .idiom(correctIdiom)
            .score(score)
            .difficulty(difficulty)
            .timeTaken(timeTaken)
            .hintsUsed(hintsUsed)
            .accuracyRate(accuracyRate)
            .completed(isCorrect)
            .build();

        idiomScoreRepository.save(idiomScore);

        // Complete session
        gameSessionService.completeSession(session.getId(), score);

        // Update leaderboard if completed
        if (isCorrect) {
            leaderboardService.updateLeaderboardAfterGame(
                playerId, GameType.IDIOM, difficulty, score, accuracyRate
            );
        }

        // Check achievements
        List<Achievement> newAchievements = achievementService.checkAndUnlockAchievements(
            playerId, score, timeTaken, accuracyRate, hintsUsed
        );

        // Load idiom details for result display
        Map<String, Object> idiomDetails = loadIdiomDetails(correctIdiom);
        String definition = (String) idiomDetails.getOrDefault("definition", "");
        String meaning = (String) idiomDetails.getOrDefault("meaning", "");
        String usage = (String) idiomDetails.getOrDefault("usage", "");
        String pinyin = (String) idiomDetails.getOrDefault("pinyin", "");

        log.info("Idiom answer submitted: correct={}, score={}, achievements={}",
            isCorrect, score, newAchievements.size());

        return new IdiomGameResult(
            isCorrect,
            correctIdiom,
            playerAnswer,
            score,
            accuracyRate,
            timeTaken,
            hintsUsed,
            newAchievements.stream().map(a -> a.getTitle()).collect(Collectors.toList()),
            definition,
            meaning,
            usage,
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
    public IdiomHint getHint(Long playerId, Integer hintLevel) {
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
        String idiom = (String) sessionData.get("idiom");

        // Load idiom details from configuration for rich hints
        Map<String, Object> idiomDetails = loadIdiomDetails(idiom);

        // Generate hint content based on level
        String hintContent = generateHintContent(idiom, hintLevel, idiomDetails);
        int penalty = scoringService.getHintPenalty(hintLevel);

        // Record hint usage
        gameSessionService.addHintUsage(session.getId(), hintLevel, penalty, hintContent);

        log.info("Hint provided: level={}, penalty={}", hintLevel, penalty);

        return new IdiomHint(hintLevel, hintContent, penalty);
    }

    /**
     * Gets player's idiom game history
     *
     * @param playerId player ID
     * @param limit maximum results
     * @return list of recent scores
     */
    @Transactional(readOnly = true)
    public List<IdiomScore> getPlayerHistory(Long playerId, int limit) {
        return idiomScoreRepository.findRecentScores(playerId);
    }

    /**
     * Gets player's personal best for difficulty
     *
     * @param playerId player ID
     * @param difficulty difficulty level
     * @return Optional containing personal best
     */
    @Transactional(readOnly = true)
    public Optional<IdiomScore> getPersonalBest(Long playerId, DifficultyLevel difficulty) {
        return idiomScoreRepository.findPersonalBest(playerId, difficulty);
    }

    /**
     * Restarts quiz by clearing question history
     * This allows player to replay all questions from the beginning
     *
     * @param playerId player ID
     */
    public void restartQuiz(Long playerId) {
        log.info("Restarting idiom quiz for player: {}", playerId);
        questionHistoryService.clearHistory(playerId, "IDIOM");
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    /**
     * Scrambles characters using Fisher-Yates shuffle
     * Ensures result is different from original
     */
    private List<String> scrambleCharacters(List<String> original) {
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
     * Loads idiom details from configuration file
     */
    private Map<String, Object> loadIdiomDetails(String idiom) {
        try {
            Map<String, Object> config = configurationService.loadConfigurationAsMap("idioms.json");
            List<Map<String, Object>> idioms = (List<Map<String, Object>>) config.get("idioms");

            return idioms.stream()
                .filter(i -> idiom.equals(i.get("idiom")))
                .findFirst()
                .orElse(Map.of());
        } catch (Exception e) {
            log.warn("Failed to load idiom details for: {}", idiom, e);
            return Map.of();
        }
    }

    /**
     * Generates hint content based on level
     * Level 1: Show meaning/definition (least penalty)
     * Level 2: Reveal first character position (medium penalty)
     * Level 3: Show usage example (highest penalty)
     */
    private String generateHintContent(String idiom, int hintLevel, Map<String, Object> idiomDetails) {
        return switch (hintLevel) {
            case 1 -> {
                // Level 1: Show meaning/definition
                String definition = (String) idiomDetails.getOrDefault("definition", "");
                String meaning = (String) idiomDetails.getOrDefault("meaning", "");
                if (!definition.isEmpty()) {
                    yield "💡 含义提示：" + definition +
                          (!meaning.isEmpty() ? "\n📖 英文释义：" + meaning : "");
                }
                yield "💡 提示：这是一个常见成语";
            }
            case 2 -> {
                // Level 2: Reveal first character position
                String firstChar = String.valueOf(idiom.charAt(0));
                String pinyin = (String) idiomDetails.getOrDefault("pinyin", "");
                yield "🔤 第一个字：" + firstChar +
                      (!pinyin.isEmpty() ? "\n🗣️ 拼音：" + pinyin.split(" ")[0] : "");
            }
            case 3 -> {
                // Level 3: Show usage example and character count
                String usage = (String) idiomDetails.getOrDefault("usage", "");
                String origin = (String) idiomDetails.getOrDefault("origin", "");
                if (!usage.isEmpty()) {
                    yield "📝 用法示例：" + usage;
                }
                if (!origin.isEmpty()) {
                    yield "📚 出处提示：" + origin;
                }
                yield "💭 提示：共" + idiom.length() + "个字，思考一下它的用法场景";
            }
            default -> throw new IllegalArgumentException("Invalid hint level: " + hintLevel);
        };
    }

    // ========================================================================
    // DTOs
    // ========================================================================

    /**
     * Game state with scrambled characters
     */
    public record IdiomGameState(
        List<String> scrambledCharacters,
        int characterCount,
        int timeLimitSeconds,
        DifficultyLevel difficulty,
        String definition,
        String pinyin
    ) {
    }

    /**
     * Game result after submission
     */
    public record IdiomGameResult(
        boolean isCorrect,
        String correctIdiom,
        String playerAnswer,
        int score,
        double accuracyRate,
        int timeTaken,
        int hintsUsed,
        List<String> newAchievements,
        String definition,
        String meaning,
        String usage,
        String pinyin
    ) {
        public boolean isPerfectScore() {
            return isCorrect && accuracyRate == 1.0 && hintsUsed == 0;
        }
    }

    /**
     * Hint information
     */
    public record IdiomHint(
        int level,
        String content,
        int penalty
    ) {
    }
}