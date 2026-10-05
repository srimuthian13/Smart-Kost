package com.example.tenant_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PaymentServiceClient {
    private final RestTemplate restTemplate;

    @Value("${payment.service.url:http://localhost:8084}")
    private String paymentServiceUrl;

    public void createInvoice(Long tenantId, Long roomId, BigDecimal amount, LocalDate dueDate) {
        PaymentRequest request = new PaymentRequest();
        request.setTenantId(tenantId);
        request.setRoomId(roomId);
        request.setAmount(amount);
        request.setDueDate(dueDate);
        restTemplate.postForObject(paymentServiceUrl + "/api/payments", request, Void.class);
    }
}
