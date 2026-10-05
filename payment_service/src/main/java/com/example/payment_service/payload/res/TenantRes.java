package com.example.payment_service.payload.res;

import lombok.Data;

@Data
public class TenantRes {
    private Long id;
    private String name;
    private Long roomId;
    private String status;
    private String email;
    private java.math.BigDecimal monthlyRent;
}
