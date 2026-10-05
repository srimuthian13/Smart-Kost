package com.example.payment_service.client;

import com.example.payment_service.payload.res.RoomRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "room-service",
        url = "${room.service.url}"
)
public interface RoomClient {

    @GetMapping("/api/rooms/{id}")
    RoomRes getRoomById(
            @PathVariable Long id
    );
}