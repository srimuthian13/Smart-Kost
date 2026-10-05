package com.example.tenant_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long tenantId;
    private Long roomId;
    private BigDecimal amount;
    private LocalDate dueDate;
}
