package com.example.tenant_service.controller;


import com.example.tenant_service.payload.req.TenantReq;
import com.example.tenant_service.payload.res.TenantRes;
import com.example.tenant_service.payload.res.TenantStatisticRes;
import com.example.tenant_service.service.TenantService;

import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService tenantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)

    public TenantRes createTenant(@Valid @RequestBody TenantReq tenantReq) {
        return tenantService.createTenant(tenantReq);
    }

    @PostMapping("/{id}/checkout")
    public TenantRes checkOutTenant(@PathVariable Long id) {
        return tenantService.checkOutTenant(id);
    }

    @GetMapping("/{id}")
    public TenantRes getTenantById(@PathVariable Long id) {
        return tenantService.getTenantById(id);
    }

    @GetMapping
    public ResponseEntity<?> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/active")
    public List<TenantRes> getActiveTenants() {
        return tenantService.getActiveTenants();
    }

    @GetMapping("/room/{roomId}")
    public List<TenantRes> getTenantsByRoom(@PathVariable Long roomId) {
        return tenantService.getTenantsByRoom(roomId);
    }

    @GetMapping("/user/{userId}")
    public List<TenantRes> getTenantsByUserId(@PathVariable Long userId) {
        return tenantService.getTenantsByUserId(userId);
    }

    @GetMapping("/history")
    public List<TenantRes> getTenantHistory() {

        return tenantService.getTenantHistory();
    }

    @GetMapping("/statistics")
    public TenantStatisticRes getStatistic() {

        return tenantService.getStatistic();
    }

    @GetMapping("/search")
    public List<TenantRes> searchTenant(
            @RequestParam String keyword) {

        return tenantService.searchTenant(keyword);
    }

    @PostMapping("/approve-payment")
    public ResponseEntity<?> approvePayment(@RequestParam Long tenantId, @RequestParam Long roomId) {
        tenantService.approvePayment(tenantId, roomId);
        return ResponseEntity.ok("Tenant and room updated");
    }

     @PutMapping("/{tenantId}")
    public TenantRes updateTenant(
            @PathVariable Long tenantId,
            @Valid @RequestBody TenantReq request) {

        return tenantService.updateTenant(tenantId, request);
    }

    @GetMapping("/active/count")
public ResponseEntity<Long> countActiveTenants() {

    return ResponseEntity.ok(
            tenantService.countActiveTenants());

}

@GetMapping("/pending/count")
public ResponseEntity<Long> countPendingTenants() {

    return ResponseEntity.ok(
            tenantService.countPendingTenants());

}

    @PostMapping("/blacklist")
    public ResponseEntity<?> blacklistTenant(@RequestParam Long tenantId) {
        tenantService.blacklistTenant(tenantId);
        return ResponseEntity.ok("Tenant blacklisted");
    }

@GetMapping("/checkout/count")
public ResponseEntity<Long> countCheckoutTenants() {

    return ResponseEntity.ok(
            tenantService.countCheckoutTenants());

}

}