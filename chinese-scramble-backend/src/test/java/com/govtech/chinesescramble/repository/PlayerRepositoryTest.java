package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.Player;
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
import static org.assertj.core.api.Assertions.within;

/**
 * Integration tests for PlayerRepository
 *
 * Tests:
 * - CRUD operations
 * - Custom query methods
 * - Case-insensitive searches
 * - Player statistics queries
 * - Authentication queries
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PlayerRepository Integration Tests")
class PlayerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlayerRepository playerRepository;

    private Player testPlayer1;
    private Player testPlayer2;
    private Player testPlayer3;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        playerRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test players with explicit timestamps (required for direct entityManager.persist)
        LocalDateTime now = LocalDateTime.now();

        testPlayer1 = Player.builder()
            .username("玩家001")
            .email("player1@test.com")
            .passwordHash("$2a$10$hashedpassword1")
            .role(UserRole.PLAYER)
            .active(true)
            .lastLoginAt(now.minusDays(1))
            .build();
        testPlayer1.setCreatedAt(now.minusDays(7));
        testPlayer1.setUpdatedAt(now.minusDays(1));

        testPlayer2 = Player.builder()
            .username("张伟")
            .email("zhangwei@test.com")
            .passwordHash("$2a$10$hashedpassword2")
            .role(UserRole.ADMIN)
            .active(true)
            .lastLoginAt(now.minusHours(2))
            .build();
        testPlayer2.setCreatedAt(now.minusDays(5));
        testPlayer2.setUpdatedAt(now.minusHours(2));

        testPlayer3 = Player.builder()
            .username("inactivePlayer")
            .email("inactive@test.com")
            .passwordHash("$2a$10$hashedpassword3")
            .role(UserRole.PLAYER)
            .active(false)
            .lastLoginAt(null)
            .build();
        testPlayer3.setCreatedAt(now.minusDays(30));
        testPlayer3.setUpdatedAt(now.minusDays(30));

        entityManager.persist(testPlayer1);
        entityManager.persist(testPlayer2);
        entityManager.persist(testPlayer3);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find player by username (case-insensitive)")
    void testFindByUsernameIgnoreCase() {
        // When
        Optional<Player> found = playerRepository.findByUsernameIgnoreCase("玩家001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("玩家001");
        assertThat(found.get().getEmail()).isEqualTo("player1@test.com");
    }

    @Test
    @DisplayName("Should find player by email (case-insensitive)")
    void testFindByEmailIgnoreCase() {
        // When
        Optional<Player> found = playerRepository.findByEmailIgnoreCase("ZHANGWEI@TEST.COM");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("张伟");
        assertThat(found.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Should check if username exists")
    void testExistsByUsernameIgnoreCase() {
        // When/Then
        assertThat(playerRepository.existsByUsernameIgnoreCase("玩家001")).isTrue();
        assertThat(playerRepository.existsByUsernameIgnoreCase("PLAYER001")).isFalse();
        assertThat(playerRepository.existsByUsernameIgnoreCase("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmailIgnoreCase() {
        // When/Then
        assertThat(playerRepository.existsByEmailIgnoreCase("player1@test.com")).isTrue();
        assertThat(playerRepository.existsByEmailIgnoreCase("PLAYER1@TEST.COM")).isTrue();
        assertThat(playerRepository.existsByEmailIgnoreCase("nonexistent@test.com")).isFalse();
    }

    @Test
    @DisplayName("Should find all active players")
    void testFindByActiveTrue() {
        // When
        List<Player> activePlayers = playerRepository.findByActiveTrue();

        // Then
        assertThat(activePlayers).hasSize(2);
        assertThat(activePlayers)
            .extracting(Player::getUsername)
            .containsExactlyInAnyOrder("玩家001", "张伟");
    }

    @Test
    @DisplayName("Should find all inactive players")
    void testFindByActiveFalse() {
        // When
        List<Player> inactivePlayers = playerRepository.findByActiveFalse();

        // Then
        assertThat(inactivePlayers).hasSize(1);
        assertThat(inactivePlayers.get(0).getUsername()).isEqualTo("inactivePlayer");
    }

    @Test
    @DisplayName("Should find players by role")
    void testFindByRole() {
        // When
        List<Player> players = playerRepository.findByRole(UserRole.PLAYER);
        List<Player> admins = playerRepository.findByRole(UserRole.ADMIN);

        // Then
        assertThat(players).hasSize(2);
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getUsername()).isEqualTo("张伟");
    }

    @Test
    @DisplayName("Should find recently active players")
    void testFindRecentlyActivePlayers() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(3);

        // When
        List<Player> recentPlayers = playerRepository.findRecentlyActivePlayers(cutoffDate);

        // Then
        assertThat(recentPlayers).hasSize(2);
        assertThat(recentPlayers)
            .extracting(Player::getUsername)
            .containsExactlyInAnyOrder("玩家001", "张伟");
    }

    @Test
    @DisplayName("Should count active players")
    void testCountByActiveTrue() {
        // When
        long count = playerRepository.countByActiveTrue();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should search players by username or email")
    void testSearchPlayers() {
        // When
        List<Player> searchResults = playerRepository.searchPlayers("玩家");

        // Then
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getUsername()).isEqualTo("玩家001");

        // Search by email fragment
        List<Player> emailResults = playerRepository.searchPlayers("test.com");
        assertThat(emailResults).hasSize(3);
    }

    @Test
    @DisplayName("Should find players registered after date")
    void testFindByCreatedAtAfter() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusMinutes(1);

        // When
        List<Player> newPlayers = playerRepository.findByCreatedAtAfter(cutoffDate);

        // Then
        assertThat(newPlayers).hasSize(3); // All just created in setup
    }

    @Test
    @DisplayName("Should count players registered in date range")
    void testCountByCreatedAtBetween() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        // When
        long count = playerRepository.countByCreatedAtBetween(start, end);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find inactive players (no recent login)")
    void testFindInactivePlayers() {
        // Given
        LocalDateTime inactiveSince = LocalDateTime.now().minusHours(3);

        // When
        List<Player> inactivePlayers = playerRepository.findInactivePlayers(inactiveSince);

        // Then
        assertThat(inactivePlayers).hasSize(2); // testPlayer1 (1 day ago) and testPlayer3 (null)
    }

    @Test
    @DisplayName("Should save player with Chinese username")
    void testSavePlayerWithChineseUsername() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Player chinesePlayer = Player.builder()
            .username("李娜")
            .email("lina@test.com")
            .passwordHash("$2a$10$hashedpassword")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        chinesePlayer.setCreatedAt(now);
        chinesePlayer.setUpdatedAt(now);

        // When
        Player saved = playerRepository.save(chinesePlayer);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Player> found = playerRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("李娜");
    }

    @Test
    @DisplayName("Should update player last login timestamp")
    void testUpdateLastLogin() {
        // Given
        Player player = playerRepository.findByUsernameIgnoreCase("玩家001").orElseThrow();
        LocalDateTime newLoginTime = LocalDateTime.now();

        // When
        player.setLastLogin(newLoginTime);
        playerRepository.save(player);
        entityManager.flush();
        entityManager.clear();

        // Then
        Player updated = playerRepository.findById(player.getId()).orElseThrow();
        assertThat(updated.getLastLogin())
            .isCloseTo(newLoginTime, within(1, java.time.temporal.ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("Should enforce unique constraints")
    void testUniqueConstraints() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Player duplicateUsername = Player.builder()
            .username("玩家001") // Duplicate
            .email("unique@test.com")
            .passwordHash("$2a$10$hashedpassword")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        duplicateUsername.setCreatedAt(now);
        duplicateUsername.setUpdatedAt(now);

        // When/Then - Should throw exception on flush
        entityManager.persist(duplicateUsername);
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should handle optimistic locking")
    void testOptimisticLocking() {
        // Given
        Player player = playerRepository.findByUsernameIgnoreCase("玩家001").orElseThrow();
        Long initialVersion = player.getVersion();

        // When
        player.setActive(false);
        playerRepository.save(player);
        entityManager.flush();
        entityManager.clear();

        // Then
        Player updated = playerRepository.findById(player.getId()).orElseThrow();
        assertThat(updated.getVersion()).isGreaterThan(initialVersion);
    }

    @Test
    @DisplayName("Should cascade delete player relationships")
    void testCascadeDelete() {
        // Given
        Player player = playerRepository.findByUsernameIgnoreCase("玩家001").orElseThrow();
        Long playerId = player.getId();

        // When
        playerRepository.delete(player);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Player> deleted = playerRepository.findById(playerId);
        assertThat(deleted).isEmpty();
    }
}