package com.govtech.chinesescramble.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * QuestionHistoryService - Prevents duplicate questions within a session
 *
 * Features:
 * - Tracks recently shown questions per player
 * - Maintains exclusion list of last N questions (default: 10)
 * - Thread-safe concurrent access
 * - Automatic cleanup when exclusion list reaches limit
 * - Separate tracking for idiom and sentence games
 *
 * Implementation:
 * - Uses in-memory ConcurrentHashMap for performance
 * - Circular buffer approach with LinkedList
 * - Can be extended to use Redis for distributed systems
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Service
@Slf4j
public class QuestionHistoryService {

    private static final int DEFAULT_EXCLUSION_SIZE = 10;

    // Key format: "playerId:gameType" (e.g., "1:IDIOM", "1:SENTENCE")
    private final Map<String, LinkedList<String>> questionHistory = new ConcurrentHashMap<>();

    /**
     * Adds a question to player's history
     *
     * @param playerId player ID
     * @param gameType game type (IDIOM/SENTENCE)
     * @param questionId question identifier (idiom text or sentence ID)
     */
    public void addQuestion(Long playerId, String gameType, String questionId) {
        String key = buildKey(playerId, gameType);

        questionHistory.compute(key, (k, history) -> {
            if (history == null) {
                history = new LinkedList<>();
            }

            // Add to end of list
            history.addLast(questionId);

            // Keep only last N questions
            while (history.size() > DEFAULT_EXCLUSION_SIZE) {
                String removed = history.removeFirst();
                log.debug("Removed old question from history: player={}, gameType={}, question={}",
                    playerId, gameType, removed);
            }

            return history;
        });

        log.debug("Added question to history: player={}, gameType={}, question={}, historySize={}",
            playerId, gameType, questionId, questionHistory.get(key).size());
    }

    /**
     * Checks if a question was recently shown to player
     *
     * @param playerId player ID
     * @param gameType game type
     * @param questionId question identifier
     * @return true if question is in recent history
     */
    public boolean wasRecentlyShown(Long playerId, String gameType, String questionId) {
        String key = buildKey(playerId, gameType);
        LinkedList<String> history = questionHistory.get(key);

        if (history == null) {
            return false;
        }

        boolean wasShown = history.contains(questionId);

        if (wasShown) {
            log.debug("Question was recently shown: player={}, gameType={}, question={}",
                playerId, gameType, questionId);
        }

        return wasShown;
    }

    /**
     * Gets list of excluded questions for player
     *
     * @param playerId player ID
     * @param gameType game type
     * @return set of recently shown question IDs
     */
    public Set<String> getExcludedQuestions(Long playerId, String gameType) {
        String key = buildKey(playerId, gameType);
        LinkedList<String> history = questionHistory.get(key);

        if (history == null) {
            return Collections.emptySet();
        }

        return new HashSet<>(history);
    }

    /**
     * Clears question history for a player
     *
     * @param playerId player ID
     * @param gameType game type (null to clear all game types)
     */
    public void clearHistory(Long playerId, String gameType) {
        if (gameType == null) {
            // Clear all game types for player
            questionHistory.keySet().removeIf(key -> key.startsWith(playerId + ":"));
            log.info("Cleared all question history for player: {}", playerId);
        } else {
            String key = buildKey(playerId, gameType);
            questionHistory.remove(key);
            log.info("Cleared question history: player={}, gameType={}", playerId, gameType);
        }
    }

    /**
     * Gets count of questions in history
     *
     * @param playerId player ID
     * @param gameType game type
     * @return number of questions in history
     */
    public int getHistorySize(Long playerId, String gameType) {
        String key = buildKey(playerId, gameType);
        LinkedList<String> history = questionHistory.get(key);
        return history != null ? history.size() : 0;
    }

    /**
     * Gets statistics about question history
     *
     * @return map of statistics
     */
    public Map<String, Object> getStatistics() {
        int totalPlayers = (int) questionHistory.keySet().stream()
            .map(key -> key.split(":")[0])
            .distinct()
            .count();

        int totalEntries = questionHistory.size();

        int totalQuestions = questionHistory.values().stream()
            .mapToInt(LinkedList::size)
            .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPlayers", totalPlayers);
        stats.put("totalEntries", totalEntries);
        stats.put("totalQuestionsTracked", totalQuestions);
        stats.put("exclusionSize", DEFAULT_EXCLUSION_SIZE);

        return stats;
    }

    /**
     * Builds cache key from player ID and game type
     */
    private String buildKey(Long playerId, String gameType) {
        return playerId + ":" + gameType;
    }

    /**
     * Clears all history (for testing or maintenance)
     */
    public void clearAll() {
        int size = questionHistory.size();
        questionHistory.clear();
        log.info("Cleared all question history: {} entries removed", size);
    }
}
