package com.example.payment_service.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.payment_service.entity.PaymentEntity;
import com.example.payment_service.entity.PaymentStatus;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByTenantId(Long tenantId);

    Optional<PaymentEntity> findByOrderId(String orderId);

   
    List<PaymentEntity> findByStatus(PaymentStatus status);

   
    List<PaymentEntity> findByStatusAndDueDateBefore(
            PaymentStatus status,
            LocalDate date);


    Long countByStatus(PaymentStatus status);

   
    boolean existsByTenantIdAndBillingMonthAndBillingYear(
            Long tenantId,
            Integer billingMonth,
            Integer billingYear);

   
    @Query("""
        SELECT COALESCE(SUM(p.amount),0)
        FROM PaymentEntity p
        WHERE p.status = 'PAID'
        """)
    BigDecimal getTotalRevenue();


    @Query("""
        SELECT COALESCE(SUM(p.amount),0)
        FROM PaymentEntity p
        WHERE p.status = 'PAID'
        AND p.billingMonth = :month
        AND p.billingYear = :year
        """)
    BigDecimal getMonthlyIncome(
            @Param("month") Integer month,
            @Param("year") Integer year);

   
    @Query("""
        SELECT
            MONTH(p.paymentDate),
            SUM(p.amount)
        FROM PaymentEntity p
        WHERE p.status = 'PAID'
        GROUP BY MONTH(p.paymentDate)
        ORDER BY MONTH(p.paymentDate)
        """)
    List<Object[]> getMonthlyRevenue();

}