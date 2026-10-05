package com.example.payment_service.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.payment_service.entity.PaymentStatus;
import com.example.payment_service.payload.req.PaymentReq;
import com.example.payment_service.payload.res.MonthlyRevenueRes;
import com.example.payment_service.payload.res.PaymentRes;
import com.example.payment_service.payload.res.PaymentSummaryRes;

public interface PaymentService {


    PaymentRes createPayment(PaymentReq request);

   
    PaymentRes getPaymentById(Long paymentId);


    PaymentRes getPaymentByOrderId(String orderId);


    PaymentRes uploadProof(
            Long paymentId,
            MultipartFile file,
            String paymentMethod);

  
    PaymentRes confirmPayment(Long paymentId);

    PaymentRes rejectPayment(Long paymentId);

    PaymentRes cancelPayment(Long paymentId);

    PaymentRes qrisPay(Long paymentId);

    List<PaymentRes> getAllPayments();

    List<PaymentRes> getPaymentsByTenant(Long tenantId);

    List<PaymentRes> getPaymentsByStatus(PaymentStatus status);

    PaymentSummaryRes getSummary();

    List<MonthlyRevenueRes> getMonthlyRevenue();

   
    ByteArrayInputStream exportExcel() throws IOException;

}