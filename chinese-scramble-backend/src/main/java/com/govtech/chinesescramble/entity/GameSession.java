package com.govtech.chinesescramble.entity;

import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.entity.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * GameSession Entity - Represents an active or completed game session
 *
 * Session Lifecycle:
 * 1. Created when player starts a new game (status: ACTIVE)
 * 2. Player plays the game, may use hints, may pause
 * 3. Session ends in one of three ways:
 *    a) Player submits answer -> COMPLETED (score saved)
 *    b) Player quits -> ABANDONED (no score)
 *    c) Timeout/inactivity -> EXPIRED (auto-cleanup)
 *
 * Session Data:
 * - Stores game state as JSON (current question, remaining time, etc.)
 * - Tracks hint usage (via HintUsage relationship)
 * - Records start/completion timestamps
 * - Links to final score once completed
 *
 * Auto-Expiry:
 * - Scheduled job runs every 5 minutes
 * - Sessions inactive for 30+ minutes are marked EXPIRED
 * - Prevents indefinitely ACTIVE sessions
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Entity
@Table(
    name = "game_sessions",
    indexes = {
        @Index(name = "idx_session_player", columnList = "player_id"),
        @Index(name = "idx_session_status", columnList = "status"),
        @Index(name = "idx_session_started", columnList = "started_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = "hintsUsed")
@ToString(exclude = "hintsUsed")
public class GameSession extends BaseEntity {

    /**
     * Player who owns this game session
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_session_player"))
    private Player player;

    /**
     * Type of game being played
     * IDIOM: Idiom scramble game
     * SENTENCE: Sentence crafting game
     */
    @Column(nullable = false, length = 20, name = "game_type")
    @Enumerated(EnumType.STRING)
    private GameType gameType;

    /**
     * Difficulty level of this session
     * Determines time limit and scoring multipliers
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    /**
     * Current status of the session
     * ACTIVE: Game in progress
     * COMPLETED: Game finished successfully
     * ABANDONED: Player quit before completion
     * EXPIRED: Session timed out
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    /**
     * Timestamp when the session started
     * Immutable - set on creation
     */
    @Column(nullable = false, updatable = false, name = "started_at")
    private LocalDateTime startedAt;

    /**
     * Timestamp when the session was completed/abandoned/expired
     * Null while session is ACTIVE
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Final score achieved (only for COMPLETED sessions)
     * Null for ABANDONED or EXPIRED sessions
     */
    @Column(name = "final_score")
    private Integer finalScore;

    /**
     * Session data stored as JSON
     * Contains game state:
     * - Current question/idiom/sentence
     * - Scrambled characters/words
     * - Allowed characters/words
     * - Remaining time
     * - Pause status
     * Example: {"idiom":"一马当先","scrambled":["先","当","马","一"],"timeRemaining":120}
     */
    @Column(columnDefinition = "TEXT", name = "session_data")
    private String sessionData;

    /**
     * Hints used during this session
     * Cascade: ALL - deleting session deletes hint records
     */
    @OneToMany(
        mappedBy = "gameSession",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<HintUsage> hintsUsed = new HashSet<>();

    /**
     * Lifecycle callback before persist - sets timestamps and validates
     */
    @PrePersist
    private void onPrePersist() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        validateCompletedStatus();
    }

    /**
     * Lifecycle callback before update - validates
     */
    @PreUpdate
    private void onPreUpdate() {
        validateCompletedStatus();
    }

    /**
     * Validates that completed sessions have a completion timestamp
     */
    private void validateCompletedStatus() {
        if (status != SessionStatus.ACTIVE && completedAt == null) {
            throw new IllegalStateException(
                "Completed, abandoned, or expired sessions must have a completion timestamp"
            );
        }
    }

    /**
     * Completes the session with a score
     *
     * @param score the final score achieved
     */
    public void complete(Integer score) {
        this.status = SessionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.finalScore = score;
    }

    /**
     * Marks the session as abandoned
     */
    public void abandon() {
        this.status = SessionStatus.ABANDONED;
        this.completedAt = LocalDateTime.now();
        this.finalScore = null;
    }

    /**
     * Marks the session as expired
     */
    public void expire() {
        this.status = SessionStatus.EXPIRED;
        this.completedAt = LocalDateTime.now();
        this.finalScore = null;
    }

    /**
     * Adds a hint usage record to this session
     *
     * @param hintUsage the hint usage to add
     */
    public void addHintUsage(HintUsage hintUsage) {
        hintsUsed.add(hintUsage);
        hintUsage.setGameSession(this);
    }

    /**
     * Gets the total number of hints used in this session
     *
     * @return hint count
     */
    public int getHintCount() {
        return hintsUsed != null ? hintsUsed.size() : 0;
    }

    /**
     * Calculates the duration of this session
     *
     * @return duration in seconds, or 0 if not completed
     */
    public long getDurationSeconds() {
        if (startedAt == null) return 0;
        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        return Duration.between(startedAt, endTime).getSeconds();
    }

    /**
     * Checks if this session is still active
     *
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }

    /**
     * Checks if this session is completed successfully
     *
     * @return true if status is COMPLETED
     */
    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED;
    }

    /**
     * Checks if this session should be expired (inactive for 30+ minutes)
     *
     * @return true if session is ACTIVE and older than 30 minutes
     */
    public boolean shouldExpire() {
        if (!isActive() || startedAt == null) return false;
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        return startedAt.isBefore(thirtyMinutesAgo);
    }
}