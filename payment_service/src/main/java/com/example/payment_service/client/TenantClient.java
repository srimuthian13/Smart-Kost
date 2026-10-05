package com.example.payment_service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.payment_service.payload.res.TenantRes;

@FeignClient(name = "tenant-service", url = "${tenant.service.url}")
public interface TenantClient {

    @GetMapping("/api/tenants/{id}")
    TenantRes getTenantById(@PathVariable Long id);

    @GetMapping("/api/tenants/active")
    List<TenantRes> getActiveTenants();

    @PostMapping("/api/tenants/approve-payment")
    void approvePayment(@RequestParam("tenantId") Long tenantId, @RequestParam("roomId") Long roomId);
}