package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.Leaderboard;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.entity.enums.UserRole;
import com.govtech.chinesescramble.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for PlayerService
 * Tests cover all main methods:
 * - Registration and authentication
 * - Profile and password management
 * - Account activation/deactivation
 * - Player statistics
 * - Player search and queries
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private IdiomScoreRepository idiomScoreRepository;

    @Mock
    private SentenceScoreRepository sentenceScoreRepository;

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        reset(playerRepository, idiomScoreRepository, sentenceScoreRepository,
              achievementRepository, leaderboardRepository, passwordEncoder);
    }

    @Test
    void testRegisterPlayer_Success() {
        // Given
        String username = "newplayer";
        String email = "new@test.com";
        String password = "password123";
        String hashedPassword = "hashedPassword";

        when(playerRepository.existsByUsernameIgnoreCase(username)).thenReturn(false);
        when(playerRepository.existsByEmailIgnoreCase(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        when(playerRepository.save(any(Player.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Player player = playerService.registerPlayer(username, email, password);

        // Then
        assertThat(player).isNotNull();
        assertThat(player.getUsername()).isEqualTo(username);
        assertThat(player.getEmail()).isEqualTo(email);
        assertThat(player.getPasswordHash()).isEqualTo(hashedPassword);
        assertThat(player.getRole()).isEqualTo(UserRole.PLAYER);
        assertThat(player.isActive()).isTrue();
        verify(passwordEncoder).encode(password);
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void testRegisterPlayer_UsernameAlreadyExists() {
        // Given
        String username = "existing";
        String email = "new@test.com";
        String password = "password123";

        when(playerRepository.existsByUsernameIgnoreCase(username)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() ->
            playerService.registerPlayer(username, email, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username already exists");

        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void testRegisterPlayer_EmailAlreadyExists() {
        // Given
        String username = "newplayer";
        String email = "existing@test.com";
        String password = "password123";

        when(playerRepository.existsByUsernameIgnoreCase(username)).thenReturn(false);
        when(playerRepository.existsByEmailIgnoreCase(email)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() ->
            playerService.registerPlayer(username, email, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already registered");

        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void testGetPlayerById() {
        // Given
        Long playerId = 1L;
        Player player = createPlayer(playerId, "testuser");

        when(playerRepository.findById(playerId))
            .thenReturn(Optional.of(player));

        // When
        Optional<Player> result = playerService.getPlayerById(playerId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(playerId);
    }

    @Test
    void testDeactivateAccount() {
        // Given
        Long playerId = 1L;
        Player player = createPlayer(playerId, "testuser");

        when(playerRepository.findById(playerId))
            .thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        playerService.deactivateAccount(playerId);

        // Then
        verify(playerRepository).save(argThat(p -> !p.isActive()));
    }

    @Test
    void testReactivateAccount() {
        // Given
        Long playerId = 1L;
        Player player = createPlayer(playerId, "testuser");
        player.setActive(false);

        when(playerRepository.findById(playerId))
            .thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        playerService.reactivateAccount(playerId);

        // Then
        verify(playerRepository).save(argThat(Player::isActive));
    }

    @Test
    void testRegisterPlayer_PasswordTooShort() {
        // Given
        String username = "newplayer";
        String email = "new@test.com";
        String password = "short";

        when(playerRepository.existsByUsernameIgnoreCase(username)).thenReturn(false);
        when(playerRepository.existsByEmailIgnoreCase(email)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() ->
            playerService.registerPlayer(username, email, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least 8 characters");

        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void testAuthenticatePlayer_SuccessByUsername() {
        // Given
        String username = "testuser";
        String password = "password123";
        Player player = createPlayer(1L, username);

        when(playerRepository.findByUsernameIgnoreCase(username))
            .thenReturn(Optional.of(player));
        when(passwordEncoder.matches(password, player.getPasswordHash()))
            .thenReturn(true);

        // When
        Optional<Player> result = playerService.authenticatePlayer(username, password);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
    }

    @Test
    void testAuthenticatePlayer_SuccessByEmail() {
        // Given
        String email = "testuser@test.com";
        String password = "password123";
        Player player = createPlayer(1L, "testuser");

        when(playerRepository.findByUsernameIgnoreCase(email))
            .thenReturn(Optional.empty());
        when(playerRepository.findByEmailIgnoreCase(email))
            .thenReturn(Optional.of(player));
        when(passwordEncoder.matches(password, player.getPasswordHash()))
            .thenReturn(true);

        // When
        Optional<Player> result = playerService.authenticatePlayer(email, password);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
    }

    @Test
    void testAuthenticatePlayer_FailureWrongPassword() {
        // Given
        String username = "testuser";
        String password = "wrongpassword";
        Player player = createPlayer(1L, username);

        when(playerRepository.findByUsernameIgnoreCase(username))
            .thenReturn(Optional.of(player));
        when(passwordEncoder.matches(password, player.getPasswordHash()))
            .thenReturn(false);

        // When
        Optional<Player> result = playerService.authenticatePlayer(username, password);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testUpdateLastLogin() {
        // Given
        Long playerId = 1L;
        Player player = createPlayer(playerId, "testuser");

        when(playerRepository.findById(playerId))
            .thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        playerService.updateLastLogin(playerId);

        // Then
        verify(playerRepository).save(argThat(p -> p.getLastLogin() != null));
    }

    @Test
    void testUpdateProfile_Success() {
        // Given
        Long playerId = 1L;
        Player player = createPlayer(playerId, "testuser");
        String newEmail = "newemail@test.com";

        when(playerRepository.findById(playerId))
            .thenReturn(Optional.of(player));
        when(playerRepository.existsByEmailIgnoreCase(newEmail))
            .thenReturn(false);
        when(playerRepository.save(any(Player.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Player result = playerService.updateProfile(playerId, newEmail);

        // Then
        assertThat(result.getEmail()).isEqualTo(newEmail);
        verify(playerRepository).save(player);
    }

    @Test
    void testUpdateProfile_EmailAlreadyExists() {
        // Given
        Long playerId = 1L;
        Player player = createPlayer(playerId, "testuser");
        String newEmail = "existing@test.com";

        when(playerRepository.findById(playerId))
            .thenReturn(Optional.of(player));
        when(playerRepository.existsByEmailIgnoreCase(newEmail))
            .thenReturn(true);

        // When/Then
        assertThatThrownBy(() ->
            playerService.updateProfile(playerId, newEmail))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already registered");
    }

    @Test
    void testChangePassword_Success() {
        // Given
        Long playerId = 1L;
        Player player = createPlayer(playerId, "testuser");
        String oldPassword = "oldpassword";
        String newPassword = "newpassword123";
        String newHashedPassword = "newHashedPassword";

        when(playerRepository.findById(playerId))
            .thenReturn(Optional.of(player));
        when(passwordEncoder.matches(oldPassword, player.getPasswordHash()))
            .thenReturn(true);
        when(passwordEncoder.encode(newPassword))
            .thenReturn(newHashedPassword);
        when(playerRepository.save(any(Player.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Player result = playerService.changePassword(playerId, oldPassword, newPassword);

        // Then
        assertThat(result.getPasswordHash()).isEqualTo(newHashedPassword);
        verify(passwordEncoder).encode(newPassword);
        verify(playerRepository).save(player);
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        // Given
        Long playerId = 1L;
        Player player = createPlayer(playerId, "testuser");
        String oldPassword = "wrongpassword";
        String newPassword = "newpassword123";

        when(playerRepository.findById(playerId))
            .thenReturn(Optional.of(player));
        when(passwordEncoder.matches(oldPassword, player.getPasswordHash()))
            .thenReturn(false);

        // When/Then
        assertThatThrownBy(() ->
            playerService.changePassword(playerId, oldPassword, newPassword))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Current password is incorrect");
    }

    @Test
    void testChangePassword_NewPasswordTooShort() {
        // Given
        Long playerId = 1L;
        Player player = createPlayer(playerId, "testuser");
        String oldPassword = "oldpassword";
        String newPassword = "short";

        when(playerRepository.findById(playerId))
            .thenReturn(Optional.of(player));
        when(passwordEncoder.matches(oldPassword, player.getPasswordHash()))
            .thenReturn(true);

        // When/Then
        assertThatThrownBy(() ->
            playerService.changePassword(playerId, oldPassword, newPassword))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least 8 characters");
    }

    @Test
    void testGetPlayerByUsername() {
        // Given
        String username = "testuser";
        Player player = createPlayer(1L, username);

        when(playerRepository.findByUsernameIgnoreCase(username))
            .thenReturn(Optional.of(player));

        // When
        Optional<Player> result = playerService.getPlayerByUsername(username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
    }

    @Test
    void testGetPlayerStatistics() {
        // Given
        Long playerId = 1L;
        Player player = createPlayer(playerId, "testuser");

        Leaderboard leaderboard1 = Leaderboard.builder()
            .player(player)
            .gameType(GameType.IDIOM)
            .rank(5)
            .totalScore(1000)
            .build();

        Leaderboard leaderboard2 = Leaderboard.builder()
            .player(player)
            .gameType(GameType.SENTENCE)
            .rank(10)
            .totalScore(800)
            .build();

        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(10L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(5L);
        when(idiomScoreRepository.calculateTotalScore(playerId, null)).thenReturn(Optional.of(1000L));
        when(sentenceScoreRepository.calculateTotalScore(playerId, null)).thenReturn(Optional.of(500L));
        when(idiomScoreRepository.calculateAverageAccuracy(playerId)).thenReturn(Optional.of(0.85));
        when(sentenceScoreRepository.calculateAverageAccuracy(playerId)).thenReturn(Optional.of(0.75));
        when(achievementRepository.countByPlayerId(playerId)).thenReturn(5L);
        when(leaderboardRepository.findByPlayerId(playerId))
            .thenReturn(Arrays.asList(leaderboard1, leaderboard2));

        // When
        PlayerService.PlayerStatistics result = playerService.getPlayerStatistics(playerId);

        // Then
        assertThat(result.totalGames()).isEqualTo(15L);
        assertThat(result.idiomGames()).isEqualTo(10L);
        assertThat(result.sentenceGames()).isEqualTo(5L);
        assertThat(result.totalScore()).isEqualTo(1500L);
        assertThat(result.achievementCount()).isEqualTo(5L);
        assertThat(result.bestRank()).isEqualTo(5);
        assertThat(result.leaderboardCount()).isEqualTo(2);
    }

    @Test
    void testGetPlayerStatistics_NoGamesPlayed() {
        // Given
        Long playerId = 1L;

        when(idiomScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);
        when(sentenceScoreRepository.countCompletedGamesByPlayer(playerId)).thenReturn(0L);
        when(idiomScoreRepository.calculateTotalScore(playerId, null)).thenReturn(Optional.empty());
        when(sentenceScoreRepository.calculateTotalScore(playerId, null)).thenReturn(Optional.empty());
        when(idiomScoreRepository.calculateAverageAccuracy(playerId)).thenReturn(Optional.empty());
        when(sentenceScoreRepository.calculateAverageAccuracy(playerId)).thenReturn(Optional.empty());
        when(achievementRepository.countByPlayerId(playerId)).thenReturn(0L);
        when(leaderboardRepository.findByPlayerId(playerId)).thenReturn(Collections.emptyList());

        // When
        PlayerService.PlayerStatistics result = playerService.getPlayerStatistics(playerId);

        // Then
        assertThat(result.totalGames()).isEqualTo(0L);
        assertThat(result.totalScore()).isEqualTo(0L);
        assertThat(result.overallAccuracy()).isEqualTo(0.0);
        assertThat(result.bestRank()).isNull();
    }

    @Test
    void testGetActivePlayers() {
        // Given
        Player player1 = createPlayer(1L, "user1");
        Player player2 = createPlayer(2L, "user2");

        when(playerRepository.findByActiveTrue())
            .thenReturn(Arrays.asList(player1, player2));

        // When
        List<Player> result = playerService.getActivePlayers();

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    void testSearchPlayers() {
        // Given
        String searchTerm = "test";
        Player player1 = createPlayer(1L, "testuser");

        when(playerRepository.searchPlayers(searchTerm))
            .thenReturn(Arrays.asList(player1));

        // When
        List<Player> result = playerService.searchPlayers(searchTerm);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).contains("test");
    }

    @Test
    void testGetRecentlyActivePlayers() {
        // Given
        Player player1 = createPlayer(1L, "activeuser");

        when(playerRepository.findRecentlyActivePlayers(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(player1));

        // When
        List<Player> result = playerService.getRecentlyActivePlayers();

        // Then
        assertThat(result).hasSize(1);
        verify(playerRepository).findRecentlyActivePlayers(any(LocalDateTime.class));
    }

    @Test
    void testGetInactivePlayers() {
        // Given
        Player player1 = createPlayer(1L, "inactiveuser");

        when(playerRepository.findInactivePlayers(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(player1));

        // When
        List<Player> result = playerService.getInactivePlayers();

        // Then
        assertThat(result).hasSize(1);
        verify(playerRepository).findInactivePlayers(any(LocalDateTime.class));
    }

    @Test
    void testGetTotalPlayerCount() {
        // Given
        when(playerRepository.count()).thenReturn(100L);

        // When
        long result = playerService.getTotalPlayerCount();

        // Then
        assertThat(result).isEqualTo(100L);
    }

    @Test
    void testGetActivePlayerCount() {
        // Given
        when(playerRepository.countByActiveTrue()).thenReturn(80L);

        // When
        long result = playerService.getActivePlayerCount();

        // Then
        assertThat(result).isEqualTo(80L);
    }

    @Test
    void testClearPlayerCache() {
        // Given
        Long playerId = 1L;

        // When
        playerService.clearPlayerCache(playerId);

        // Then
        // Just verify it doesn't throw exception
        // Cache eviction is handled by Spring AOP
    }

    @Test
    void testClearAllPlayerCaches() {
        // When
        playerService.clearAllPlayerCaches();

        // Then
        // Just verify it doesn't throw exception
        // Cache eviction is handled by Spring AOP
    }

    @Test
    void testPlayerStatistics_AccuracyPercentage() {
        // Given
        PlayerService.PlayerStatistics stats = new PlayerService.PlayerStatistics(
            10L, 6L, 4L, 1000L, 0.85, 5L, 1, 2
        );

        // When
        double result = stats.accuracyPercentage();

        // Then
        assertThat(result).isEqualTo(85.0);
    }

    @Test
    void testPlayerStatistics_AverageScorePerGame() {
        // Given
        PlayerService.PlayerStatistics stats = new PlayerService.PlayerStatistics(
            10L, 6L, 4L, 1000L, 0.85, 5L, 1, 2
        );

        // When
        double result = stats.averageScorePerGame();

        // Then
        assertThat(result).isEqualTo(100.0);
    }

    @Test
    void testPlayerStatistics_HasTopRank() {
        // Given
        PlayerService.PlayerStatistics stats1 = new PlayerService.PlayerStatistics(
            10L, 6L, 4L, 1000L, 0.85, 5L, 5, 2
        );
        PlayerService.PlayerStatistics stats2 = new PlayerService.PlayerStatistics(
            10L, 6L, 4L, 1000L, 0.85, 5L, 15, 2
        );
        PlayerService.PlayerStatistics stats3 = new PlayerService.PlayerStatistics(
            10L, 6L, 4L, 1000L, 0.85, 5L, null, 2
        );

        // Then
        assertThat(stats1.hasTopRank()).isTrue();
        assertThat(stats2.hasTopRank()).isFalse();
        assertThat(stats3.hasTopRank()).isFalse();
    }

    // Helper method

    private Player createPlayer(Long id, String username) {
        Player player = Player.builder()
            .username(username)
            .email(username + "@test.com")
            .passwordHash("hashedPassword")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        ReflectionTestUtils.setField(player, "id", id);
        return player;
    }
}
