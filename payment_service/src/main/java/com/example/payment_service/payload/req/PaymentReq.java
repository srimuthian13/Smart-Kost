package com.example.payment_service.payload.req;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentReq {

   @NotNull(message = "Tenant ID wajib diisi")
    private Long tenantId;

    
    @NotNull(message = "Room ID wajib diisi")
    private Long roomId;

   
    @NotNull(message = "Nominal pembayaran wajib diisi")
    @DecimalMin(value = "1.0", inclusive = false, message = "Nominal harus lebih dari 0")
    private BigDecimal amount;

    @NotNull(message = "Tanggal jatuh tempo wajib diisi")
    private LocalDate dueDate;

   
    private String paymentMethod;

}