package com.example.payment_service.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.payment_service.client.RoomClient;
import com.example.payment_service.client.TenantClient;
import com.example.payment_service.entity.PaymentEntity;
import com.example.payment_service.entity.PaymentStatus;
import com.example.payment_service.exception.ResourceNotFoundException;
import com.example.payment_service.payload.res.RoomRes;
import com.example.payment_service.payload.res.TenantRes;
import com.example.payment_service.payload.req.PaymentReq;
import com.example.payment_service.payload.res.MonthlyRevenueRes;
import com.example.payment_service.payload.res.PaymentRes;
import com.example.payment_service.payload.res.PaymentSummaryRes;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.PaymentService;
import com.example.payment_service.service.EmailService;
import com.example.payment_service.utility.ExcelUtility;
import com.example.payment_service.uploads.payments.UploadUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.example.payment_service.utility.PdfGeneratorUtil;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final TenantClient tenantClient;
    private final RoomClient roomClient;
    private final ExcelUtility excelUtility;
    private final EmailService emailService;
    private final UploadUtil uploadUtil;
    private final PdfGeneratorUtil pdfGeneratorUtil;



    @Override
    public PaymentRes createPayment(PaymentReq request) {

        TenantRes tenant = tenantClient.getTenantById(request.getTenantId());
        RoomRes room = roomClient.getRoomById(request.getRoomId());

        if (tenant == null) {
            throw new ResourceNotFoundException(
                    "Tenant tidak ditemukan");
        }

        if (room == null) {
            throw new ResourceNotFoundException(
                    "Room tidak ditemukan");
        }

        if (!tenant.getRoomId()
                .equals(request.getRoomId())) {

            throw new IllegalArgumentException(
                    "Room tidak sesuai dengan tenant");
        }

        if (!"ACTIVE".equalsIgnoreCase(tenant.getStatus())
                && !"PENDING".equalsIgnoreCase(tenant.getStatus())) {

            throw new IllegalArgumentException(
                    "Tenant tidak aktif atau pending");
        }

        String orderId = "INV-" + System.currentTimeMillis();
        PaymentEntity payment = PaymentEntity.builder()
                .tenantId(request.getTenantId())
                .roomId(request.getRoomId())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .status(PaymentStatus.UNPAID)
                .orderId(orderId)
                .build();

        PaymentEntity saved = paymentRepository.save(payment);

        return mapToResponse(saved);
    }

    @Override
    public PaymentRes getPaymentById(Long id) {
        return mapToResponse(getPaymentEntityById(id));
    }

    @Override
    public List<PaymentRes> getPaymentsByTenant(
            Long tenantId) {

        return paymentRepository
                .findByTenantId(tenantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PaymentRes> getPaymentsByStatus(PaymentStatus status) {

        return paymentRepository
                .findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PaymentRes> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PaymentRes mapToResponse(PaymentEntity payment) {
        return PaymentRes.builder()
                .id(payment.getId())
                .tenantId(payment.getTenantId())
                .roomId(payment.getRoomId())
                .amount(payment.getAmount())
                .dueDate(payment.getDueDate())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .proofUrl(payment.getProofImage())
                .orderId(payment.getOrderId())
                .paymentDate(payment.getPaymentDate())
                .build();
    }

    private PaymentEntity getPaymentEntityById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment tidak ditemukan"));
    }

    @Override
    public PaymentRes qrisPay(Long id) {
        PaymentEntity payment = getPaymentEntityById(id);
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Pembayaran sudah lunas");
        }
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentMethod("QRIS");
        payment.setPaymentDate(LocalDate.now());
        
        PaymentEntity saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }

    @Override
    public PaymentRes cancelPayment(Long id) {
        PaymentEntity payment = getPaymentEntityById(id);
        payment.setStatus(PaymentStatus.CANCELLED);
        return mapToResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentSummaryRes getSummary() {

        return PaymentSummaryRes.builder()
                .totalInvoices(paymentRepository.count())
                .paidInvoices(paymentRepository.countByStatus(PaymentStatus.PAID))
                .pendingInvoices(paymentRepository.countByStatus(PaymentStatus.UNPAID))
                .overdueInvoices(paymentRepository.countByStatus(PaymentStatus.OVERDUE))
                .totalRevenue(paymentRepository.getTotalRevenue())
                .build();
    }

    @Override
    public List<MonthlyRevenueRes> getMonthlyRevenue() {
        return paymentRepository
                .getMonthlyRevenue()
                .stream().map(row -> new MonthlyRevenueRes(
                ((Number) row[0]).intValue(), new BigDecimal(row[1].toString())))
                .toList();
    }

    @Override
    public ByteArrayInputStream exportExcel()
            throws IOException {

        return excelUtility.exportPayments(paymentRepository.findAll());
    }

    @Override
    public PaymentRes confirmPayment(Long paymentId) {
        PaymentEntity payment = getPaymentEntityById(paymentId);
        return processPaymentSuccess(payment);
    }

    @Override
    public PaymentRes rejectPayment(Long paymentId) {
        PaymentEntity payment = getPaymentEntityById(paymentId);
        payment.setStatus(PaymentStatus.REJECTED);
        paymentRepository.save(payment);
        return mapToResponse(payment);
    }

    @Override
    public PaymentRes getPaymentByOrderId(String orderId) {
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment tidak ditemukan"));
        return mapToResponse(payment);
    }

    private PaymentRes processPaymentSuccess(PaymentEntity payment) {
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDate.now());
        paymentRepository.save(payment);

        TenantRes tenant = tenantClient.getTenantById(payment.getTenantId());

        try {
            tenantClient.approvePayment(tenant.getId(), payment.getRoomId());
        } catch (Exception e) {
            // Log but don't fail payment
            e.printStackTrace();
        }

        try {
            RoomRes room = roomClient.getRoomById(payment.getRoomId());
            File pdf = pdfGeneratorUtil.generateInvoicePdf(payment, tenant, room.getRoomType());

            if (tenant.getEmail() != null) {
                String body = "Halo " + tenant.getName() + ",<br><br>Pembayaran Anda telah berhasil.<br>Invoice resmi terlampir dalam bentuk PDF.<br><br>Terima kasih telah menggunakan Smart Kost.";
                emailService.sendEmailWithAttachment(tenant.getEmail(), "Pembayaran Berhasil - Smart Kost", body, pdf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapToResponse(payment);
    }

    @Override
    public PaymentRes uploadProof(Long paymentId, MultipartFile file, String paymentMethod) {
        PaymentEntity payment = getPaymentEntityById(paymentId);

        try {
            String filename = uploadUtil.saveFile(file);
            payment.setProofImage(filename);
            payment.setPaymentMethod(paymentMethod);
            payment.setStatus(PaymentStatus.WAITING_CONFIRMATION);

            paymentRepository.save(payment);

            return mapToResponse(payment);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Upload bukti pembayaran gagal",
                    e);

        }

    }
}
