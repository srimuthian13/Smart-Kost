package com.example.payment_service.payload.res;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class RoomRes {

    private Long id;
    private String roomNumber;
    private String roomType;
    private BigDecimal price;
}