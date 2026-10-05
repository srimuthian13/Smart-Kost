package com.example.payment_service.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.payment_service.entity.PaymentEntity;
import com.example.payment_service.entity.PaymentStatus;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.client.TenantClient;
import com.example.payment_service.payload.res.TenantRes;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OverdueScheduler {
    private final PaymentRepository paymentRepository;
    private final TenantClient tenantClient;
    private final EmailService emailService;

    @Scheduled(cron = "0 */1 * * * *")
    public void markOverduePayments(){
        System.out.println("Checking overdue payment");
        List<PaymentEntity> payments = paymentRepository.findByStatusAndDueDateBefore(PaymentStatus.UNPAID, LocalDate.now());
        payments.forEach(payment -> {
            payment.setStatus(PaymentStatus.OVERDUE);
            try {
                TenantRes tenant = tenantClient.getTenantById(payment.getTenantId());
                if (tenant != null && tenant.getEmail() != null) {
                    emailService.sendInvoiceEmail(tenant.getEmail(), "Tagihan Jatuh Tempo", "Tagihan Anda sebesar " + payment.getAmount() + " telah jatuh tempo.");
                }
            } catch (Exception e) {
                System.err.println("Gagal mengambil data tenant " + payment.getTenantId() + " di scheduler: " + e.getMessage());
            }
        });
        paymentRepository.saveAll(payments);
    }
}
