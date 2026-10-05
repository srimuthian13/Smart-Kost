package com.example.payment_service.payload.res;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyRevenueRes {
    private Integer month;
    private BigDecimal revenue;
}
