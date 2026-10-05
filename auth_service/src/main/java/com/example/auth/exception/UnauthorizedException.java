package com.example.auth.exception;

public class UnauthorizedException extends RuntimeException {
    // untuk akses yang tidak sah, seperti token tidak valid atau tidak ada token
    public UnauthorizedException(String message) {
        super(message);
    }
    
}
