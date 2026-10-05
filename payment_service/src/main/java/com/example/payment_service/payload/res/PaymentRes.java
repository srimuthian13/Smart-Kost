package com.example.payment_service.payload.res;

import com.example.payment_service.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRes {
    private Long id;
    private Long tenantId;
    private Long roomId;
    private BigDecimal amount;
    private LocalDate dueDate;
    private PaymentStatus status;
    private String paymentMethod;
    private String proofUrl;
    private String orderId;
    private LocalDate paymentDate;
}
    