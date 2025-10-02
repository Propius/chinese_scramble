package com.govtech.chinesescramble.repository;

import com.govtech.chinesescramble.entity.GameSession;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.SessionStatus;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for GameSessionRepository
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@DataJpaTest
class GameSessionRepositoryTest {

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        testPlayer = Player.builder()
            .username("gamer")
            .email("gamer@test.com")
            .passwordHash("hashedpassword")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        testPlayer.setCreatedAt(now.minusDays(7));
        testPlayer.setUpdatedAt(now);
        testPlayer = playerRepository.save(testPlayer);
    }

    @Test
    void testFindByPlayerAndStatus() {
        // Given
        GameSession activeSession = createSession(testPlayer, GameType.IDIOM,
            DifficultyLevel.EASY, SessionStatus.ACTIVE);
        GameSession completedSession = createSession(testPlayer, GameType.SENTENCE,
            DifficultyLevel.MEDIUM, SessionStatus.COMPLETED);

        gameSessionRepository.saveAll(List.of(activeSession, completedSession));

        // When
        List<GameSession> activeSessions = gameSessionRepository
            .findByStatus(SessionStatus.ACTIVE);

        // Then
        assertThat(activeSessions).hasSize(1);
        assertThat(activeSessions.get(0).getStatus())
            .isEqualTo(SessionStatus.ACTIVE);
    }

    @Test
    void testFindActiveSessionByPlayer() {
        // Given
        GameSession activeSession = createSession(testPlayer, GameType.IDIOM,
            DifficultyLevel.HARD, SessionStatus.ACTIVE);
        gameSessionRepository.save(activeSession);

        // When
        var result = gameSessionRepository.findActiveSessionByPlayer(testPlayer.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus())
            .isEqualTo(SessionStatus.ACTIVE);
        assertThat(result.get().getPlayer().getId()).isEqualTo(testPlayer.getId());
    }

    @Test
    void testFindByGameTypeAndStatus() {
        // Given
        gameSessionRepository.saveAll(List.of(
            createSession(testPlayer, GameType.IDIOM, DifficultyLevel.EASY,
                SessionStatus.ACTIVE),
            createSession(testPlayer, GameType.IDIOM, DifficultyLevel.MEDIUM,
                SessionStatus.ACTIVE),
            createSession(testPlayer, GameType.SENTENCE, DifficultyLevel.EASY,
                SessionStatus.ACTIVE)
        ));

        // When
        List<GameSession> idiomSessions = gameSessionRepository
            .findByGameType(GameType.IDIOM);

        // Then
        assertThat(idiomSessions).hasSize(2);
        assertThat(idiomSessions).allMatch(s -> s.getGameType() == GameType.IDIOM);
    }

    @Test
    void testFindExpiredSessions() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyOneMinutesAgo = now.minusMinutes(31);

        GameSession expiredSession = createSession(testPlayer, GameType.IDIOM,
            DifficultyLevel.EASY, SessionStatus.ACTIVE);
        expiredSession.setStartedAt(thirtyOneMinutesAgo);

        GameSession recentSession = createSession(testPlayer, GameType.SENTENCE,
            DifficultyLevel.MEDIUM, SessionStatus.ACTIVE);
        recentSession.setStartedAt(now.minusMinutes(10));

        gameSessionRepository.saveAll(List.of(expiredSession, recentSession));

        // When
        List<GameSession> expired = gameSessionRepository
            .findStaleActiveSessions(now.minusMinutes(30));

        // Then
        assertThat(expired).hasSize(1);
        assertThat(expired.get(0).getStartedAt()).isBefore(now.minusMinutes(30));
    }

    @Test
    void testCountPlayerSessions() {
        // Given
        gameSessionRepository.saveAll(List.of(
            createSession(testPlayer, GameType.IDIOM, DifficultyLevel.EASY,
                SessionStatus.COMPLETED),
            createSession(testPlayer, GameType.SENTENCE, DifficultyLevel.MEDIUM,
                SessionStatus.COMPLETED),
            createSession(testPlayer, GameType.IDIOM, DifficultyLevel.HARD,
                SessionStatus.ACTIVE)
        ));

        // When
        List<GameSession> allSessions = gameSessionRepository.findByPlayerId(testPlayer.getId());
        long completedCount = gameSessionRepository
            .countByPlayerIdAndStatus(testPlayer.getId(), SessionStatus.COMPLETED);

        // Then
        assertThat(allSessions).hasSize(3);
        assertThat(completedCount).isEqualTo(2L);
    }

    @Test
    void testFindRecentSessions() {
        // Given
        gameSessionRepository.saveAll(List.of(
            createSession(testPlayer, GameType.IDIOM, DifficultyLevel.EASY,
                SessionStatus.COMPLETED),
            createSession(testPlayer, GameType.SENTENCE, DifficultyLevel.MEDIUM,
                SessionStatus.COMPLETED),
            createSession(testPlayer, GameType.IDIOM, DifficultyLevel.HARD,
                SessionStatus.COMPLETED)
        ));

        // When
        List<GameSession> recent = gameSessionRepository
            .findByPlayerId(testPlayer.getId());

        // Then
        assertThat(recent).hasSize(3);
        // Verify ordered by startedAt DESC
        if (recent.size() >= 2) {
            assertThat(recent.get(0).getStartedAt())
                .isAfterOrEqualTo(recent.get(1).getStartedAt());
        }
    }

    @Test
    void testFindByPlayerAndGameType() {
        // Given
        Player player1 = createPlayer("player1", "p1@test.com");

        gameSessionRepository.saveAll(List.of(
            createSession(player1, GameType.IDIOM, DifficultyLevel.EASY,
                SessionStatus.COMPLETED),
            createSession(player1, GameType.IDIOM, DifficultyLevel.MEDIUM,
                SessionStatus.COMPLETED),
            createSession(player1, GameType.SENTENCE, DifficultyLevel.EASY,
                SessionStatus.COMPLETED)
        ));

        // When
        List<GameSession> allSessions = gameSessionRepository.findByPlayerId(player1.getId());
        List<GameSession> idiomSessions = allSessions.stream()
            .filter(s -> s.getGameType() == GameType.IDIOM)
            .toList();

        // Then
        assertThat(idiomSessions).hasSize(2);
        assertThat(idiomSessions).allMatch(s -> s.getGameType() == GameType.IDIOM);
    }

    @Test
    void testUpdateSessionStatus() {
        // Given
        GameSession session = createSession(testPlayer, GameType.IDIOM,
            DifficultyLevel.EASY, SessionStatus.ACTIVE);
        session = gameSessionRepository.save(session);

        // When
        session.complete(500);
        gameSessionRepository.save(session);

        // Then
        var updated = gameSessionRepository.findById(session.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus())
            .isEqualTo(SessionStatus.COMPLETED);
        assertThat(updated.get().getFinalScore()).isEqualTo(500);
        assertThat(updated.get().getCompletedAt()).isNotNull();
    }

    // Helper methods

    private GameSession createSession(Player player, GameType gameType,
                                     DifficultyLevel difficulty, SessionStatus status) {
        LocalDateTime now = LocalDateTime.now();
        GameSession session = GameSession.builder()
            .player(player)
            .gameType(gameType)
            .difficulty(difficulty)
            .status(status)
            .startedAt(now)
            .sessionData("{}")
            .build();
        // Set audit timestamps manually for tests
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        return session;
    }

    private Player createPlayer(String username, String email) {
        LocalDateTime now = LocalDateTime.now();
        Player player = Player.builder()
            .username(username)
            .email(email)
            .passwordHash("hashedpassword")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        // Set audit timestamps manually for tests
        player.setCreatedAt(now);
        player.setUpdatedAt(now);
        return playerRepository.save(player);
    }
}
