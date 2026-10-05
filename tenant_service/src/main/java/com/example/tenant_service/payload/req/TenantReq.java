package com.example.tenant_service.payload.req;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TenantReq {
    private Long userId;

    @NotNull
    private long roomId;

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @Email
    private String email;

    @NotBlank
    private String nik;

    private String address;
    private String occupation;
 

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private BigDecimal monthlyRent;
}
