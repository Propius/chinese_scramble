package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.UserRole;
import com.govtech.chinesescramble.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for PlayerService
 * TODO: Fix tests - several methods have wrong signatures/assumptions
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
            .hasMessageContaining("Email already exists");

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

    // Helper method

    private Player createPlayer(Long id, String username) {
        Player player = Player.builder()
            .username(username)
            .email(username + "@test.com")
            .passwordHash("hashedPassword")
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
}
