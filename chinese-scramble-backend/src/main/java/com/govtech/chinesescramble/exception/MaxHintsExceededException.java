package com.govtech.chinesescramble.exception;

/**
 * Exception thrown when player attempts to use more hints than allowed
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public class MaxHintsExceededException extends RuntimeException {

    private final int maxHints;
    private final int currentHints;

    public MaxHintsExceededException(int maxHints, int currentHints) {
        super(String.format("Maximum hints exceeded. Max: %d, Current: %d", maxHints, currentHints));
        this.maxHints = maxHints;
        this.currentHints = currentHints;
    }

    public int getMaxHints() {
        return maxHints;
    }

    public int getCurrentHints() {
        return currentHints;
    }
}
