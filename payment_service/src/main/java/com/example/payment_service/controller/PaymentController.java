package com.example.payment_service.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.payment_service.entity.PaymentStatus;
import com.example.payment_service.payload.req.PaymentReq;
import com.example.payment_service.payload.res.MonthlyRevenueRes;
import com.example.payment_service.payload.res.PaymentRes;
import com.example.payment_service.payload.res.PaymentSummaryRes;
import com.example.payment_service.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentRes> create(@Valid @RequestBody PaymentReq request) {
        return ResponseEntity.status(201).body(paymentService.createPayment(request));

    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentRes> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping
    public ResponseEntity<List<PaymentRes>> getAll() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<PaymentRes>> getByTenant(
            @PathVariable Long tenantId) {

        return ResponseEntity.ok(
                paymentService
                        .getPaymentsByTenant(
                                tenantId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentRes>> getByStatus(
            @PathVariable PaymentStatus status) {

        return ResponseEntity.ok(
                paymentService.getPaymentsByStatus(status));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<PaymentRes> confirmPayment(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                paymentService.confirmPayment(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<PaymentRes> rejectPayment(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                paymentService.rejectPayment(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentRes> getByOrderId(
            @PathVariable String orderId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByOrderId(orderId));
    }


    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PaymentRes> cancel(
            @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.cancelPayment(id));
    }

    @PostMapping("/{id}/qris-pay")
    public ResponseEntity<PaymentRes> qrisPay(
            @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.qrisPay(id));
    }

    @GetMapping("/summary")
    public ResponseEntity<PaymentSummaryRes> summary() {

        return ResponseEntity.ok(
                paymentService.getSummary());
    }

    @GetMapping("/monthly-revenue")
    public ResponseEntity<List<MonthlyRevenueRes>> monthlyRevenue() {

        return ResponseEntity.ok(
                paymentService.getMonthlyRevenue());
    }

    @PostMapping("/{id}/upload-proof")
    public ResponseEntity<PaymentRes> uploadProof(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod) {

        return ResponseEntity.ok(
                paymentService.uploadProof(
                        id,
                        file,
                        paymentMethod));
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() throws IOException {
        ByteArrayInputStream in = paymentService.exportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payments.xlsx")
                .body(new InputStreamResource(in));
    }

    @GetMapping(value = "/{id}/qris", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQris(@PathVariable Long id) {
        try {
            PaymentRes payment = paymentService.getPaymentById(id);
            // Simulated QRIS payload/URL for the frontend simulation page
            String qrisData = "http://localhost:8080/qris-simulate.html?paymentId=" + id + "&amount=" + payment.getAmount() + "&tenantId=" + payment.getTenantId();
            
            com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(qrisData, com.google.zxing.BarcodeFormat.QR_CODE, 300, 300);
            
            java.io.ByteArrayOutputStream pngOutputStream = new java.io.ByteArrayOutputStream();
            com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(pngData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
