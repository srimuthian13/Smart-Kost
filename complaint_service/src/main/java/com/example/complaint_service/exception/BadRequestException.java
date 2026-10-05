package com.example.complaint_service.exception;


public class BadRequestException extends RuntimeException {
   // untuk email sudah terdaftar, atau data yang dikirim tidak valid
    public BadRequestException(String message) {
        super(message);
    }
    
}
