package com.example.tenant_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @NotBlank
    private String name;

    @NotNull
    private Long roomId;

    @NotBlank
    @Column(unique = true)
    private String phone;

    private String email;

    @Column(unique = true)
    private String nik;

    private String address;

    private String occupation;

    private String emergencyContact;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal monthlyRent;

    @Enumerated(EnumType.STRING)
    private TenantStatusEntity status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDate checkOutDate;


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }


    @PreUpdate

    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
