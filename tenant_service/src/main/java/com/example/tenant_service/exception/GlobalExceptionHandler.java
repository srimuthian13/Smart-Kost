package com.example.tenant_service.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import com.example.tenant_service.exception.DuplicateNikException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TenantNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleTenantNotFoundException(TenantNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(DuplicateNikException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleDuplicateNikException(DuplicateNikException ex) {
        return Map.of("error", ex.getMessage());
    }

    
}
