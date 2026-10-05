package com.example.room.payload.res;



import com.example.room.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RoomResponse(

        Long id,

        String roomNumber,

        String roomType,

        BigDecimal price,

        Integer capacity,

        RoomStatus status,

        String description,

        List<String> facilities,

        List<String> images,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {}