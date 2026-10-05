package com.example.complaint_service.payload.res;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ErrorRes {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}