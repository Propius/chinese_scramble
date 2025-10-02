package com.govtech.chinesescramble.exception;

/**
 * Exception thrown when game state is invalid or corrupted
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public class InvalidGameStateException extends RuntimeException {

    private final String gameState;

    public InvalidGameStateException(String message) {
        super(message);
        this.gameState = null;
    }

    public InvalidGameStateException(String message, String gameState) {
        super(message);
        this.gameState = gameState;
    }

    public String getGameState() {
        return gameState;
    }
}
