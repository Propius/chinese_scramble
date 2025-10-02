package com.govtech.chinesescramble.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QuestionHistoryService
 */
@DisplayName("QuestionHistoryService Tests")
class QuestionHistoryServiceTest {

    private QuestionHistoryService questionHistoryService;

    @BeforeEach
    void setUp() {
        questionHistoryService = new QuestionHistoryService();
        questionHistoryService.clearAll(); // Clear state before each test
    }

    @Test
    @DisplayName("Should add question to history")
    void testAddQuestion() {
        // Arrange
        Long playerId = 1L;
        String gameType = "IDIOM";
        String questionId = "井底之蛙";

        // Act
        questionHistoryService.addQuestion(playerId, gameType, questionId);

        // Assert
        Set<String> excluded = questionHistoryService.getExcludedQuestions(playerId, gameType);
        assertTrue(excluded.contains(questionId));
        assertEquals(1, excluded.size());
    }

    @Test
    @DisplayName("Should detect recently shown question")
    void testWasRecentlyShown() {
        // Arrange
        Long playerId = 1L;
        String gameType = "IDIOM";
        String questionId = "井底之蛙";

        questionHistoryService.addQuestion(playerId, gameType, questionId);

        // Act
        boolean wasShown = questionHistoryService.wasRecentlyShown(playerId, gameType, questionId);

        // Assert
        assertTrue(wasShown);
    }

    @Test
    @DisplayName("Should return false for question not in history")
    void testWasNotRecentlyShown() {
        // Arrange
        Long playerId = 1L;
        String gameType = "IDIOM";

        // Act
        boolean wasShown = questionHistoryService.wasRecentlyShown(playerId, gameType, "未添加的成语");

        // Assert
        assertFalse(wasShown);
    }

    @Test
    @DisplayName("Should maintain only last 10 questions")
    void testExclusionListSize() {
        // Arrange
        Long playerId = 1L;
        String gameType = "IDIOM";

        // Act - Add 15 questions
        for (int i = 1; i <= 15; i++) {
            questionHistoryService.addQuestion(playerId, gameType, "question" + i);
        }

        // Assert
        Set<String> excluded = questionHistoryService.getExcludedQuestions(playerId, gameType);
        assertEquals(10, excluded.size());
        assertFalse(excluded.contains("question1")); // First 5 should be removed
        assertTrue(excluded.contains("question15")); // Last one should be present
    }

    @Test
    @DisplayName("Should track different game types separately")
    void testSeparateGameTypes() {
        // Arrange
        Long playerId = 1L;
        String idiomQuestion = "井底之蛙";
        String sentenceQuestion = "我喜欢学中文";

        // Act
        questionHistoryService.addQuestion(playerId, "IDIOM", idiomQuestion);
        questionHistoryService.addQuestion(playerId, "SENTENCE", sentenceQuestion);

        // Assert
        Set<String> idiomExcluded = questionHistoryService.getExcludedQuestions(playerId, "IDIOM");
        Set<String> sentenceExcluded = questionHistoryService.getExcludedQuestions(playerId, "SENTENCE");

        assertEquals(1, idiomExcluded.size());
        assertEquals(1, sentenceExcluded.size());
        assertTrue(idiomExcluded.contains(idiomQuestion));
        assertTrue(sentenceExcluded.contains(sentenceQuestion));
        assertFalse(idiomExcluded.contains(sentenceQuestion));
    }

    @Test
    @DisplayName("Should track different players separately")
    void testSeparatePlayers() {
        // Arrange
        Long player1 = 1L;
        Long player2 = 2L;
        String gameType = "IDIOM";
        String question1 = "井底之蛙";
        String question2 = "画蛇添足";

        // Act
        questionHistoryService.addQuestion(player1, gameType, question1);
        questionHistoryService.addQuestion(player2, gameType, question2);

        // Assert
        Set<String> player1Excluded = questionHistoryService.getExcludedQuestions(player1, gameType);
        Set<String> player2Excluded = questionHistoryService.getExcludedQuestions(player2, gameType);

        assertTrue(player1Excluded.contains(question1));
        assertFalse(player1Excluded.contains(question2));
        assertTrue(player2Excluded.contains(question2));
        assertFalse(player2Excluded.contains(question1));
    }

