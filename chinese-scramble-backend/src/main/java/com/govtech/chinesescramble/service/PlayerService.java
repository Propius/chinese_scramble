package com.govtech.chinesescramble.service;

import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.UserRole;
import com.govtech.chinesescramble.repository.AchievementRepository;
import com.govtech.chinesescramble.repository.IdiomScoreRepository;
import com.govtech.chinesescramble.repository.LeaderboardRepository;
import com.govtech.chinesescramble.repository.PlayerRepository;
import com.govtech.chinesescramble.repository.SentenceScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PlayerService - Manages player accounts and profiles
 *
 * Features:
 * - Player registration with validation
 * - Password management (BCrypt hashing)
 * - Profile updates
 * - Player statistics calculation
 * - Activity tracking
 * - Account activation/deactivation
 *
 * Registration Validation:
 * - Username: 3-50 characters, supports Chinese
 * - Email: Valid email format, unique
 * - Password: Minimum 8 characters, BCrypt hashed
 * - Default role: PLAYER
 * - Default status: ACTIVE
 *
 * Statistics Tracked:
 * - Total games played
 * - Games by type (idiom/sentence)
 * - Total score
 * - Average accuracy
 * - Achievement count
 * - Leaderboard positions
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final IdiomScoreRepository idiomScoreRepository;
    private final SentenceScoreRepository sentenceScoreRepository;
    private final AchievementRepository achievementRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new player
     *
     * @param username username (supports Chinese)
     * @param email email address
     * @param password plain text password (will be hashed)
     * @return created player
     */
    public Player registerPlayer(String username, String email, String password) {
        log.info("Registering new player: username={}, email={}", username, email);

        // Validate username uniqueness
        if (playerRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        // Validate email uniqueness
        if (playerRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        // Validate password length
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(password);

        // Create player
        Player player = Player.builder()
            .username(username)
            .email(email)
            .passwordHash(hashedPassword)
            .role(UserRole.PLAYER)
            .active(true)
            .build();

        Player saved = playerRepository.save(player);
        log.info("Player registered successfully: id={}, username={}", saved.getId(), saved.getUsername());

        return saved;
    }

    /**
     * Authenticates a player
     *
     * @param username username or email
     * @param password plain text password
     * @return Optional containing authenticated player
     */
    @Transactional(readOnly = true)
    public Optional<Player> authenticatePlayer(String username, String password) {
        log.debug("Authenticating player: {}", username);

        // Try username first
        Optional<Player> playerByUsername = playerRepository.findByUsernameIgnoreCase(username);
        if (playerByUsername.isPresent()) {
            Player player = playerByUsername.get();
            if (passwordEncoder.matches(password, player.getPasswordHash())) {
                log.info("Player authenticated: id={}, username={}", player.getId(), player.getUsername());
                return Optional.of(player);
            }
        }

        // Try email
        Optional<Player> playerByEmail = playerRepository.findByEmailIgnoreCase(username);
        if (playerByEmail.isPresent()) {
            Player player = playerByEmail.get();
            if (passwordEncoder.matches(password, player.getPasswordHash())) {
                log.info("Player authenticated: id={}, username={}", player.getId(), player.getUsername());
                return Optional.of(player);
            }
        }

        log.warn("Authentication failed for: {}", username);
        return Optional.empty();
    }

    /**
     * Updates player's last login timestamp
     *
     * @param playerId player ID
     */
    public void updateLastLogin(Long playerId) {
        log.debug("Updating last login: player={}", playerId);

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        player.setLastLogin(LocalDateTime.now());
        playerRepository.save(player);
    }

    /**
     * Updates player profile
     *
     * @param playerId player ID
     * @param email new email (optional)
     * @return updated player
     */
    public Player updateProfile(Long playerId, String email) {
        log.info("Updating profile: player={}", playerId);

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        if (email != null && !email.equals(player.getEmail())) {
            // Validate email uniqueness
            if (playerRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalArgumentException("Email already registered: " + email);
            }
            player.setEmail(email);
        }

        return playerRepository.save(player);
    }

    /**
     * Changes player password
     *
     * @param playerId player ID
     * @param oldPassword current password
     * @param newPassword new password
     * @return updated player
     */
    public Player changePassword(Long playerId, String oldPassword, String newPassword) {
        log.info("Changing password: player={}", playerId);

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, player.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }

        // Hash and update password
        String hashedPassword = passwordEncoder.encode(newPassword);
        player.setPasswordHash(hashedPassword);

        return playerRepository.save(player);
    }

    /**
     * Deactivates player account
     *
     * @param playerId player ID
     * @return deactivated player
     */
    public Player deactivateAccount(Long playerId) {
        log.warn("Deactivating account: player={}", playerId);

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        player.setActive(false);
        return playerRepository.save(player);
    }

    /**
     * Reactivates player account
     *
     * @param playerId player ID
     * @return reactivated player
     */
    public Player reactivateAccount(Long playerId) {
        log.info("Reactivating account: player={}", playerId);

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        player.setActive(true);
        return playerRepository.save(player);
    }

    /**
     * Gets player by ID
     * Uses Spring Cache (10-minute TTL)
     *
     * @param playerId player ID
     * @return Optional containing player
     */
    @Cacheable(value = "playerStats", key = "'player_' + #playerId")
    @Transactional(readOnly = true)
    public Optional<Player> getPlayerById(Long playerId) {
        return playerRepository.findById(playerId);
    }

    /**
     * Gets player by username
     *
     * @param username username
     * @return Optional containing player
     */
    @Transactional(readOnly = true)
    public Optional<Player> getPlayerByUsername(String username) {
        return playerRepository.findByUsernameIgnoreCase(username);
    }

    /**
     * Gets player statistics
     *
     * @param playerId player ID
     * @return player statistics
     */
    @Cacheable(value = "playerStats", key = "'stats_' + #playerId")
    @Transactional(readOnly = true)
    public PlayerStatistics getPlayerStatistics(Long playerId) {
        log.debug("Calculating statistics for player: {}", playerId);

        // Game counts
        long idiomGames = idiomScoreRepository.countCompletedGamesByPlayer(playerId);
        long sentenceGames = sentenceScoreRepository.countCompletedGamesByPlayer(playerId);
        long totalGames = idiomGames + sentenceGames;

        // Total scores
        long idiomTotalScore = idiomScoreRepository.calculateTotalScore(playerId, null).orElse(0L);
        long sentenceTotalScore = sentenceScoreRepository.calculateTotalScore(playerId, null).orElse(0L);
        long totalScore = idiomTotalScore + sentenceTotalScore;

        // Average accuracy
        double idiomAvgAccuracy = idiomScoreRepository.calculateAverageAccuracy(playerId).orElse(0.0);
        double sentenceAvgAccuracy = sentenceScoreRepository.calculateAverageAccuracy(playerId).orElse(0.0);
        double overallAccuracy = totalGames > 0
            ? (idiomAvgAccuracy * idiomGames + sentenceAvgAccuracy * sentenceGames) / totalGames
            : 0.0;

        // Achievement count
        long achievementCount = achievementRepository.countByPlayerId(playerId);

        // Best ranks
        List<com.govtech.chinesescramble.entity.Leaderboard> leaderboards =
            leaderboardRepository.findByPlayerId(playerId);

        Integer bestRank = leaderboards.stream()
            .map(com.govtech.chinesescramble.entity.Leaderboard::getRank)
            .min(Integer::compareTo)
            .orElse(null);

        return new PlayerStatistics(
            totalGames,
            idiomGames,
            sentenceGames,
            totalScore,
            overallAccuracy,
            achievementCount,
            bestRank,
            leaderboards.size()
        );
    }

    /**
     * Gets all active players
     *
     * @return list of active players
     */
    @Transactional(readOnly = true)
    public List<Player> getActivePlayers() {
        return playerRepository.findByActiveTrue();
    }

    /**
     * Searches players by username or email
     *
     * @param searchTerm search term
     * @return list of matching players
     */
    @Transactional(readOnly = true)
    public List<Player> searchPlayers(String searchTerm) {
        return playerRepository.searchPlayers(searchTerm);
    }

    /**
     * Gets recently active players (logged in within last 7 days)
     *
     * @return list of recently active players
     */
    @Transactional(readOnly = true)
    public List<Player> getRecentlyActivePlayers() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return playerRepository.findRecentlyActivePlayers(sevenDaysAgo);
    }

    /**
     * Gets inactive players (no login in 30+ days)
     *
     * @return list of inactive players
     */
    @Transactional(readOnly = true)
    public List<Player> getInactivePlayers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return playerRepository.findInactivePlayers(thirtyDaysAgo);
    }

    /**
     * Gets total player count
     *
     * @return player count
     */
    @Transactional(readOnly = true)
    public long getTotalPlayerCount() {
        return playerRepository.count();
    }

    /**
     * Gets active player count
     *
     * @return active player count
     */
    @Transactional(readOnly = true)
    public long getActivePlayerCount() {
        return playerRepository.countByActiveTrue();
    }

    /**
     * Clears player cache
     *
     * @param playerId player ID
     */
    @CacheEvict(value = "playerStats", key = "'player_' + #playerId")
    public void clearPlayerCache(Long playerId) {
        log.debug("Clearing cache for player: {}", playerId);
    }

    /**
     * Clears all player caches
     */
    @CacheEvict(value = "playerStats", allEntries = true)
    public void clearAllPlayerCaches() {
        log.info("Clearing all player caches");
    }

    // ========================================================================
    // DTOs
    // ========================================================================

    /**
     * Player statistics
     */
    public record PlayerStatistics(
        long totalGames,
        long idiomGames,
        long sentenceGames,
        long totalScore,
        double overallAccuracy,
        long achievementCount,
        Integer bestRank,
        int leaderboardCount
    ) {
        public double accuracyPercentage() {
            return overallAccuracy * 100.0;
        }

        public double averageScorePerGame() {
            return totalGames > 0 ? (double) totalScore / totalGames : 0.0;
        }

        public boolean hasTopRank() {
            return bestRank != null && bestRank <= 10;
        }
    }
}