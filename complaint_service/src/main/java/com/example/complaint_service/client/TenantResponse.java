package com.example.complaint_service.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantResponse {
    private boolean success;
    private String message;
    private Object data;
}