    @Test
    @DisplayName("Should clear history for specific game type")
    void testClearHistoryForGameType() {
        // Arrange
        Long playerId = 1L;
        questionHistoryService.addQuestion(playerId, "IDIOM", "question1");
        questionHistoryService.addQuestion(playerId, "SENTENCE", "question2");

        // Act
        questionHistoryService.clearHistory(playerId, "IDIOM");

        // Assert
        Set<String> idiomExcluded = questionHistoryService.getExcludedQuestions(playerId, "IDIOM");
        Set<String> sentenceExcluded = questionHistoryService.getExcludedQuestions(playerId, "SENTENCE");

        assertEquals(0, idiomExcluded.size());
        assertEquals(1, sentenceExcluded.size());
    }

    @Test
    @DisplayName("Should clear all history for player")
    void testClearAllHistoryForPlayer() {
        // Arrange
        Long playerId = 1L;
        questionHistoryService.addQuestion(playerId, "IDIOM", "question1");
        questionHistoryService.addQuestion(playerId, "SENTENCE", "question2");

        // Act
        questionHistoryService.clearHistory(playerId, null);

        // Assert
        Set<String> idiomExcluded = questionHistoryService.getExcludedQuestions(playerId, "IDIOM");
        Set<String> sentenceExcluded = questionHistoryService.getExcludedQuestions(playerId, "SENTENCE");

        assertEquals(0, idiomExcluded.size());
        assertEquals(0, sentenceExcluded.size());
    }

    @Test
    @DisplayName("Should get correct history size")
    void testGetHistorySize() {
        // Arrange
        Long playerId = 1L;
        String gameType = "IDIOM";

        // Act
        questionHistoryService.addQuestion(playerId, gameType, "q1");
        questionHistoryService.addQuestion(playerId, gameType, "q2");
        questionHistoryService.addQuestion(playerId, gameType, "q3");

        // Assert
        int size = questionHistoryService.getHistorySize(playerId, gameType);
        assertEquals(3, size);
    }

    @Test
    @DisplayName("Should return empty set for new player")
    void testGetExcludedQuestionsForNewPlayer() {
        // Arrange
        Long playerId = 999L;
        String gameType = "IDIOM";

        // Act
        Set<String> excluded = questionHistoryService.getExcludedQuestions(playerId, gameType);

        // Assert
        assertNotNull(excluded);
        assertEquals(0, excluded.size());
    }

    @Test
    @DisplayName("Should handle duplicate question additions")
    void testDuplicateQuestions() {
        // Arrange
        Long playerId = 1L;
        String gameType = "IDIOM";
        String questionId = "井底之蛙";

        // Act
        questionHistoryService.addQuestion(playerId, gameType, questionId);
        questionHistoryService.addQuestion(playerId, gameType, questionId);

        // Assert
        Set<String> excluded = questionHistoryService.getExcludedQuestions(playerId, gameType);
        assertEquals(1, excluded.size()); // Should still only contain one instance
        assertTrue(excluded.contains(questionId));
    }

    @Test
    @DisplayName("Should maintain FIFO order when exceeding limit")
    void testFIFOOrder() {
        // Arrange
        Long playerId = 1L;
        String gameType = "IDIOM";

        // Act - Add 12 questions
        for (int i = 1; i <= 12; i++) {
            questionHistoryService.addQuestion(playerId, gameType, "q" + i);
        }

        // Assert
        Set<String> excluded = questionHistoryService.getExcludedQuestions(playerId, gameType);
        assertEquals(10, excluded.size());
        assertFalse(excluded.contains("q1")); // Oldest should be removed
        assertFalse(excluded.contains("q2")); // Second oldest should be removed
        assertTrue(excluded.contains("q3")); // Should still be present
        assertTrue(excluded.contains("q12")); // Most recent should be present
    }

    @Test
    @DisplayName("Should generate statistics correctly")
    void testGetStatistics() {
        // Arrange
        questionHistoryService.addQuestion(1L, "IDIOM", "q1");
        questionHistoryService.addQuestion(1L, "SENTENCE", "q2");
        questionHistoryService.addQuestion(2L, "IDIOM", "q3");

        // Act
        var stats = questionHistoryService.getStatistics();

        // Assert
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalPlayers"));
        assertTrue(stats.containsKey("totalEntries"));
        assertTrue(stats.containsKey("totalQuestionsTracked"));
        assertEquals(2, stats.get("totalPlayers")); // 2 unique players
        assertEquals(3, stats.get("totalEntries")); // 3 entries total
    }
}
