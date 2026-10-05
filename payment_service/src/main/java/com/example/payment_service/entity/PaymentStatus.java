package com.example.payment_service.entity;

public enum PaymentStatus {
    UNPAID,
    WAITING_CONFIRMATION,
    PAID,
    REJECTED,
    OVERDUE,
    CANCELLED
}
