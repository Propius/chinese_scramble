package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.Achievement;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.repository.AchievementRepository;
import com.govtech.chinesescramble.repository.IdiomScoreRepository;
import com.govtech.chinesescramble.repository.LeaderboardRepository;
import com.govtech.chinesescramble.repository.PlayerRepository;
import com.govtech.chinesescramble.repository.SentenceScoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AchievementService - Manages player achievements and milestones
 *
 * Achievement Types:
 * 1. FIRST_WIN: Complete first game successfully
 * 2. SPEED_DEMON: Complete game in under 30 seconds
 * 3. PERFECT_SCORE: 100% accuracy with no hints
 * 4. HUNDRED_GAMES: Play 100 games total
 * 5. IDIOM_MASTER: Reach #1 on any idiom leaderboard
 * 6. SENTENCE_MASTER: Reach #1 on any sentence leaderboard
 * 7. TOP_RANKED: Reach top 10 on any leaderboard
 * 8. CONSISTENCY: Play every day for 7 consecutive days
 * 9. HIGH_SCORER: Achieve 1000+ points in a single game
 * 10. HINT_FREE: Complete 10 games without using hints
 *
 * Features:
 * - Automatic achievement checking after each game
 * - Prevent duplicate unlocks
 * - Metadata storage (scores, timestamps, etc.)
 * - Achievement rarity calculation
 * - Chinese achievement titles and descriptions
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final PlayerRepository playerRepository;
    private final IdiomScoreRepository idiomScoreRepository;
    private final SentenceScoreRepository sentenceScoreRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final ObjectMapper objectMapper;

    /**
     * Checks and unlocks achievements after a game
     *
     * @param playerId player ID
     * @param score score achieved
     * @param timeTaken time in seconds
     * @param accuracyRate accuracy (0.0-1.0)
     * @param hintsUsed hints used (0-3)
     * @return list of newly unlocked achievements
     */
    public List<Achievement> checkAndUnlockAchievements(
        Long playerId,
        Integer score,
        Integer timeTaken,
        Double accuracyRate,
        Integer hintsUsed
    ) {
        log.info("Checking achievements for player: {}", playerId);

        List<Achievement> newlyUnlocked = new java.util.ArrayList<>();

        // Check each achievement type
        newlyUnlocked.addAll(checkFirstWin(playerId));
        newlyUnlocked.addAll(checkSpeedDemon(playerId, timeTaken));
        newlyUnlocked.addAll(checkPerfectScore(playerId, accuracyRate, hintsUsed, score));
        newlyUnlocked.addAll(checkHundredGames(playerId));
        newlyUnlocked.addAll(checkTopRanked(playerId));
        newlyUnlocked.addAll(checkHighScorer(playerId, score));
        newlyUnlocked.addAll(checkHintFree(playerId));

        if (!newlyUnlocked.isEmpty()) {
            log.info("Player {} unlocked {} new achievements", playerId, newlyUnlocked.size());
        }

        return newlyUnlocked;
    }

    /**
     * Checks FIRST_WIN achievement
     */
    private List<Achievement> checkFirstWin(Long playerId) {
        if (hasAchievement(playerId, "FIRST_WIN")) {
            return List.of();
        }

        // Check if this is the first completed game
        long idiomCount = idiomScoreRepository.countCompletedGamesByPlayer(playerId);
        long sentenceCount = sentenceScoreRepository.countCompletedGamesByPlayer(playerId);

        if (idiomCount + sentenceCount == 1) {
            return List.of(unlockAchievement(
                playerId,
                "FIRST_WIN",
                "首次胜利",
                "完成第一个游戏",
                Map.of("totalGames", 1)
            ));
        }

        return List.of();
    }

    /**
     * Checks SPEED_DEMON achievement
     */
    private List<Achievement> checkSpeedDemon(Long playerId, Integer timeTaken) {
        if (hasAchievement(playerId, "SPEED_DEMON")) {
            return List.of();
        }

        if (timeTaken < 30) {
            return List.of(unlockAchievement(
                playerId,
                "SPEED_DEMON",
                "速度之王",
                "在30秒内完成一个游戏",
                Map.of("time", timeTaken)
            ));
        }

        return List.of();
    }

    /**
     * Checks PERFECT_SCORE achievement
     */
    private List<Achievement> checkPerfectScore(
        Long playerId,
        Double accuracyRate,
        Integer hintsUsed,
        Integer score
    ) {
        if (hasAchievement(playerId, "PERFECT_SCORE")) {
            return List.of();
        }

        if (accuracyRate >= 1.0 && hintsUsed == 0) {
            return List.of(unlockAchievement(
                playerId,
                "PERFECT_SCORE",
                "完美主义者",
                "100%准确率且不使用提示完成游戏",
                Map.of("score", score, "accuracy", accuracyRate)
            ));
        }

        return List.of();
    }

    /**
     * Checks HUNDRED_GAMES achievement
     */
    private List<Achievement> checkHundredGames(Long playerId) {
        if (hasAchievement(playerId, "HUNDRED_GAMES")) {
            return List.of();
        }

        long idiomCount = idiomScoreRepository.countCompletedGamesByPlayer(playerId);
        long sentenceCount = sentenceScoreRepository.countCompletedGamesByPlayer(playerId);
        long totalGames = idiomCount + sentenceCount;

        if (totalGames >= 100) {
            return List.of(unlockAchievement(
                playerId,
                "HUNDRED_GAMES",
                "百场老将",
                "完成100个游戏",
                Map.of("totalGames", totalGames)
            ));
        }

        return List.of();
    }

    /**
     * Checks TOP_RANKED achievement
     */
    private List<Achievement> checkTopRanked(Long playerId) {
        if (hasAchievement(playerId, "TOP_RANKED")) {
            return List.of();
        }

        List<com.govtech.chinesescramble.entity.Leaderboard> topTenEntries =
            leaderboardRepository.findTopTenEntriesByPlayer(playerId);

        if (!topTenEntries.isEmpty()) {
            return List.of(unlockAchievement(
                playerId,
                "TOP_RANKED",
                "排行榜之星",
                "进入任何排行榜前10名",
                Map.of("bestRank", topTenEntries.get(0).getRank())
            ));
        }

        return List.of();
    }

    /**
     * Checks IDIOM_MASTER achievement
     */
    private List<Achievement> checkIdiomMaster(Long playerId) {
        if (hasAchievement(playerId, "IDIOM_MASTER")) {
            return List.of();
        }

        List<com.govtech.chinesescramble.entity.Leaderboard> firstPlaceEntries =
            leaderboardRepository.findFirstPlaceEntriesByPlayer(playerId);

        boolean hasIdiomFirstPlace = firstPlaceEntries.stream()
            .anyMatch(entry -> entry.getGameType() == com.govtech.chinesescramble.entity.enums.GameType.IDIOM);

        if (hasIdiomFirstPlace) {
            return List.of(unlockAchievement(
                playerId,
                "IDIOM_MASTER",
                "成语大师",
                "在任何成语排行榜上获得第一名",
                Map.of("achievement", "idiom_master")
            ));
        }

        return List.of();
    }

    /**
     * Checks SENTENCE_MASTER achievement
     */
    private List<Achievement> checkSentenceMaster(Long playerId) {
        if (hasAchievement(playerId, "SENTENCE_MASTER")) {
            return List.of();
        }

        List<com.govtech.chinesescramble.entity.Leaderboard> firstPlaceEntries =
            leaderboardRepository.findFirstPlaceEntriesByPlayer(playerId);

        boolean hasSentenceFirstPlace = firstPlaceEntries.stream()
            .anyMatch(entry -> entry.getGameType() == com.govtech.chinesescramble.entity.enums.GameType.SENTENCE);

        if (hasSentenceFirstPlace) {
            return List.of(unlockAchievement(
                playerId,
                "SENTENCE_MASTER",
                "造句大师",
                "在任何造句排行榜上获得第一名",
                Map.of("achievement", "sentence_master")
            ));
        }

        return List.of();
    }

    /**
     * Checks HIGH_SCORER achievement
     */
    private List<Achievement> checkHighScorer(Long playerId, Integer score) {
        if (hasAchievement(playerId, "HIGH_SCORER")) {
            return List.of();
        }

        if (score >= 1000) {
            return List.of(unlockAchievement(
                playerId,
                "HIGH_SCORER",
                "高分达人",
                "单局获得1000分以上",
                Map.of("score", score)
            ));
        }

        return List.of();
    }

    /**
     * Checks HINT_FREE achievement
     */
    private List<Achievement> checkHintFree(Long playerId) {
        if (hasAchievement(playerId, "HINT_FREE")) {
            return List.of();
        }

        long idiomHintFree = idiomScoreRepository.countHintFreeGamesByPlayer(playerId);
        long sentenceHintFree = sentenceScoreRepository.countHintFreeGamesByPlayer(playerId);
        long totalHintFree = idiomHintFree + sentenceHintFree;

        if (totalHintFree >= 10) {
            return List.of(unlockAchievement(
                playerId,
                "HINT_FREE",
                "独立思考者",
                "完成10个游戏且不使用任何提示",
                Map.of("hintFreeGames", totalHintFree)
            ));
        }

        return List.of();
    }

    /**
     * Unlocks an achievement for a player
     *
     * @param playerId player ID
     * @param achievementType achievement type
     * @param title Chinese title
     * @param description Chinese description
     * @param metadata additional data
     * @return unlocked achievement
     */
    private Achievement unlockAchievement(
        Long playerId,
        String achievementType,
        String title,
        String description,
        Map<String, Object> metadata
    ) {
        log.info("Unlocking achievement {} for player {}", achievementType, playerId);

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        String metadataJson = convertToJson(metadata);

        Achievement achievement = Achievement.builder()
            .player(player)
            .achievementType(achievementType)
            .title(title)
            .description(description)
            .unlockedAt(LocalDateTime.now())
            .metadata(metadataJson)
            .build();

        return achievementRepository.save(achievement);
    }

    /**
     * Checks if player has an achievement
     *
     * @param playerId player ID
     * @param achievementType achievement type
     * @return true if already unlocked
     */
    @Transactional(readOnly = true)
    public boolean hasAchievement(Long playerId, String achievementType) {
        return achievementRepository.existsByPlayerIdAndAchievementType(playerId, achievementType);
    }

    /**
     * Gets all achievements for a player
     *
     * @param playerId player ID
     * @return list of achievements
     */
    @Transactional(readOnly = true)
    public List<Achievement> getPlayerAchievements(Long playerId) {
        return achievementRepository.findByPlayerId(playerId);
    }

    /**
     * Gets only unlocked achievements for a player
     * Filters out achievements that haven't been unlocked yet
     *
     * @param playerId player ID
     * @return list of unlocked achievements
     */
    @Transactional(readOnly = true)
    public List<Achievement> getUnlockedAchievements(Long playerId) {
        return achievementRepository.findByPlayerId(playerId).stream()
            .filter(achievement -> achievement.getUnlockedAt() != null)
            .toList();
    }

    /**
     * Gets recently unlocked achievements (within 24 hours)
     *
     * @param playerId player ID
     * @return list of recent achievements
     */
    @Transactional(readOnly = true)
    public List<Achievement> getRecentAchievements(Long playerId) {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return achievementRepository.findRecentAchievements(playerId, twentyFourHoursAgo);
    }

    /**
     * Gets achievement count for player
     *
     * @param playerId player ID
     * @return achievement count
     */
    @Transactional(readOnly = true)
    public long getAchievementCount(Long playerId) {
        return achievementRepository.countByPlayerId(playerId);
    }

    /**
     * Gets achievement rarity (% of players who have it)
     *
     * @param achievementType achievement type
     * @return rarity percentage (0-100)
     */
    @Transactional(readOnly = true)
    public double getAchievementRarity(String achievementType) {
        long totalPlayers = playerRepository.count();
        if (totalPlayers == 0) {
            return 0.0;
        }

        long playersWithAchievement = achievementRepository.countByAchievementType(achievementType);
        return (playersWithAchievement * 100.0) / totalPlayers;
    }

    /**
     * Gets achievement statistics
     *
     * @return statistics map
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getAchievementStatistics() {
        List<Object[]> distribution = achievementRepository.getAchievementDistribution();

        Map<String, Long> stats = new HashMap<>();
        for (Object[] row : distribution) {
            String achievementType = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            stats.put(achievementType, count);
        }

        return stats;
    }

    /**
     * Converts metadata map to JSON string
     */
    private String convertToJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.error("Failed to convert metadata to JSON", e);
            return "{}";
        }
    }
}