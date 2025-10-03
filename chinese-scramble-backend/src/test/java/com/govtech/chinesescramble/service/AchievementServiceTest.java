package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.Achievement;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.UserRole;
import com.govtech.chinesescramble.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Test class for AchievementService
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private IdiomScoreRepository idiomScoreRepository;

    @Mock
    private SentenceScoreRepository sentenceScoreRepository;

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private AchievementService achievementService;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testPlayer = createPlayer(1L, "achiever");
        reset(achievementRepository, playerRepository, idiomScoreRepository,
              sentenceScoreRepository, leaderboardRepository);

        // Default stub: all achievement types return false (not yet unlocked)
        // This prevents strict stubbing violations when checkAndUnlockAchievements() checks all achievement types
        when(achievementRepository.existsByPlayerIdAndAchievementType(anyLong(), anyString()))
            .thenReturn(false);
        when(achievementRepository.save(any(Achievement.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testCheckAndUnlockAchievements_FirstWin() {
        // Given
        Long playerId = 1L;
        Integer score = 250;
        Integer timeTaken = 45;
        Double accuracyRate = 100.0;
        Integer hintsUsed = 0;

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, score, timeTaken, accuracyRate, hintsUsed);

        // Then
        assertThat(achievements).isNotEmpty();
        assertThat(achievements).anyMatch(a -> "FIRST_WIN".equals(a.getAchievementType()));
        verify(achievementRepository, atLeastOnce()).save(any(Achievement.class));
    }

    @Test
    void testCheckAndUnlockAchievements_SpeedDemon() {
        // Given
        Long playerId = 1L;
        Integer score = 200;
        Integer timeTaken = 25; // <30 seconds
        Double accuracyRate = 95.0;
        Integer hintsUsed = 0;

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, score, timeTaken, accuracyRate, hintsUsed);

        // Then
        assertThat(achievements).anyMatch(a -> "SPEED_DEMON".equals(a.getAchievementType()));
    }

    @Test
    void testCheckAndUnlockAchievements_PerfectScore() {
        // Given
        Long playerId = 1L;
        Integer score = 250;
        Integer timeTaken = 40;
        Double accuracyRate = 100.0;
        Integer hintsUsed = 0;

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, score, timeTaken, accuracyRate, hintsUsed);

        // Then
        assertThat(achievements).anyMatch(a -> "PERFECT_SCORE".equals(a.getAchievementType()));
    }

    @Test
    void testCheckAndUnlockAchievements_HighScorer() {
        // Given
        Long playerId = 1L;
        Integer score = 1200; // >1000
        Integer timeTaken = 40;
        Double accuracyRate = 100.0;
        Integer hintsUsed = 0;

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, score, timeTaken, accuracyRate, hintsUsed);

        // Then
        assertThat(achievements).anyMatch(a -> "HIGH_SCORER".equals(a.getAchievementType()));
    }

    @Test
    void testCheckAndUnlockAchievements_NoDuplicates() {
        // Given
        Long playerId = 1L;
        Integer score = 250;
        Integer timeTaken = 25;
        Double accuracyRate = 100.0;
        Integer hintsUsed = 0;

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(achievementRepository.existsByPlayerIdAndAchievementType(eq(playerId), any(String.class)))
            .thenReturn(true); // All achievements already unlocked
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(50L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(50L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, score, timeTaken, accuracyRate, hintsUsed);

        // Then
        assertThat(achievements).isEmpty(); // No new achievements
        verify(achievementRepository, never()).save(any(Achievement.class));
    }

    @Test
    void testGetPlayerAchievements() {
        // Given
        Long playerId = 1L;
        List<Achievement> playerAchievements = List.of(
            createAchievement(testPlayer, "FIRST_WIN"),
            createAchievement(testPlayer, "SPEED_DEMON")
        );

        when(achievementRepository.findByPlayerId(playerId)).thenReturn(playerAchievements);

        // When
        List<Achievement> result = achievementService.getPlayerAchievements(playerId);

        // Then
        assertThat(result).hasSize(2);
        verify(achievementRepository).findByPlayerId(playerId);
    }

    @Test
    void testGetUnlockedAchievements() {
        // Given
        Long playerId = 1L;
        List<Achievement> unlockedAchievements = List.of(
            createAchievement(testPlayer, "FIRST_WIN"),
            createAchievement(testPlayer, "PERFECT_SCORE")
        );

        when(achievementRepository.findByPlayerId(playerId)).thenReturn(unlockedAchievements);

        // When
        List<Achievement> result = achievementService.getUnlockedAchievements(playerId);

        // Then
        assertThat(result).hasSize(2);
        verify(achievementRepository).findByPlayerId(playerId);
    }

    @Test
    void testCheckHundredGames() {
        // Given
        Long playerId = 1L;

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(60L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(40L); // Total: 100

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 200, 50, 0.9, 1);

        // Then
        assertThat(achievements).anyMatch(a -> "HUNDRED_GAMES".equals(a.getAchievementType()));
    }

    @Test
    void testCheckHundredGames_NotYetReached() {
        // Given
        Long playerId = 1L;

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(50L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(30L); // Total: 80

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 200, 50, 0.9, 1);

        // Then
        assertThat(achievements).noneMatch(a -> "HUNDRED_GAMES".equals(a.getAchievementType()));
    }

    @Test
    void testCheckTopRanked() {
        // Given
        Long playerId = 1L;
        com.govtech.chinesescramble.entity.Leaderboard topEntry = createLeaderboardEntry(playerId, 5);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(leaderboardRepository.findTopTenEntriesByPlayer(playerId))
            .thenReturn(List.of(topEntry));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 200, 50, 0.9, 1);

        // Then
        assertThat(achievements).anyMatch(a -> "TOP_RANKED".equals(a.getAchievementType()));
    }

    @Test
    void testCheckTopRanked_NotInTopTen() {
        // Given
        Long playerId = 1L;

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(leaderboardRepository.findTopTenEntriesByPlayer(playerId))
            .thenReturn(List.of()); // Empty list
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 200, 50, 0.9, 1);

        // Then
        assertThat(achievements).noneMatch(a -> "TOP_RANKED".equals(a.getAchievementType()));
    }

    @Test
    void testCheckHintFree() {
        // Given
        Long playerId = 1L;

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countHintFreeGamesByPlayer(playerId)).thenReturn(6L);
        when(sentenceScoreRepository.countHintFreeGamesByPlayer(playerId)).thenReturn(4L); // Total: 10
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 200, 50, 0.9, 0);

        // Then
        assertThat(achievements).anyMatch(a -> "HINT_FREE".equals(a.getAchievementType()));
    }

    @Test
    void testCheckHintFree_NotYetReached() {
        // Given
        Long playerId = 1L;

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countHintFreeGamesByPlayer(playerId)).thenReturn(4L);
        when(sentenceScoreRepository.countHintFreeGamesByPlayer(playerId)).thenReturn(3L); // Total: 7
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 200, 50, 0.9, 0);

        // Then
        assertThat(achievements).noneMatch(a -> "HINT_FREE".equals(a.getAchievementType()));
    }

    @Test
    void testGetRecentAchievements() {
        // Given
        Long playerId = 1L;
        List<Achievement> recentAchievements = List.of(
            createAchievement(testPlayer, "FIRST_WIN"),
            createAchievement(testPlayer, "SPEED_DEMON")
        );

        when(achievementRepository.findRecentAchievements(eq(playerId), any(java.time.LocalDateTime.class)))
            .thenReturn(recentAchievements);

        // When
        List<Achievement> result = achievementService.getRecentAchievements(playerId);

        // Then
        assertThat(result).hasSize(2);
        verify(achievementRepository).findRecentAchievements(eq(playerId), any(java.time.LocalDateTime.class));
    }

    @Test
    void testGetAchievementCount() {
        // Given
        Long playerId = 1L;
        when(achievementRepository.countByPlayerId(playerId)).thenReturn(5L);

        // When
        long count = achievementService.getAchievementCount(playerId);

        // Then
        assertThat(count).isEqualTo(5L);
        verify(achievementRepository).countByPlayerId(playerId);
    }

    @Test
    void testGetAchievementRarity() {
        // Given
        String achievementType = "FIRST_WIN";
        when(playerRepository.count()).thenReturn(100L);
        when(achievementRepository.countByAchievementType(achievementType)).thenReturn(25L);

        // When
        double rarity = achievementService.getAchievementRarity(achievementType);

        // Then
        assertThat(rarity).isEqualTo(25.0); // 25% of players have it
        verify(playerRepository).count();
        verify(achievementRepository).countByAchievementType(achievementType);
    }

    @Test
    void testGetAchievementRarity_NoPlayers() {
        // Given - Test division by zero handling
        String achievementType = "FIRST_WIN";
        when(playerRepository.count()).thenReturn(0L);

        // When
        double rarity = achievementService.getAchievementRarity(achievementType);

        // Then
        assertThat(rarity).isEqualTo(0.0);
        verify(playerRepository).count();
        verify(achievementRepository, never()).countByAchievementType(anyString());
    }

    @Test
    void testGetAchievementStatistics() {
        // Given
        List<Object[]> distribution = List.of(
            new Object[]{"FIRST_WIN", 50L},
            new Object[]{"SPEED_DEMON", 30L},
            new Object[]{"PERFECT_SCORE", 20L}
        );
        when(achievementRepository.getAchievementDistribution()).thenReturn(distribution);

        // When
        java.util.Map<String, Long> stats = achievementService.getAchievementStatistics();

        // Then
        assertThat(stats).hasSize(3);
        assertThat(stats.get("FIRST_WIN")).isEqualTo(50L);
        assertThat(stats.get("SPEED_DEMON")).isEqualTo(30L);
        assertThat(stats.get("PERFECT_SCORE")).isEqualTo(20L);
        verify(achievementRepository).getAchievementDistribution();
    }

    @Test
    void testGetAchievementStatistics_Empty() {
        // Given
        when(achievementRepository.getAchievementDistribution()).thenReturn(List.of());

        // When
        java.util.Map<String, Long> stats = achievementService.getAchievementStatistics();

        // Then
        assertThat(stats).isEmpty();
        verify(achievementRepository).getAchievementDistribution();
    }

    @Test
    void testHasAchievement_True() {
        // Given
        Long playerId = 1L;
        String achievementType = "FIRST_WIN";
        when(achievementRepository.existsByPlayerIdAndAchievementType(playerId, achievementType))
            .thenReturn(true);

        // When
        boolean hasIt = achievementService.hasAchievement(playerId, achievementType);

        // Then
        assertThat(hasIt).isTrue();
        verify(achievementRepository).existsByPlayerIdAndAchievementType(playerId, achievementType);
    }

    @Test
    void testHasAchievement_False() {
        // Given
        Long playerId = 1L;
        String achievementType = "SPEED_DEMON";
        when(achievementRepository.existsByPlayerIdAndAchievementType(playerId, achievementType))
            .thenReturn(false);

        // When
        boolean hasIt = achievementService.hasAchievement(playerId, achievementType);

        // Then
        assertThat(hasIt).isFalse();
        verify(achievementRepository).existsByPlayerIdAndAchievementType(playerId, achievementType);
    }

    @Test
    void testUnlockAchievement_JsonConversionError() throws Exception {
        // Given
        Long playerId = 1L;
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON error"));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 200, 50, 0.9, 1);

        // Then - Should still unlock achievement with empty JSON metadata
        assertThat(achievements).isNotEmpty();
        verify(achievementRepository, atLeastOnce()).save(any(Achievement.class));
    }

    @Test
    void testCheckSpeedDemon_ExactlyAtThreshold() {
        // Given - Test boundary condition (29 seconds, still under 30)
        Long playerId = 1L;
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 200, 29, 0.9, 1);

        // Then
        assertThat(achievements).anyMatch(a -> "SPEED_DEMON".equals(a.getAchievementType()));
    }

    @Test
    void testCheckSpeedDemon_JustOverThreshold() {
        // Given - Test boundary condition (30 seconds, at threshold)
        Long playerId = 1L;
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 200, 30, 0.9, 1);

        // Then
        assertThat(achievements).noneMatch(a -> "SPEED_DEMON".equals(a.getAchievementType()));
    }

    @Test
    void testCheckPerfectScore_WithHints() {
        // Given - Perfect accuracy but used hints
        Long playerId = 1L;
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 250, 40, 1.0, 1); // Used 1 hint

        // Then
        assertThat(achievements).noneMatch(a -> "PERFECT_SCORE".equals(a.getAchievementType()));
    }

    @Test
    void testCheckPerfectScore_ImperfectAccuracy() {
        // Given - No hints but accuracy not perfect
        Long playerId = 1L;
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 250, 40, 0.99, 0); // 99% accuracy

        // Then
        assertThat(achievements).noneMatch(a -> "PERFECT_SCORE".equals(a.getAchievementType()));
    }

    @Test
    void testCheckHighScorer_ExactlyAtThreshold() {
        // Given - Exactly 1000 points
        Long playerId = 1L;
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 1000, 40, 0.9, 1);

        // Then
        assertThat(achievements).anyMatch(a -> "HIGH_SCORER".equals(a.getAchievementType()));
    }

    @Test
    void testCheckHighScorer_JustUnderThreshold() {
        // Given - 999 points (just under)
        Long playerId = 1L;
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 999, 40, 0.9, 1);

        // Then
        assertThat(achievements).noneMatch(a -> "HIGH_SCORER".equals(a.getAchievementType()));
    }

    @Test
    void testGetUnlockedAchievements_WithNullUnlockedAt() {
        // Given
        Long playerId = 1L;
        Achievement unlockedAchievement = createAchievement(testPlayer, "FIRST_WIN");
        Achievement lockedAchievement = Achievement.builder()
            .player(testPlayer)
            .achievementType("LOCKED")
            .title("Locked")
            .description("Not yet unlocked")
            .metadata("{}")
            .build();
        // Don't set unlockedAt for locked achievement

        when(achievementRepository.findByPlayerId(playerId))
            .thenReturn(List.of(unlockedAchievement, lockedAchievement));

        // When
        List<Achievement> result = achievementService.getUnlockedAchievements(playerId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAchievementType()).isEqualTo("FIRST_WIN");
    }

    // ========================================================================
    // checkIdiomMaster() Tests - 0% coverage (private method via reflection)
    // ========================================================================

    @Test
    void testCheckIdiomMaster_HasFirstPlace() throws Exception {
        // Given
        Long playerId = 1L;
        com.govtech.chinesescramble.entity.Leaderboard idiomFirstPlace = createLeaderboardEntry(
            playerId,
            1,
            com.govtech.chinesescramble.entity.enums.GameType.IDIOM,
            com.govtech.chinesescramble.entity.enums.DifficultyLevel.MEDIUM
        );

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(leaderboardRepository.findFirstPlaceEntriesByPlayer(playerId))
            .thenReturn(List.of(idiomFirstPlace));

        // When - Use reflection to call private method
        java.lang.reflect.Method method = AchievementService.class
            .getDeclaredMethod("checkIdiomMaster", Long.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Achievement> achievements = (List<Achievement>) method.invoke(achievementService, playerId);

        // Then
        assertThat(achievements).hasSize(1);
        assertThat(achievements.get(0).getAchievementType()).isEqualTo("IDIOM_MASTER");
        verify(leaderboardRepository).findFirstPlaceEntriesByPlayer(playerId);
    }

    @Test
    void testCheckIdiomMaster_NoFirstPlace() throws Exception {
        // Given
        Long playerId = 1L;
        com.govtech.chinesescramble.entity.Leaderboard sentenceSecondPlace = createLeaderboardEntry(
            playerId,
            2,
            com.govtech.chinesescramble.entity.enums.GameType.SENTENCE,
            com.govtech.chinesescramble.entity.enums.DifficultyLevel.EASY
        );

        when(leaderboardRepository.findFirstPlaceEntriesByPlayer(playerId))
            .thenReturn(List.of(sentenceSecondPlace)); // Sentence, not idiom

        // When
        java.lang.reflect.Method method = AchievementService.class
            .getDeclaredMethod("checkIdiomMaster", Long.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Achievement> achievements = (List<Achievement>) method.invoke(achievementService, playerId);

        // Then
        assertThat(achievements).isEmpty();
    }

    @Test
    void testCheckIdiomMaster_EmptyFirstPlaceEntries() throws Exception {
        // Given
        Long playerId = 1L;

        when(leaderboardRepository.findFirstPlaceEntriesByPlayer(playerId))
            .thenReturn(List.of()); // No first place entries

        // When
        java.lang.reflect.Method method = AchievementService.class
            .getDeclaredMethod("checkIdiomMaster", Long.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Achievement> achievements = (List<Achievement>) method.invoke(achievementService, playerId);

        // Then
        assertThat(achievements).isEmpty();
    }

    @Test
    void testCheckIdiomMaster_AlreadyUnlocked() throws Exception {
        // Given
        Long playerId = 1L;
        com.govtech.chinesescramble.entity.Leaderboard idiomFirstPlace = createLeaderboardEntry(
            playerId,
            1,
            com.govtech.chinesescramble.entity.enums.GameType.IDIOM,
            com.govtech.chinesescramble.entity.enums.DifficultyLevel.HARD
        );

        when(achievementRepository.existsByPlayerIdAndAchievementType(playerId, "IDIOM_MASTER"))
            .thenReturn(true); // Already has achievement
        when(leaderboardRepository.findFirstPlaceEntriesByPlayer(playerId))
            .thenReturn(List.of(idiomFirstPlace));

        // When
        java.lang.reflect.Method method = AchievementService.class
            .getDeclaredMethod("checkIdiomMaster", Long.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Achievement> achievements = (List<Achievement>) method.invoke(achievementService, playerId);

        // Then
        assertThat(achievements).isEmpty();
    }

    // ========================================================================
    // checkSentenceMaster() Tests - 0% coverage (private method via reflection)
    // ========================================================================

    @Test
    void testCheckSentenceMaster_HasFirstPlace() throws Exception {
        // Given
        Long playerId = 1L;
        com.govtech.chinesescramble.entity.Leaderboard sentenceFirstPlace = createLeaderboardEntry(
            playerId,
            1,
            com.govtech.chinesescramble.entity.enums.GameType.SENTENCE,
            com.govtech.chinesescramble.entity.enums.DifficultyLevel.EASY
        );

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(leaderboardRepository.findFirstPlaceEntriesByPlayer(playerId))
            .thenReturn(List.of(sentenceFirstPlace));

        // When
        java.lang.reflect.Method method = AchievementService.class
            .getDeclaredMethod("checkSentenceMaster", Long.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Achievement> achievements = (List<Achievement>) method.invoke(achievementService, playerId);

        // Then
        assertThat(achievements).hasSize(1);
        assertThat(achievements.get(0).getAchievementType()).isEqualTo("SENTENCE_MASTER");
        verify(leaderboardRepository).findFirstPlaceEntriesByPlayer(playerId);
    }

    @Test
    void testCheckSentenceMaster_NoFirstPlace() throws Exception {
        // Given
        Long playerId = 1L;
        com.govtech.chinesescramble.entity.Leaderboard idiomSecondPlace = createLeaderboardEntry(
            playerId,
            2,
            com.govtech.chinesescramble.entity.enums.GameType.IDIOM,
            com.govtech.chinesescramble.entity.enums.DifficultyLevel.HARD
        );

        when(leaderboardRepository.findFirstPlaceEntriesByPlayer(playerId))
            .thenReturn(List.of(idiomSecondPlace)); // Idiom, not sentence

        // When
        java.lang.reflect.Method method = AchievementService.class
            .getDeclaredMethod("checkSentenceMaster", Long.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Achievement> achievements = (List<Achievement>) method.invoke(achievementService, playerId);

        // Then
        assertThat(achievements).isEmpty();
    }

    @Test
    void testCheckSentenceMaster_EmptyFirstPlaceEntries() throws Exception {
        // Given
        Long playerId = 1L;

        when(leaderboardRepository.findFirstPlaceEntriesByPlayer(playerId))
            .thenReturn(List.of()); // No first place entries

        // When
        java.lang.reflect.Method method = AchievementService.class
            .getDeclaredMethod("checkSentenceMaster", Long.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Achievement> achievements = (List<Achievement>) method.invoke(achievementService, playerId);

        // Then
        assertThat(achievements).isEmpty();
    }

    @Test
    void testCheckSentenceMaster_AlreadyUnlocked() throws Exception {
        // Given
        Long playerId = 1L;
        com.govtech.chinesescramble.entity.Leaderboard sentenceFirstPlace = createLeaderboardEntry(
            playerId,
            1,
            com.govtech.chinesescramble.entity.enums.GameType.SENTENCE,
            com.govtech.chinesescramble.entity.enums.DifficultyLevel.MEDIUM
        );

        when(achievementRepository.existsByPlayerIdAndAchievementType(playerId, "SENTENCE_MASTER"))
            .thenReturn(true); // Already has achievement
        when(leaderboardRepository.findFirstPlaceEntriesByPlayer(playerId))
            .thenReturn(List.of(sentenceFirstPlace));

        // When
        java.lang.reflect.Method method = AchievementService.class
            .getDeclaredMethod("checkSentenceMaster", Long.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Achievement> achievements = (List<Achievement>) method.invoke(achievementService, playerId);

        // Then
        assertThat(achievements).isEmpty();
    }

    @Test
    void testCheckBothMasters_BothUnlocked() throws Exception {
        // Given - Player has first place in both game types
        Long playerId = 1L;
        com.govtech.chinesescramble.entity.Leaderboard idiomFirstPlace = createLeaderboardEntry(
            playerId,
            1,
            com.govtech.chinesescramble.entity.enums.GameType.IDIOM,
            com.govtech.chinesescramble.entity.enums.DifficultyLevel.HARD
        );
        com.govtech.chinesescramble.entity.Leaderboard sentenceFirstPlace = createLeaderboardEntry(
            playerId,
            1,
            com.govtech.chinesescramble.entity.enums.GameType.SENTENCE,
            com.govtech.chinesescramble.entity.enums.DifficultyLevel.MEDIUM
        );

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(testPlayer));
        when(leaderboardRepository.findFirstPlaceEntriesByPlayer(playerId))
            .thenReturn(List.of(idiomFirstPlace, sentenceFirstPlace));

        // When - call both methods
        java.lang.reflect.Method idiomMethod = AchievementService.class
            .getDeclaredMethod("checkIdiomMaster", Long.class);
        idiomMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Achievement> idiomAchievements = (List<Achievement>) idiomMethod.invoke(achievementService, playerId);

        java.lang.reflect.Method sentenceMethod = AchievementService.class
            .getDeclaredMethod("checkSentenceMaster", Long.class);
        sentenceMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Achievement> sentenceAchievements = (List<Achievement>) sentenceMethod.invoke(achievementService, playerId);

        // Then
        assertThat(idiomAchievements).hasSize(1);
        assertThat(idiomAchievements.get(0).getAchievementType()).isEqualTo("IDIOM_MASTER");
        assertThat(sentenceAchievements).hasSize(1);
        assertThat(sentenceAchievements.get(0).getAchievementType()).isEqualTo("SENTENCE_MASTER");
    }

    // Helper methods

    private com.govtech.chinesescramble.entity.Leaderboard createLeaderboardEntry(
        Long playerId,
        Integer rank,
        com.govtech.chinesescramble.entity.enums.GameType gameType,
        com.govtech.chinesescramble.entity.enums.DifficultyLevel difficulty
    ) {
        com.govtech.chinesescramble.entity.Leaderboard entry =
            com.govtech.chinesescramble.entity.Leaderboard.builder()
                .player(testPlayer)
                .gameType(gameType)
                .difficulty(difficulty)
                .rank(rank)
                .totalScore(1000)
                .gamesPlayed(10)
                .averageScore(100.0)
                .accuracyRate(95.0)
                .build();
        return entry;
    }

    private com.govtech.chinesescramble.entity.Leaderboard createLeaderboardEntry(Long playerId, Integer rank) {
        com.govtech.chinesescramble.entity.Leaderboard entry =
            com.govtech.chinesescramble.entity.Leaderboard.builder()
                .player(testPlayer)
                .gameType(com.govtech.chinesescramble.entity.enums.GameType.IDIOM)
                .difficulty(com.govtech.chinesescramble.entity.enums.DifficultyLevel.MEDIUM)
                .rank(rank)
                .totalScore(1000)
                .gamesPlayed(10)
                .averageScore(100.0)
                .accuracyRate(95.0)
                .build();
        return entry;
    }

    private Player createPlayer(Long id, String username) {
        Player player = Player.builder()
            .username(username)
            .email(username + "@test.com")
            .passwordHash("hash")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        // Set id via reflection for test purposes
        try {
            java.lang.reflect.Field idField = player.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(player, id);
        } catch (Exception ignored) {
        }
        return player;
    }

    private Achievement createAchievement(Player player, String achievementType) {
        String title = switch (achievementType) {
            case "FIRST_WIN" -> "首次胜利";
            case "SPEED_DEMON" -> "速度恶魔";
            case "PERFECT_SCORE" -> "完美得分";
            case "HUNDRED_GAMES" -> "百场达人";
            case "IDIOM_MASTER" -> "成语大师";
            case "SENTENCE_MASTER" -> "句子大师";
            case "TOP_RANKED" -> "顶级玩家";
            case "CONSISTENCY" -> "坚持不懈";
            case "HIGH_SCORER" -> "高分达人";
            case "HINT_FREE" -> "无提示挑战";
            default -> "Unknown Achievement";
        };

        Achievement achievement = Achievement.builder()
            .player(player)
            .achievementType(achievementType)
            .title(title)
            .description("Achievement unlocked")
            .metadata("{}")
            .build();
        // Manually trigger @PrePersist behavior for test
        if (achievement.getUnlockedAt() == null) {
            achievement.setUnlockedAt(java.time.LocalDateTime.now());
        }
        return achievement;
    }
}
