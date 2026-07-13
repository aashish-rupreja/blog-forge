package com.blogforge.exception;

public class AuthorApplicationAlreadyExistsException extends RuntimeException {
    public AuthorApplicationAlreadyExistsException(String message) {
        super(message);
    }
}
