package com.example.complaint_service.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TenantClient {
     private final RestClient restClient;

    public TenantClient() {

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8083")
                .build();
    }

    public Object getTenant(Long tenantId) {

        return restClient.get()
                .uri("/api/tenants/{id}", tenantId)
                .retrieve()
                .body(Object.class);
    }

    public java.util.List<java.util.Map<String, Object>> getTenantsByUserId(Long userId) {
        return restClient.get()
                .uri("/api/tenants/user/{userId}", userId)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<java.util.List<java.util.Map<String, Object>>>() {});
    }
}
