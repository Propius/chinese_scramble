package com.govtech.chinesescramble.entity;

import com.govtech.chinesescramble.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Player Entity - Represents a registered user/player in the Chinese Word Scramble Game
 *
 * Security Notes:
 * - Password is stored as hash only (passwordHash field)
 * - Email and username are unique and indexed for fast lookup
 * - UTF-8 encoding supports Chinese usernames
 *
 * Relationships:
 * - One-to-Many with IdiomScore (player's idiom game history)
 * - One-to-Many with SentenceScore (player's sentence game history)
 * - One-to-Many with GameSession (active/completed game sessions)
 * - One-to-Many with Leaderboard (player rankings)
 * - One-to-Many with Achievement (unlocked achievements)
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Entity
@Table(
    name = "players",
    indexes = {
        @Index(name = "idx_player_username", columnList = "username"),
        @Index(name = "idx_player_email", columnList = "email"),
        @Index(name = "idx_player_active", columnList = "active")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {
    "idiomScores", "sentenceScores", "gameSessions", "leaderboardEntries", "achievements"
})
@ToString(exclude = {
    "idiomScores", "sentenceScores", "gameSessions", "leaderboardEntries", "achievements"
})
public class Player extends BaseEntity {

    /**
     * Username - unique identifier for the player
     * Supports Chinese characters (e.g., "玩家123")
     * Length: 3-50 characters
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Email address - used for authentication and notifications
     * Must be unique across all players
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Password hash - NEVER store plain text passwords
     * Uses BCrypt with cost factor 12+ (configured in SecurityConfig)
     * Length: 60 characters for BCrypt hash
     */
    @Column(nullable = false, length = 255, name = "password_hash")
    private String passwordHash;

    /**
     * User role - determines permissions and access levels
     * Default: PLAYER
     * Options: PLAYER, ADMIN, MODERATOR
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.PLAYER;

    /**
     * Active status - whether the account is active
     * Inactive accounts cannot log in
     * Default: true
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Last login timestamp - tracks player activity
     * Updated on each successful login
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * Idiom game scores - history of all idiom games played
     * Cascade: ALL - deleting player deletes all scores
     * Fetch: LAZY - only load when explicitly needed
     */
    @OneToMany(
        mappedBy = "player",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<IdiomScore> idiomScores = new HashSet<>();

    /**
     * Sentence game scores - history of all sentence games played
     * Cascade: ALL - deleting player deletes all scores
     * Fetch: LAZY - only load when explicitly needed
     */
    @OneToMany(
        mappedBy = "player",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<SentenceScore> sentenceScores = new HashSet<>();

    /**
     * Game sessions - active and completed game sessions
     * Cascade: ALL - deleting player deletes all sessions
     */
    @OneToMany(
        mappedBy = "player",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<GameSession> gameSessions = new HashSet<>();

    /**
     * Leaderboard entries - player's rankings across different game modes
     * Cascade: ALL - deleting player removes from leaderboard
     */
    @OneToMany(
        mappedBy = "player",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<Leaderboard> leaderboardEntries = new HashSet<>();

    /**
     * Achievements - unlocked achievements for the player
     * Cascade: ALL - deleting player removes achievements
     */
    @OneToMany(
        mappedBy = "player",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<Achievement> achievements = new HashSet<>();

    /**
     * Helper method to add an idiom score to this player
     * Maintains bidirectional relationship
     *
     * @param idiomScore the idiom score to add
     */
    public void addIdiomScore(IdiomScore idiomScore) {
        idiomScores.add(idiomScore);
        idiomScore.setPlayer(this);
    }

    /**
     * Helper method to remove an idiom score from this player
     * Maintains bidirectional relationship
     *
     * @param idiomScore the idiom score to remove
     */
    public void removeIdiomScore(IdiomScore idiomScore) {
        idiomScores.remove(idiomScore);
        idiomScore.setPlayer(null);
    }

    /**
     * Helper method to add a sentence score to this player
     * Maintains bidirectional relationship
     *
     * @param sentenceScore the sentence score to add
     */
    public void addSentenceScore(SentenceScore sentenceScore) {
        sentenceScores.add(sentenceScore);
        sentenceScore.setPlayer(this);
    }

    /**
     * Helper method to remove a sentence score from this player
     * Maintains bidirectional relationship
     *
     * @param sentenceScore the sentence score to remove
     */
    public void removeSentenceScore(SentenceScore sentenceScore) {
        sentenceScores.remove(sentenceScore);
        sentenceScore.setPlayer(null);
    }

    /**
     * Updates the last login timestamp to current time
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Deactivates the player account
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Reactivates the player account
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Checks if the player account is active
     *
     * @return true if account is active, false otherwise
     */
    public boolean isActive() {
        return this.active != null && this.active;
    }

    /**
     * Gets the last login timestamp
     *
     * @return last login timestamp, or null if never logged in
     */
    public LocalDateTime getLastLogin() {
        return this.lastLoginAt;
    }

    /**
     * Sets the last login timestamp
     *
     * @param lastLogin the last login timestamp to set
     */
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLoginAt = lastLogin;
    }
}