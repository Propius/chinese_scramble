package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.IdiomScore;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
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
 * Integration tests for IdiomScoreRepository
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("IdiomScoreRepository Integration Tests")
class IdiomScoreRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IdiomScoreRepository idiomScoreRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private Player testPlayer;
    private IdiomScore score1;
    private IdiomScore score2;
    private IdiomScore score3;

    @BeforeEach
    void setUp() {
        idiomScoreRepository.deleteAll();
        playerRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test player with explicit timestamps
        LocalDateTime now = LocalDateTime.now();
        testPlayer = Player.builder()
            .username("玩家001")
            .email("player@test.com")
            .passwordHash("$2a$10$hashedpassword")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        testPlayer.setCreatedAt(now.minusDays(7));
        testPlayer.setUpdatedAt(now);
        entityManager.persist(testPlayer);

        // Create test scores with explicit timestamps
        score1 = IdiomScore.builder()
            .player(testPlayer)
            .idiom("一马当先")
            .score(450)
            .difficulty(DifficultyLevel.EASY)
            .timeTaken(45)
            .hintsUsed(0)
            .accuracyRate(1.0)
            .completed(true)
            .build();
        score1.setCreatedAt(now.minusHours(3));
        score1.setUpdatedAt(now.minusHours(3));

        score2 = IdiomScore.builder()
            .player(testPlayer)
            .idiom("井底之蛙")
            .score(720)
            .difficulty(DifficultyLevel.HARD)
            .timeTaken(95)
            .hintsUsed(1)
            .accuracyRate(0.92)
            .completed(true)
            .build();
        score2.setCreatedAt(now.minusHours(2));
        score2.setUpdatedAt(now.minusHours(2));

        score3 = IdiomScore.builder()
            .player(testPlayer)
            .idiom("画蛇添足")
            .score(520)
            .difficulty(DifficultyLevel.MEDIUM)
            .timeTaken(65)
            .hintsUsed(0)
            .accuracyRate(1.0)
            .completed(true)
            .build();
        score3.setCreatedAt(now.minusHours(1));
        score3.setUpdatedAt(now.minusHours(1));

        entityManager.persist(score1);
        entityManager.persist(score2);
        entityManager.persist(score3);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find all scores for a player")
    void testFindByPlayerId() {
        // When
        List<IdiomScore> scores = idiomScoreRepository.findByPlayerId(testPlayer.getId());

        // Then
        assertThat(scores).hasSize(3);
        // Should be ordered by playedAt DESC
        assertThat(scores.get(0).getIdiom()).isEqualTo("画蛇添足"); // Most recent
    }

    @Test
    @DisplayName("Should find scores by player and difficulty")
    void testFindByPlayerIdAndDifficulty() {
        // When
        List<IdiomScore> easyScores = idiomScoreRepository
            .findByPlayerIdAndDifficulty(testPlayer.getId(), DifficultyLevel.EASY);
        List<IdiomScore> hardScores = idiomScoreRepository
            .findByPlayerIdAndDifficulty(testPlayer.getId(), DifficultyLevel.HARD);

        // Then
        assertThat(easyScores).hasSize(1);
        assertThat(easyScores.get(0).getIdiom()).isEqualTo("一马当先");
        assertThat(hardScores).hasSize(1);
        assertThat(hardScores.get(0).getScore()).isEqualTo(720);
    }

    @Test
    @DisplayName("Should find top scores by difficulty")
    void testFindTopScoresByDifficulty() {
        // When
        List<IdiomScore> topEasy = idiomScoreRepository
            .findTopScoresByDifficulty(DifficultyLevel.EASY, 10);

        // Then
        assertThat(topEasy).hasSize(1);
        assertThat(topEasy.get(0).getScore()).isEqualTo(450);
    }

    @Test
    @DisplayName("Should find personal best score")
    void testFindPersonalBest() {
        // When
        Optional<IdiomScore> personalBest = idiomScoreRepository
            .findPersonalBest(testPlayer.getId(), DifficultyLevel.MEDIUM);

        // Then
        assertThat(personalBest).isPresent();
        assertThat(personalBest.get().getScore()).isEqualTo(520);
        assertThat(personalBest.get().getIdiom()).isEqualTo("画蛇添足");
    }

    @Test
    @DisplayName("Should find perfect scores")
    void testFindPerfectScores() {
        // When
        List<IdiomScore> perfectScores = idiomScoreRepository
            .findPerfectScores(testPlayer.getId());

        // Then
        assertThat(perfectScores).hasSize(2); // score1 and score3 have 100% accuracy and 0 hints
        assertThat(perfectScores)
            .extracting(IdiomScore::getAccuracyRate)
            .containsOnly(1.0);
        assertThat(perfectScores)
            .extracting(IdiomScore::getHintsUsed)
            .containsOnly(0);
    }

    @Test
    @DisplayName("Should find speed demon scores")
    void testFindSpeedDemonScores() {
        // When
        List<IdiomScore> speedScores = idiomScoreRepository
            .findSpeedDemonScores(testPlayer.getId(), 50); // Under 50 seconds

        // Then
        assertThat(speedScores).hasSize(1);
        assertThat(speedScores.get(0).getTimeTaken()).isLessThan(50);
        assertThat(speedScores.get(0).getIdiom()).isEqualTo("一马当先");
    }

    @Test
    @DisplayName("Should count completed games")
    void testCountCompletedGamesByPlayer() {
        // When
        long count = idiomScoreRepository.countCompletedGamesByPlayer(testPlayer.getId());

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should count hint-free games")
    void testCountHintFreeGamesByPlayer() {
        // When
        long count = idiomScoreRepository.countHintFreeGamesByPlayer(testPlayer.getId());

        // Then
        assertThat(count).isEqualTo(2); // score1 and score3
    }

    @Test
    @DisplayName("Should calculate average score")
    void testCalculateAverageScore() {
        // When
        Optional<Double> avgScore = idiomScoreRepository
            .calculateAverageScore(testPlayer.getId(), DifficultyLevel.MEDIUM);

        // Then
        assertThat(avgScore).isPresent();
        assertThat(avgScore.get()).isEqualTo(520.0);
    }

    @Test
    @DisplayName("Should calculate average accuracy")
    void testCalculateAverageAccuracy() {
        // When
        Optional<Double> avgAccuracy = idiomScoreRepository
            .calculateAverageAccuracy(testPlayer.getId());

        // Then
        assertThat(avgAccuracy).isPresent();
        assertThat(avgAccuracy.get()).isCloseTo(0.973, org.assertj.core.data.Offset.offset(0.01));
        // (1.0 + 0.92 + 1.0) / 3 = 0.973
    }

    @Test
    @DisplayName("Should find scores in date range")
    void testFindScoresInDateRange() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusHours(6);
        LocalDateTime end = LocalDateTime.now();

        // When
        List<IdiomScore> scoresInRange = idiomScoreRepository
            .findScoresInDateRange(testPlayer.getId(), start, end);

        // Then
        // All 3 scores (score1=-3h, score2=-2h, score3=-1h) are within last 6 hours
        assertThat(scoresInRange).hasSize(3);
    }

    @Test
    @DisplayName("Should find recent scores")
    void testFindRecentScores() {
        // When
        List<IdiomScore> recentScores = idiomScoreRepository
            .findRecentScores(testPlayer.getId());

        // Then
        assertThat(recentScores).hasSize(3);
        // Should be ordered by playedAt DESC
        assertThat(recentScores.get(0).getIdiom()).isEqualTo("画蛇添足");
        assertThat(recentScores.get(1).getIdiom()).isEqualTo("井底之蛙");
        assertThat(recentScores.get(2).getIdiom()).isEqualTo("一马当先");
    }

    @Test
    @DisplayName("Should calculate total score")
    void testCalculateTotalScore() {
        // When
        Optional<Long> totalScore = idiomScoreRepository
            .calculateTotalScore(testPlayer.getId(), DifficultyLevel.MEDIUM);

        // Then
        assertThat(totalScore).isPresent();
        assertThat(totalScore.get()).isEqualTo(520L);
    }

    @Test
    @DisplayName("Should save idiom score with Chinese characters")
    void testSaveIdiomScoreWithChineseCharacters() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        IdiomScore newScore = IdiomScore.builder()
            .player(testPlayer)
            .idiom("守株待兔")
            .score(480)
            .difficulty(DifficultyLevel.EASY)
            .timeTaken(52)
            .hintsUsed(1)
            .accuracyRate(0.95)
            .completed(true)
            .build();
        newScore.setCreatedAt(now);
        newScore.setUpdatedAt(now);

        // When
        IdiomScore saved = idiomScoreRepository.save(newScore);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<IdiomScore> found = idiomScoreRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getIdiom()).isEqualTo("守株待兔");
    }

    @Test
    @DisplayName("Should validate hints constraint (0-3)")
    void testHintsConstraint() {
        // Given
        IdiomScore invalidScore = IdiomScore.builder()
            .player(testPlayer)
            .idiom("测试成语")
            .score(500)
            .difficulty(DifficultyLevel.EASY)
            .timeTaken(60)
            .hintsUsed(4) // Invalid: > 3
            .accuracyRate(1.0)
            .completed(true)
            .build();

        // When/Then - @PrePersist validation throws during persist()
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            entityManager.persist(invalidScore);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should validate accuracy rate constraint (0.0-1.0)")
    void testAccuracyRateConstraint() {
        // Given
        IdiomScore invalidScore = IdiomScore.builder()
            .player(testPlayer)
            .idiom("测试成语")
            .score(500)
            .difficulty(DifficultyLevel.EASY)
            .timeTaken(60)
            .hintsUsed(0)
            .accuracyRate(1.5) // Invalid: > 1.0
            .completed(true)
            .build();

        // When/Then - @PrePersist validation throws during persist()
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            entityManager.persist(invalidScore);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should cascade delete when player is deleted")
    void testCascadeDelete() {
        // Given
        Long scoreId = score1.getId();

        // When
        playerRepository.delete(testPlayer);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<IdiomScore> deleted = idiomScoreRepository.findById(scoreId);
        assertThat(deleted).isEmpty();
    }
}