package com.music.trivia.server.demo.exception;

/**
 * Exception thrown when a user is not found in a session.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

