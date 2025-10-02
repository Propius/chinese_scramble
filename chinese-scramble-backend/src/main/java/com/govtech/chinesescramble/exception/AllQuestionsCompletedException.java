package com.govtech.chinesescramble.exception;

/**
 * AllQuestionsCompletedException - Thrown when player has completed all available questions
 *
 * This exception is thrown when a player has answered all questions in a given difficulty level
 * and there are no more questions available. The frontend should display a completion message
 * and offer the option to restart the quiz or try a different difficulty level.
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public class AllQuestionsCompletedException extends RuntimeException {

    public AllQuestionsCompletedException(String message) {
        super(message);
    }

    public AllQuestionsCompletedException(String message, Throwable cause) {
        super(message, cause);
    }
}
