package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.Leaderboard;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.repository.IdiomScoreRepository;
import com.govtech.chinesescramble.repository.LeaderboardRepository;
import com.govtech.chinesescramble.repository.PlayerRepository;
import com.govtech.chinesescramble.repository.SentenceScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * LeaderboardService - Manages player rankings and leaderboards
 *
 * Features:
 * - Pre-calculated rankings for fast queries
 * - Automatic rank updates after each game
 * - Scheduled bulk recalculation (daily at 3 AM)
 * - Spring Cache integration (2-minute TTL)
 * - Top players retrieval
 * - Player position lookup
 *
 * Leaderboard Types:
 * - IDIOM + EASY/MEDIUM/HARD/EXPERT: Idiom game rankings
 * - SENTENCE + EASY/MEDIUM/HARD/EXPERT: Sentence game rankings
 * - COMBINED: Overall rankings across all games
 *
 * Ranking Algorithm:
 * 1. Sum total score for each player (game type + difficulty)
 * 2. Sort by total score descending
 * 3. Calculate average score per game
 * 4. Calculate average accuracy rate
 * 5. Assign rank (1 = highest)
 * 6. Handle ties: Same total score = same rank, ordered by average score
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final PlayerRepository playerRepository;
    private final IdiomScoreRepository idiomScoreRepository;
    private final SentenceScoreRepository sentenceScoreRepository;

    /**
     * Updates leaderboard after a game is completed
     *
     * @param playerId player ID
     * @param gameType game type
     * @param difficulty difficulty level
     * @param score score achieved
     * @param accuracyRate accuracy rate (0.0-1.0)
     */
    @CacheEvict(value = "leaderboards", allEntries = true)
    public void updateLeaderboardAfterGame(
        Long playerId,
        GameType gameType,
        DifficultyLevel difficulty,
        Integer score,
        Double accuracyRate
    ) {
        log.info("Updating leaderboard: player={}, gameType={}, difficulty={}, score={}",
            playerId, gameType, difficulty, score);

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        // Find or create leaderboard entry
        Optional<Leaderboard> existingEntry = leaderboardRepository
            .findByPlayerIdAndGameTypeAndDifficulty(playerId, gameType, difficulty);

        if (existingEntry.isPresent()) {
            // Update existing entry
            Leaderboard entry = existingEntry.get();
            entry.updateWithNewGame(score, accuracyRate);
            leaderboardRepository.save(entry);
            log.debug("Updated existing leaderboard entry: rank={}, totalScore={}, games={}",
                entry.getRank(), entry.getTotalScore(), entry.getGamesPlayed());
        } else {
            // Create new entry
            Leaderboard newEntry = Leaderboard.builder()
                .player(player)
                .gameType(gameType)
                .difficulty(difficulty)
                .totalScore(score)
                .gamesPlayed(1)
                .averageScore(score.doubleValue())
                .rank(1) // Temporary rank, will be recalculated
                .accuracyRate(accuracyRate)
                .lastUpdated(LocalDateTime.now())
                .build();
            leaderboardRepository.save(newEntry);
            log.debug("Created new leaderboard entry: totalScore={}", score);
        }

        // Recalculate ranks for this game type and difficulty
        recalculateRanks(gameType, difficulty);
    }

    /**
     * Recalculates ranks for a specific game type and difficulty
     *
     * @param gameType game type
     * @param difficulty difficulty level
     */
    @CacheEvict(value = "leaderboards", allEntries = true)
    public void recalculateRanks(GameType gameType, DifficultyLevel difficulty) {
        log.info("Recalculating ranks: gameType={}, difficulty={}", gameType, difficulty);

        // Get all entries sorted by total score DESC
        List<Leaderboard> entries = leaderboardRepository
            .findAllForRankRecalculation(gameType, difficulty);

        if (entries.isEmpty()) {
            log.debug("No entries to rank for gameType={}, difficulty={}", gameType, difficulty);
            return;
        }

        // Assign ranks
        int currentRank = 1;
        Integer previousScore = null;

        for (int i = 0; i < entries.size(); i++) {
            Leaderboard entry = entries.get(i);

            // If score is same as previous, keep same rank (ties)
            if (previousScore != null && !entry.getTotalScore().equals(previousScore)) {
                currentRank = i + 1;
            }

            entry.setRank(currentRank);
            entry.setLastUpdated(LocalDateTime.now());
            previousScore = entry.getTotalScore();
        }

        leaderboardRepository.saveAll(entries);
        log.info("Ranks recalculated: {} entries updated", entries.size());
    }

    /**
     * Gets top N players for a game type and difficulty
     * Uses Spring Cache (2-minute TTL)
     *
     * @param gameType game type
     * @param difficulty difficulty level
     * @param limit maximum results
     * @return list of top leaderboard entries
     */
    @Cacheable(value = "leaderboards", key = "#gameType + '_' + #difficulty + '_' + #limit")
    @Transactional(readOnly = true)
    public List<Leaderboard> getTopPlayers(
        GameType gameType,
        DifficultyLevel difficulty,
        int limit
    ) {
        log.debug("Getting top {} players: gameType={}, difficulty={}", limit, gameType, difficulty);
        return leaderboardRepository.findTopPlayersByGameTypeAndDifficulty(gameType, difficulty, limit);
    }

    /**
     * Gets player's position on a specific leaderboard
     *
     * @param playerId player ID
     * @param gameType game type
     * @param difficulty difficulty level
     * @return Optional containing leaderboard entry
     */
    @Transactional(readOnly = true)
    public Optional<Leaderboard> getPlayerPosition(
        Long playerId,
        GameType gameType,
        DifficultyLevel difficulty
    ) {
        log.debug("Getting player position: player={}, gameType={}, difficulty={}",
            playerId, gameType, difficulty);
        return leaderboardRepository.findByPlayerIdAndGameTypeAndDifficulty(
            playerId, gameType, difficulty
        );
    }

    /**
     * Gets all leaderboard entries for a player
     *
     * @param playerId player ID
     * @return list of player's leaderboard entries
     */
    @Transactional(readOnly = true)
    public List<Leaderboard> getPlayerLeaderboards(Long playerId) {
        log.debug("Getting all leaderboards for player: {}", playerId);
        return leaderboardRepository.findByPlayerId(playerId);
    }

    /**
     * Gets all rankings for a player (alias for getPlayerLeaderboards)
     *
     * @param playerId player ID
     * @return list of player's leaderboard entries
     */
    @Transactional(readOnly = true)
    public List<Leaderboard> getPlayerRankings(Long playerId) {
        return getPlayerLeaderboards(playerId);
    }

    /**
     * Gets player's rank for a specific leaderboard (alias for getPlayerPosition)
     *
     * @param playerId player ID
     * @param gameType game type
     * @param difficulty difficulty level
     * @return Optional containing leaderboard entry
     */
    @Transactional(readOnly = true)
    public Optional<Leaderboard> getPlayerRank(
        Long playerId,
        GameType gameType,
        DifficultyLevel difficulty
    ) {
        return getPlayerPosition(playerId, gameType, difficulty);
    }

    /**
     * Gets players near a specific rank (contextual leaderboard)
     *
     * @param gameType game type
     * @param difficulty difficulty level
     * @param rank center rank
     * @param offset offset (±N positions)
     * @return list of leaderboard entries near rank
     */
    @Transactional(readOnly = true)
    public List<Leaderboard> getPlayersNearRank(
        GameType gameType,
        DifficultyLevel difficulty,
        Integer rank,
        Integer offset
    ) {
        log.debug("Getting players near rank {}: gameType={}, difficulty={}, offset={}",
            rank, gameType, difficulty, offset);
        return leaderboardRepository.findPlayersNearRank(gameType, difficulty, rank, offset);
    }

    /**
     * Checks if player is in top 10
     *
     * @param playerId player ID
     * @param gameType game type
     * @param difficulty difficulty level
     * @return true if player is in top 10
     */
    @Transactional(readOnly = true)
    public boolean isPlayerInTopTen(Long playerId, GameType gameType, DifficultyLevel difficulty) {
        Optional<Leaderboard> entry = getPlayerPosition(playerId, gameType, difficulty);
        return entry.map(Leaderboard::isTopTen).orElse(false);
    }

    /**
     * Checks if player is ranked #1
     *
     * @param playerId player ID
     * @param gameType game type
     * @param difficulty difficulty level
     * @return true if player is #1
     */
    @Transactional(readOnly = true)
    public boolean isPlayerFirstPlace(Long playerId, GameType gameType, DifficultyLevel difficulty) {
        Optional<Leaderboard> entry = getPlayerPosition(playerId, gameType, difficulty);
        return entry.map(Leaderboard::isFirstPlace).orElse(false);
    }

    /**
     * Gets total number of ranked players
     *
     * @param gameType game type
     * @param difficulty difficulty level
     * @return count of ranked players
     */
    @Transactional(readOnly = true)
    public long getTotalRankedPlayers(GameType gameType, DifficultyLevel difficulty) {
        return leaderboardRepository.countByGameTypeAndDifficulty(gameType, difficulty);
    }

    /**
     * Scheduled task to recalculate all leaderboards
     * Runs daily at 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * *") // 3:00 AM daily
    @CacheEvict(value = "leaderboards", allEntries = true)
    public void scheduledLeaderboardRecalculation() {
        log.info("Starting scheduled leaderboard recalculation");

        int totalRecalculated = 0;

        // Recalculate for all game types and difficulties
        for (GameType gameType : GameType.values()) {
            if (gameType == GameType.COMBINED) continue; // Skip combined for now

            for (DifficultyLevel difficulty : DifficultyLevel.values()) {
                try {
                    recalculateRanks(gameType, difficulty);
                    totalRecalculated++;
                } catch (Exception e) {
                    log.error("Failed to recalculate ranks: gameType={}, difficulty={}",
                        gameType, difficulty, e);
                }
            }
        }

        log.info("Scheduled leaderboard recalculation completed: {} leaderboards updated",
            totalRecalculated);
    }

    /**
     * Forces immediate recalculation of all leaderboards
     * Admin function for manual refresh
     */
    @CacheEvict(value = "leaderboards", allEntries = true)
    public void forceRecalculateAll() {
        log.warn("Forcing immediate recalculation of all leaderboards");
        scheduledLeaderboardRecalculation();
    }

    /**
     * Gets leaderboard statistics
     *
     * @param gameType game type
     * @param difficulty difficulty level
     * @return statistics
     */
    @Transactional(readOnly = true)
    public LeaderboardStatistics getStatistics(GameType gameType, DifficultyLevel difficulty) {
        Optional<Object[]> stats = leaderboardRepository.getLeaderboardStatistics(gameType, difficulty);

        if (stats.isEmpty()) {
            return new LeaderboardStatistics(0L, 0.0, 0.0);
        }

        Object[] data = stats.get();
        return new LeaderboardStatistics(
            ((Number) data[0]).longValue(),
            data[1] != null ? ((Number) data[1]).doubleValue() : 0.0,
            data[2] != null ? ((Number) data[2]).doubleValue() : 0.0
        );
    }

    // ========================================================================
    // DTOs
    // ========================================================================

    /**
     * Leaderboard statistics
     */
    public record LeaderboardStatistics(
        long totalPlayers,
        double averageScore,
        double averageAccuracy
    ) {
        public double averageAccuracyPercentage() {
            return averageAccuracy * 100.0;
        }
    }
}