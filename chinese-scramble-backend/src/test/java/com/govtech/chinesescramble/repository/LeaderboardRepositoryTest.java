package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.Leaderboard;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for LeaderboardRepository
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("LeaderboardRepository Integration Tests")
class LeaderboardRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private Player player1;
    private Player player2;
    private Player player3;

    @BeforeEach
    void setUp() {
        leaderboardRepository.deleteAll();
        playerRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test players with explicit timestamps
        LocalDateTime now = LocalDateTime.now();

        player1 = Player.builder()
            .username("玩家001")
            .email("player1@test.com")
            .passwordHash("$2a$10$hash1")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        player1.setCreatedAt(now.minusDays(7));
        player1.setUpdatedAt(now);

        player2 = Player.builder()
            .username("张伟")
            .email("player2@test.com")
            .passwordHash("$2a$10$hash2")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        player2.setCreatedAt(now.minusDays(5));
        player2.setUpdatedAt(now);

        player3 = Player.builder()
            .username("李娜")
            .email("player3@test.com")
            .passwordHash("$2a$10$hash3")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        player3.setCreatedAt(now.minusDays(3));
        player3.setUpdatedAt(now);

        entityManager.persist(player1);
        entityManager.persist(player2);
        entityManager.persist(player3);

        // Create leaderboard entries with explicit timestamps
        Leaderboard entry1 = Leaderboard.builder()
            .player(player1)
            .gameType(GameType.IDIOM)
            .difficulty(DifficultyLevel.EASY)
            .totalScore(1500)
            .gamesPlayed(5)
            .averageScore(300.0)
            .rank(1)
            .accuracyRate(0.95)
            .lastUpdated(now)
            .build();
        entry1.setCreatedAt(now.minusDays(5));
        entry1.setUpdatedAt(now);

        Leaderboard entry2 = Leaderboard.builder()
            .player(player2)
            .gameType(GameType.IDIOM)
            .difficulty(DifficultyLevel.EASY)
            .totalScore(1200)
            .gamesPlayed(4)
            .averageScore(300.0)
            .rank(2)
            .accuracyRate(0.92)
            .lastUpdated(now)
            .build();
        entry2.setCreatedAt(now.minusDays(4));
        entry2.setUpdatedAt(now);

        Leaderboard entry3 = Leaderboard.builder()
            .player(player3)
            .gameType(GameType.IDIOM)
            .difficulty(DifficultyLevel.EASY)
            .totalScore(1000)
            .gamesPlayed(3)
            .averageScore(333.3)
            .rank(3)
            .accuracyRate(0.90)
            .lastUpdated(now)
            .build();
        entry3.setCreatedAt(now.minusDays(3));
        entry3.setUpdatedAt(now);

        entityManager.persist(entry1);
        entityManager.persist(entry2);
        entityManager.persist(entry3);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find leaderboard entry by player, game type, and difficulty")
    void testFindByPlayerIdAndGameTypeAndDifficulty() {
        // When
        Optional<Leaderboard> found = leaderboardRepository
            .findByPlayerIdAndGameTypeAndDifficulty(
                player1.getId(),
                GameType.IDIOM,
                DifficultyLevel.EASY
            );

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getRank()).isEqualTo(1);
        assertThat(found.get().getTotalScore()).isEqualTo(1500);
    }

    @Test
    @DisplayName("Should find top players by game type and difficulty")
    void testFindTopPlayersByGameTypeAndDifficulty() {
        // When
        List<Leaderboard> topPlayers = leaderboardRepository
            .findTopPlayersByGameTypeAndDifficulty(
                GameType.IDIOM,
                DifficultyLevel.EASY,
                10
            );

        // Then
        assertThat(topPlayers).hasSize(3);
        assertThat(topPlayers.get(0).getRank()).isEqualTo(1);
        assertThat(topPlayers.get(0).getPlayer().getUsername()).isEqualTo("玩家001");
        assertThat(topPlayers.get(1).getRank()).isEqualTo(2);
        assertThat(topPlayers.get(2).getRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find all leaderboard entries for a player")
    void testFindByPlayerId() {
        // When
        List<Leaderboard> playerEntries = leaderboardRepository
            .findByPlayerId(player1.getId());

        // Then
        assertThat(playerEntries).hasSize(1);
        assertThat(playerEntries.get(0).getTotalScore()).isEqualTo(1500);
    }

    @Test
    @DisplayName("Should find top 10 entries for a player")
    void testFindTopTenEntriesByPlayer() {
        // When
        List<Leaderboard> topTenEntries = leaderboardRepository
            .findTopTenEntriesByPlayer(player1.getId());

        // Then
        assertThat(topTenEntries).hasSize(1);
        assertThat(topTenEntries.get(0).getRank()).isLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("Should find first place entries for a player")
    void testFindFirstPlaceEntriesByPlayer() {
        // When
        List<Leaderboard> firstPlaceEntries = leaderboardRepository
            .findFirstPlaceEntriesByPlayer(player1.getId());

        // Then
        assertThat(firstPlaceEntries).hasSize(1);
        assertThat(firstPlaceEntries.get(0).getRank()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find players near a specific rank")
    void testFindPlayersNearRank() {
        // When
        List<Leaderboard> nearRank = leaderboardRepository
            .findPlayersNearRank(
                GameType.IDIOM,
                DifficultyLevel.EASY,
                2,
                1 // ±1 position
            );

        // Then
        assertThat(nearRank).hasSize(3); // Ranks 1, 2, 3
    }

    @Test
    @DisplayName("Should count total players on leaderboard")
    void testCountByGameTypeAndDifficulty() {
        // When
        long count = leaderboardRepository.countByGameTypeAndDifficulty(
            GameType.IDIOM,
            DifficultyLevel.EASY
        );

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should update leaderboard entry with new game")
    void testUpdateWithNewGame() {
        // Given
        Leaderboard entry = leaderboardRepository
            .findByPlayerIdAndGameTypeAndDifficulty(
                player1.getId(),
                GameType.IDIOM,
                DifficultyLevel.EASY
            ).orElseThrow();

        int initialScore = entry.getTotalScore();
        int initialGamesPlayed = entry.getGamesPlayed();

        // When
        entry.updateWithNewGame(500, 0.98);
        leaderboardRepository.save(entry);
        entityManager.flush();
        entityManager.clear();

        // Then
        Leaderboard updated = leaderboardRepository.findById(entry.getId()).orElseThrow();
        assertThat(updated.getTotalScore()).isEqualTo(initialScore + 500);
        assertThat(updated.getGamesPlayed()).isEqualTo(initialGamesPlayed + 1);
        assertThat(updated.getAverageScore()).isCloseTo(333.3, org.assertj.core.data.Offset.offset(1.0));
    }

    @Test
    @DisplayName("Should enforce unique constraint on player, game type, and difficulty")
    void testUniqueConstraint() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Leaderboard duplicate = Leaderboard.builder()
            .player(player1)
            .gameType(GameType.IDIOM)
            .difficulty(DifficultyLevel.EASY)
            .totalScore(2000)
            .gamesPlayed(10)
            .averageScore(200.0)
            .rank(1)
            .accuracyRate(0.95)
            .build();
        duplicate.setCreatedAt(now);
        duplicate.setUpdatedAt(now);

        // When/Then - Testing unique constraint violation
        entityManager.persist(duplicate);
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should find entries by game type")
    void testFindByGameType() {
        // When
        List<Leaderboard> idiomEntries = leaderboardRepository.findByGameType(GameType.IDIOM);

        // Then
        assertThat(idiomEntries).hasSize(3);
    }

    @Test
    @DisplayName("Should find entries by difficulty")
    void testFindByDifficulty() {
        // When
        List<Leaderboard> easyEntries = leaderboardRepository
            .findByDifficulty(DifficultyLevel.EASY);

        // Then
        assertThat(easyEntries).hasSize(3);
    }

    @Test
    @DisplayName("Should find top accuracy players")
    void testFindTopAccuracyPlayers() {
        // When
        List<Leaderboard> topAccuracy = leaderboardRepository
            .findTopAccuracyPlayers(
                GameType.IDIOM,
                DifficultyLevel.EASY,
                3, // Min games
                10
            );

        // Then
        assertThat(topAccuracy).hasSize(3);
        assertThat(topAccuracy.get(0).getAccuracyRate()).isGreaterThanOrEqualTo(
            topAccuracy.get(1).getAccuracyRate()
        );
    }

    @Test
    @DisplayName("Should find all for rank recalculation")
    void testFindAllForRankRecalculation() {
        // When
        List<Leaderboard> allEntries = leaderboardRepository
            .findAllForRankRecalculation(
                GameType.IDIOM,
                DifficultyLevel.EASY
            );

        // Then
        assertThat(allEntries).hasSize(3);
        // Should be ordered by totalScore DESC
        assertThat(allEntries.get(0).getTotalScore()).isEqualTo(1500);
        assertThat(allEntries.get(1).getTotalScore()).isEqualTo(1200);
        assertThat(allEntries.get(2).getTotalScore()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Should validate rank is positive")
    void testRankValidation() {
        // Given
        Leaderboard invalidEntry = Leaderboard.builder()
            .player(player1)
            .gameType(GameType.SENTENCE)
            .difficulty(DifficultyLevel.MEDIUM)
            .totalScore(1000)
            .gamesPlayed(5)
            .averageScore(200.0)
            .rank(0) // Invalid: must be >= 1
            .accuracyRate(0.95)
            .build();

        // When/Then - @PrePersist validation throws during persist()
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            entityManager.persist(invalidEntry);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should validate accuracy rate range")
    void testAccuracyRateValidation() {
        // Given
        Leaderboard invalidEntry = Leaderboard.builder()
            .player(player1)
            .gameType(GameType.SENTENCE)
            .difficulty(DifficultyLevel.MEDIUM)
            .totalScore(1000)
            .gamesPlayed(5)
            .averageScore(200.0)
            .rank(1)
            .accuracyRate(1.5) // Invalid: must be <= 1.0
            .build();

        // When/Then - @PrePersist validation throws during persist()
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            entityManager.persist(invalidEntry);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should cascade delete when player is deleted")
    void testCascadeDelete() {
        // Given
        Leaderboard entry = leaderboardRepository
            .findByPlayerIdAndGameTypeAndDifficulty(
                player1.getId(),
                GameType.IDIOM,
                DifficultyLevel.EASY
            ).orElseThrow();
        Long entryId = entry.getId();

        // When
        playerRepository.delete(player1);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Leaderboard> deleted = leaderboardRepository.findById(entryId);
        assertThat(deleted).isEmpty();
    }
}