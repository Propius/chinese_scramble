package com.govtech.chinesescramble.exception;

/**
 * Exception thrown when attempting to register a player with duplicate username/email
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public class DuplicatePlayerException extends RuntimeException {

    private final String field;
    private final String value;

    public DuplicatePlayerException(String field, String value) {
        super(String.format("%s already exists: %s", field, value));
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
