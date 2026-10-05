package com.example.tenant_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long tenantId;
    private Long roomId;
    private BigDecimal amount;
    private String status;
    private LocalDate dueDate;
    private LocalDateTime paymentDate;
}
