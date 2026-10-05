package com.example.payment_service.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.payment_service.client.RoomClient;
import com.example.payment_service.client.TenantClient;
import com.example.payment_service.entity.PaymentEntity;
import com.example.payment_service.entity.PaymentStatus;
import com.example.payment_service.payload.res.RoomRes;
import com.example.payment_service.payload.res.TenantRes;
import com.example.payment_service.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InvoiceScheduler {

    private final TenantClient tenantClient;
    private final RoomClient roomClient;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 */10 * * * *")
    public void generateMonthlyInvoices() {

        List<TenantRes> tenants = tenantClient.getActiveTenants();

        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        for (TenantRes tenant : tenants) {

            boolean exists = paymentRepository
                    .existsByTenantIdAndBillingMonthAndBillingYear(
                            tenant.getId(),
                            month,
                            year);
            if (exists) {
                continue;
            }

            RoomRes room = roomClient.getRoomById(tenant.getRoomId());

            String orderId
                    = "INV-M-" + System.currentTimeMillis();

            PaymentEntity payment = PaymentEntity.builder()
                            .tenantId(tenant.getId())
                            .roomId(room.getId())
                            .amount(room.getPrice())
                            .billingMonth(month)
                            .billingYear(year)
                            .dueDate(LocalDate.now().plusDays(10))
                            .status(PaymentStatus.UNPAID)
                            .orderId(orderId)
                            .build();
            PaymentEntity saved = paymentRepository.save(payment);

            if (tenant.getEmail() != null) {
                java.text.NumberFormat idFormat = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("id", "ID"));
                String formattedPrice = idFormat.format(room.getPrice());
                String dateStr = saved.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
                String body = "Halo " + tenant.getName() + ",<br><br>Tagihan pembayaran Anda selanjutnya telah diterbitkan:<br>" +
                              "<b>Username Tagihan:</b> " + tenant.getName() + "<br>" +
                              "<b>Nominal:</b> " + formattedPrice + "<br>" +
                              "<b>Tanggal Jatuh Tempo:</b> " + dateStr + "<br><br>Terima kasih.";
                emailService.sendInvoiceEmail(tenant.getEmail(), "Tagihan Bulanan Smart Kost", body);
            }
        }
    }
}
