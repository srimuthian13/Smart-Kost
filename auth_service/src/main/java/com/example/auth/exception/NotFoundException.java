package com.example.auth.exception;

public class NotFoundException extends RuntimeException {
    // untuk data yang tidak ditemukan, seperti user tidak ditemukan
    public NotFoundException(String message) {
        super(message);
    }
    
}
