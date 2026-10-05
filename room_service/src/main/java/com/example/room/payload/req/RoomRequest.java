package com.example.room.payload.req;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomRequest(

    @NotBlank(message = "Room number is required")
    String roomNumber,

    @NotBlank(message = "Room type is required")
    String roomType,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0")
    BigDecimal price,

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    Integer capacity,

    String description,

    List<String> facilities,

    List<String> images 

) {
}