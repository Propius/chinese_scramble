package com.govtech.chinesescramble.exception;

/**
 * Exception thrown when a player is not found
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public class PlayerNotFoundException extends RuntimeException {

    private final Long playerId;

    public PlayerNotFoundException(Long playerId) {
        super("Player not found with ID: " + playerId);
        this.playerId = playerId;
    }

    public PlayerNotFoundException(String message) {
        super(message);
        this.playerId = null;
    }

    public Long getPlayerId() {
        return playerId;
    }
}
