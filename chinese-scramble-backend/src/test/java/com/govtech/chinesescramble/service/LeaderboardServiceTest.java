package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.Leaderboard;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.entity.enums.UserRole;
import com.govtech.chinesescramble.repository.IdiomScoreRepository;
import com.govtech.chinesescramble.repository.LeaderboardRepository;
import com.govtech.chinesescramble.repository.PlayerRepository;
import com.govtech.chinesescramble.repository.SentenceScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for LeaderboardService
 * Tests cover:
 * - Leaderboard updates after games
 * - Rank recalculation algorithms
 * - Top players retrieval
 * - Player position lookup
 * - Scheduled recalculation
 * - Statistics calculation
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private IdiomScoreRepository idiomScoreRepository;

    @Mock
    private SentenceScoreRepository sentenceScoreRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    private Player testPlayer;
    private Leaderboard testLeaderboard;

    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hash")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        ReflectionTestUtils.setField(testPlayer, "id", 1L);

        testLeaderboard = Leaderboard.builder()
            .player(testPlayer)
            .gameType(GameType.IDIOM)
            .difficulty(DifficultyLevel.EASY)
            .totalScore(1000)
            .gamesPlayed(10)
            .averageScore(100.0)
            .accuracyRate(0.85)
            .rank(1)
            .build();
        ReflectionTestUtils.setField(testLeaderboard, "id", 1L);
    }

    @Test
    void testUpdateLeaderboardAfterGame_NewEntry() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(leaderboardRepository.findByPlayerIdAndGameTypeAndDifficulty(1L, GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.empty());
        when(leaderboardRepository.save(any(Leaderboard.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(leaderboardRepository.findAllForRankRecalculation(GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Collections.emptyList());

        // Act
        leaderboardService.updateLeaderboardAfterGame(1L, GameType.IDIOM, DifficultyLevel.EASY, 100, 0.85);

        // Assert
        verify(leaderboardRepository).save(argThat(leaderboard ->
            leaderboard.getTotalScore() == 100 &&
            leaderboard.getGamesPlayed() == 1 &&
            leaderboard.getGameType() == GameType.IDIOM
        ));
        verify(leaderboardRepository).findAllForRankRecalculation(GameType.IDIOM, DifficultyLevel.EASY);
    }

    @Test
    void testUpdateLeaderboardAfterGame_ExistingEntry() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(leaderboardRepository.findByPlayerIdAndGameTypeAndDifficulty(1L, GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.of(testLeaderboard));
        when(leaderboardRepository.save(any(Leaderboard.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(leaderboardRepository.findAllForRankRecalculation(GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Collections.emptyList());

        // Act
        leaderboardService.updateLeaderboardAfterGame(1L, GameType.IDIOM, DifficultyLevel.EASY, 150, 0.90);

        // Assert
        verify(leaderboardRepository).save(testLeaderboard);
        verify(leaderboardRepository).findAllForRankRecalculation(GameType.IDIOM, DifficultyLevel.EASY);
    }

    @Test
    void testUpdateLeaderboardAfterGame_PlayerNotFound() {
        // Arrange
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            leaderboardService.updateLeaderboardAfterGame(999L, GameType.IDIOM, DifficultyLevel.EASY, 100, 0.85));
    }

    @Test
    void testRecalculateRanks_EmptyList() {
        // Arrange
        when(leaderboardRepository.findAllForRankRecalculation(GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Collections.emptyList());

        // Act
        leaderboardService.recalculateRanks(GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        verify(leaderboardRepository, never()).saveAll(anyList());
    }

    @Test
    void testRecalculateRanks_WithTies() {
        // Arrange
        Leaderboard player1 = Leaderboard.builder()
            .player(testPlayer)
            .gameType(GameType.IDIOM)
            .difficulty(DifficultyLevel.EASY)
            .totalScore(1000)
            .gamesPlayed(10)
            .build();

        Player player2 = Player.builder()
            .username("player2")
            .email("player2@test.com")
            .passwordHash("hash")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        ReflectionTestUtils.setField(player2, "id", 2L);

        Leaderboard player2Entry = Leaderboard.builder()
            .player(player2)
            .gameType(GameType.IDIOM)
            .difficulty(DifficultyLevel.EASY)
            .totalScore(1000) // Same score as player1 (tie)
            .gamesPlayed(8)
            .build();

        Player player3 = Player.builder()
            .username("player3")
            .email("player3@test.com")
            .passwordHash("hash")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        ReflectionTestUtils.setField(player3, "id", 3L);

        Leaderboard player3Entry = Leaderboard.builder()
            .player(player3)
            .gameType(GameType.IDIOM)
            .difficulty(DifficultyLevel.EASY)
            .totalScore(900)
            .gamesPlayed(9)
            .build();

        List<Leaderboard> entries = Arrays.asList(player1, player2Entry, player3Entry);

        when(leaderboardRepository.findAllForRankRecalculation(GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(entries);
        when(leaderboardRepository.saveAll(anyList())).thenReturn(entries);

        // Act
        leaderboardService.recalculateRanks(GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        verify(leaderboardRepository).saveAll(argThat(list -> {
            Leaderboard first = ((List<Leaderboard>) list).get(0);
            Leaderboard second = ((List<Leaderboard>) list).get(1);
            Leaderboard third = ((List<Leaderboard>) list).get(2);
            // First two should have rank 1 (tie), third should have rank 3
            return first.getRank() == 1 && second.getRank() == 1 && third.getRank() == 3;
        }));
    }

    @Test
    void testGetTopPlayers() {
        // Arrange
        List<Leaderboard> topPlayers = Arrays.asList(testLeaderboard);
        when(leaderboardRepository.findTopPlayersByGameTypeAndDifficulty(GameType.IDIOM, DifficultyLevel.EASY, 10))
            .thenReturn(topPlayers);

        // Act
        List<Leaderboard> result = leaderboardService.getTopPlayers(GameType.IDIOM, DifficultyLevel.EASY, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLeaderboard, result.get(0));
    }

    @Test
    void testGetPlayerPosition_Found() {
        // Arrange
        when(leaderboardRepository.findByPlayerIdAndGameTypeAndDifficulty(1L, GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.of(testLeaderboard));

        // Act
        Optional<Leaderboard> result = leaderboardService.getPlayerPosition(1L, GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testLeaderboard, result.get());
    }

    @Test
    void testGetPlayerPosition_NotFound() {
        // Arrange
        when(leaderboardRepository.findByPlayerIdAndGameTypeAndDifficulty(999L, GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.empty());

        // Act
        Optional<Leaderboard> result = leaderboardService.getPlayerPosition(999L, GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testGetPlayerLeaderboards() {
        // Arrange
        List<Leaderboard> leaderboards = Arrays.asList(testLeaderboard);
        when(leaderboardRepository.findByPlayerId(1L)).thenReturn(leaderboards);

        // Act
        List<Leaderboard> result = leaderboardService.getPlayerLeaderboards(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLeaderboard, result.get(0));
    }

    @Test
    void testGetPlayerRankings() {
        // Arrange
        List<Leaderboard> leaderboards = Arrays.asList(testLeaderboard);
        when(leaderboardRepository.findByPlayerId(1L)).thenReturn(leaderboards);

        // Act
        List<Leaderboard> result = leaderboardService.getPlayerRankings(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetPlayerRank() {
        // Arrange
        when(leaderboardRepository.findByPlayerIdAndGameTypeAndDifficulty(1L, GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.of(testLeaderboard));

        // Act
        Optional<Leaderboard> result = leaderboardService.getPlayerRank(1L, GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testLeaderboard, result.get());
    }

    @Test
    void testGetPlayersNearRank() {
        // Arrange
        List<Leaderboard> nearbyPlayers = Arrays.asList(testLeaderboard);
        when(leaderboardRepository.findPlayersNearRank(GameType.IDIOM, DifficultyLevel.EASY, 5, 2))
            .thenReturn(nearbyPlayers);

        // Act
        List<Leaderboard> result = leaderboardService.getPlayersNearRank(GameType.IDIOM, DifficultyLevel.EASY, 5, 2);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testIsPlayerInTopTen_True() {
        // Arrange
        testLeaderboard.setRank(5);
        when(leaderboardRepository.findByPlayerIdAndGameTypeAndDifficulty(1L, GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.of(testLeaderboard));

        // Act
        boolean result = leaderboardService.isPlayerInTopTen(1L, GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsPlayerInTopTen_False() {
        // Arrange
        testLeaderboard.setRank(15);
        when(leaderboardRepository.findByPlayerIdAndGameTypeAndDifficulty(1L, GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.of(testLeaderboard));

        // Act
        boolean result = leaderboardService.isPlayerInTopTen(1L, GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsPlayerInTopTen_NotRanked() {
        // Arrange
        when(leaderboardRepository.findByPlayerIdAndGameTypeAndDifficulty(999L, GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.empty());

        // Act
        boolean result = leaderboardService.isPlayerInTopTen(999L, GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsPlayerFirstPlace_True() {
        // Arrange
        testLeaderboard.setRank(1);
        when(leaderboardRepository.findByPlayerIdAndGameTypeAndDifficulty(1L, GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.of(testLeaderboard));

        // Act
        boolean result = leaderboardService.isPlayerFirstPlace(1L, GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsPlayerFirstPlace_False() {
        // Arrange
        testLeaderboard.setRank(2);
        when(leaderboardRepository.findByPlayerIdAndGameTypeAndDifficulty(1L, GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.of(testLeaderboard));

        // Act
        boolean result = leaderboardService.isPlayerFirstPlace(1L, GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetTotalRankedPlayers() {
        // Arrange
        when(leaderboardRepository.countByGameTypeAndDifficulty(GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(50L);

        // Act
        long result = leaderboardService.getTotalRankedPlayers(GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        assertEquals(50L, result);
    }

    @Test
    void testGetStatistics_WithData() {
        // Arrange
        Object[] statsData = {100L, 85.5, 0.78};
        when(leaderboardRepository.getLeaderboardStatistics(GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.of(statsData));

        // Act
        LeaderboardService.LeaderboardStatistics result =
            leaderboardService.getStatistics(GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.totalPlayers());
        assertEquals(85.5, result.averageScore());
        assertEquals(0.78, result.averageAccuracy());
    }

    @Test
    void testGetStatistics_NoData() {
        // Arrange
        when(leaderboardRepository.getLeaderboardStatistics(GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.empty());

        // Act
        LeaderboardService.LeaderboardStatistics result =
            leaderboardService.getStatistics(GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.totalPlayers());
        assertEquals(0.0, result.averageScore());
        assertEquals(0.0, result.averageAccuracy());
    }

    @Test
    void testGetStatistics_NullAverages() {
        // Arrange
        Object[] statsData = {50L, null, null};
        when(leaderboardRepository.getLeaderboardStatistics(GameType.IDIOM, DifficultyLevel.EASY))
            .thenReturn(Optional.of(statsData));

        // Act
        LeaderboardService.LeaderboardStatistics result =
            leaderboardService.getStatistics(GameType.IDIOM, DifficultyLevel.EASY);

        // Assert
        assertNotNull(result);
        assertEquals(50L, result.totalPlayers());
        assertEquals(0.0, result.averageScore());
        assertEquals(0.0, result.averageAccuracy());
    }

    @Test
    void testScheduledLeaderboardRecalculation() {
        // Arrange
        when(leaderboardRepository.findAllForRankRecalculation(any(GameType.class), any(DifficultyLevel.class)))
            .thenReturn(Collections.emptyList());

        // Act
        leaderboardService.scheduledLeaderboardRecalculation();

        // Assert
        // Should call recalculateRanks for IDIOM and SENTENCE (not COMBINED) for all 4 difficulty levels
        // Total = 2 game types * 4 difficulty levels = 8 calls
        verify(leaderboardRepository, atLeast(8)).findAllForRankRecalculation(any(GameType.class), any(DifficultyLevel.class));
    }

    @Test
    void testForceRecalculateAll() {
        // Arrange
        when(leaderboardRepository.findAllForRankRecalculation(any(GameType.class), any(DifficultyLevel.class)))
            .thenReturn(Collections.emptyList());

        // Act
        leaderboardService.forceRecalculateAll();

        // Assert
        verify(leaderboardRepository, atLeast(8)).findAllForRankRecalculation(any(GameType.class), any(DifficultyLevel.class));
    }

    @Test
    void testLeaderboardStatistics_AverageAccuracyPercentage() {
        // Arrange
        LeaderboardService.LeaderboardStatistics stats =
            new LeaderboardService.LeaderboardStatistics(100L, 85.5, 0.78);

        // Act
        double result = stats.averageAccuracyPercentage();

        // Assert
        assertEquals(78.0, result, 0.001);
    }
}
