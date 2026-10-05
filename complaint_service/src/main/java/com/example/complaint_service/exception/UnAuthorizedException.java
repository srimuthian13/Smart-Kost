package com.example.complaint_service.exception;

public class UnAuthorizedException extends RuntimeException {
    // untuk akses yang tidak sah, seperti token tidak valid atau tidak ada token
    public UnAuthorizedException(String message) {
        super(message);
    }
    
}
