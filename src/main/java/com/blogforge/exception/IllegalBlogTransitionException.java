package com.blogforge.exception;

public class IllegalBlogTransitionException extends RuntimeException {
    public IllegalBlogTransitionException(String message) {
        super(message);
    }
}
