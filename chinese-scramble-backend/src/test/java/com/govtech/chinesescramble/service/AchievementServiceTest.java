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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for AchievementService
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
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

    @InjectMocks
    private AchievementService achievementService;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testPlayer = createPlayer(1L, "achiever");
        reset(achievementRepository, playerRepository, idiomScoreRepository,
              sentenceScoreRepository, leaderboardRepository);
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
        when(achievementRepository.existsByPlayerIdAndAchievementType(playerId, "FIRST_WIN"))
            .thenReturn(false);
        when(achievementRepository.save(any(Achievement.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, score, timeTaken, accuracyRate, hintsUsed);

        // Then
        assertThat(achievements).isNotEmpty();
        assertThat(achievements).anyMatch(a -> "FIRST_WIN".equals(a.getAchievementType()));
        verify(achievementRepository).save(any(Achievement.class));
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
        when(achievementRepository.existsByPlayerIdAndAchievementType(playerId, "SPEED_DEMON"))
            .thenReturn(false);
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);
        when(achievementRepository.save(any(Achievement.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

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
        when(achievementRepository.existsByPlayerIdAndAchievementType(playerId, "PERFECT_SCORE"))
            .thenReturn(false);
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);
        when(achievementRepository.save(any(Achievement.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

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
        when(achievementRepository.existsByPlayerIdAndAchievementType(playerId, "HIGH_SCORER"))
            .thenReturn(false);
        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(1L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);
        when(achievementRepository.save(any(Achievement.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

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
        when(achievementRepository.existsByPlayerIdAndAchievementType(playerId, "HUNDRED_GAMES"))
            .thenReturn(false);
        when(achievementRepository.save(any(Achievement.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

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
        when(achievementRepository.existsByPlayerIdAndAchievementType(eq(playerId), any(String.class)))
            .thenReturn(false);
        when(achievementRepository.save(any(Achievement.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Achievement> achievements = achievementService
            .checkAndUnlockAchievements(playerId, 200, 50, 0.9, 1);

        // Then
        assertThat(achievements).noneMatch(a -> "HUNDRED_GAMES".equals(a.getAchievementType()));
    }

    // Helper methods

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

        return Achievement.builder()
            .player(player)
            .achievementType(achievementType)
            .title(title)
            .description("Achievement unlocked")
            .metadata("{}")
            .build();
    }
}
