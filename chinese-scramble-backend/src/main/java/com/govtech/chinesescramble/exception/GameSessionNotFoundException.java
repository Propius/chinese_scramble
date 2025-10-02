package com.govtech.chinesescramble.exception;

/**
 * Exception thrown when a game session is not found
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public class GameSessionNotFoundException extends RuntimeException {

    private final Long sessionId;

    public GameSessionNotFoundException(Long sessionId) {
        super("Game session not found with ID: " + sessionId);
        this.sessionId = sessionId;
    }

    public GameSessionNotFoundException(String message) {
        super(message);
        this.sessionId = null;
    }

    public Long getSessionId() {
        return sessionId;
    }
}
