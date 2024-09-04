package com.music.trivia.server.demo.exception;

/**
 * Exception thrown when a user already exists in a session.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}


