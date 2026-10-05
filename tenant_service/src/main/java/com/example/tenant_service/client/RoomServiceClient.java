package com.example.tenant_service.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Component
@RequiredArgsConstructor
public class RoomServiceClient {
    private final RestTemplate restTemplate;

    @Value("${room.service.url}")
    private String roomServiceUrl;

        public void occupyRoom(Long roomId){
            restTemplate.postForObject(roomServiceUrl + "/api/rooms/" + roomId + "/occupy", null, Void.class);
        }

        public void vacateRoom(Long roomId){
            restTemplate.postForObject(roomServiceUrl + "/api/rooms/" + roomId + "/vacate", null, Void.class);
        }
}
