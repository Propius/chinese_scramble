package com.govtech.chinesescramble.entity.enums;

/**
 * SessionStatus - Status of a game session
 *
 * ACTIVE:
 * - Session currently in progress
 * - Player is actively playing
 * - Timer is running
 * - Can transition to: COMPLETED, ABANDONED, EXPIRED
 *
 * COMPLETED:
 * - Session finished successfully
 * - Player submitted an answer
 * - Score has been calculated and saved
 * - Final state (no further transitions)
 *
 * ABANDONED:
 * - Player quit the game before completion
 * - No score saved
 * - Used for tracking user behavior
 * - Final state (no further transitions)
 *
 * EXPIRED:
 * - Session timed out (inactive for too long)
 * - Automatically set by scheduled job
 * - Prevents indefinitely active sessions
 * - Final state (no further transitions)
 *
 * State Transitions:
 * NEW -> ACTIVE (when game starts)
 * ACTIVE -> COMPLETED (player submits answer)
 * ACTIVE -> ABANDONED (player quits)
 * ACTIVE -> EXPIRED (timeout after 30 minutes of inactivity)
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public enum SessionStatus {
    /**
     * Session is currently active
     */
    ACTIVE,

    /**
     * Session completed successfully
     */
    COMPLETED,

    /**
     * Session abandoned by player
     */
    ABANDONED,

    /**
     * Session expired due to inactivity
     */
    EXPIRED;

    /**
     * Checks if this status represents an active (in-progress) session
     *
     * @return true if ACTIVE
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Checks if this status represents a finished session
     *
     * @return true if COMPLETED, ABANDONED, or EXPIRED
     */
    public boolean isFinished() {
        return this == COMPLETED || this == ABANDONED || this == EXPIRED;
    }

    /**
     * Checks if this status represents a successfully completed session
     *
     * @return true if COMPLETED
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /**
     * Gets a human-readable description
     *
     * @return status description
     */
    public String getDescription() {
        return switch (this) {
            case ACTIVE -> "Game in progress";
            case COMPLETED -> "Game completed successfully";
            case ABANDONED -> "Game abandoned by player";
            case EXPIRED -> "Game session expired";
        };
    }
}