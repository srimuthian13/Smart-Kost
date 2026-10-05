package com.example.payment_service.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.payment_service.client.TenantClient;
import com.example.payment_service.entity.PaymentEntity;
import com.example.payment_service.entity.PaymentStatus;
import com.example.payment_service.payload.res.TenantRes;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class MonthlyBillingScheduler {

    private final PaymentRepository paymentRepository;
    private final TenantClient tenantClient;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Scheduled(cron = "0 0 0 1 * ?") 
    @Transactional
    public void generateMonthlyInvoices() {
        log.info("Starting monthly billing scheduler...");
        
        try {
            List<TenantRes> activeTenants = tenantClient.getActiveTenants();
            if (activeTenants == null || activeTenants.isEmpty()) {
                log.info("No active tenants found for monthly billing.");
                return;
            }

            LocalDate today = LocalDate.now();
            int currentMonth = today.getMonthValue();
            int currentYear = today.getYear();
            LocalDate dueDate = today.plusDays(7); // Due in 7 days

            for (TenantRes tenant : activeTenants) {
                boolean alreadyBilled = paymentRepository.existsByTenantIdAndBillingMonthAndBillingYear(
                        tenant.getId(), currentMonth, currentYear);
                
                if (alreadyBilled) {
                    continue;
                }

                String orderId = "INV-" + System.currentTimeMillis();
                
                PaymentEntity payment = PaymentEntity.builder()
                        .tenantId(tenant.getId())
                        .roomId(tenant.getRoomId())
                        .amount(tenant.getMonthlyRent())
                        .dueDate(dueDate)
                        .status(PaymentStatus.UNPAID)
                        .orderId(orderId)
                        .billingMonth(currentMonth)
                        .billingYear(currentYear)
                        .build();

                PaymentEntity saved = paymentRepository.save(payment);
                

                if (tenant.getEmail() != null) {
                    String body = "Halo " + tenant.getName() + ",<br><br>"
                            + "Tagihan bulan ini telah tersedia.<br>"
                            + "Informasi:<br>"
                            + "<ul>"
                            + "<li>Nomor Kamar: " + tenant.getRoomId() + "</li>"
                            + "<li>Nominal Tagihan: Rp " + tenant.getMonthlyRent() + "</li>"
                            + "<li>Due Date: " + dueDate + "</li>"
                            + "</ul><br>"
                            + "Silakan login ke Dashboard Smart Kost untuk melakukan pembayaran.";
                            
                    emailService.sendInvoiceEmail(tenant.getEmail(), "Tagihan Bulan Ini - Smart Kost", body);
                }
                
                log.info("Generated monthly invoice for tenant: {}", tenant.getId());
            }

        } catch (Exception e) {
            log.error("Error in monthly billing scheduler", e);
        }
    }

}
