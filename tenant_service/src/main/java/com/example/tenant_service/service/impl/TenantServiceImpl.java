package com.example.tenant_service.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;


import com.example.tenant_service.client.RoomServiceClient;
import com.example.tenant_service.entity.TenantEntity;
import com.example.tenant_service.entity.TenantStatusEntity;
import com.example.tenant_service.exception.RoomUnvailableException;
import com.example.tenant_service.exception.DuplicateNikException;
import com.example.tenant_service.exception.TenantNotFoundException;
import com.example.tenant_service.payload.req.TenantReq;
import com.example.tenant_service.payload.res.TenantRes;
import com.example.tenant_service.payload.res.TenantStatisticRes;
import com.example.tenant_service.repository.TenantRepository;
import com.example.tenant_service.service.TenantService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
    private final TenantRepository tenantRepository;
    private final RoomServiceClient roomServiceClient;
    private final com.example.tenant_service.client.PaymentServiceClient paymentServiceClient;

    @Override
    public TenantRes createTenant(TenantReq tenantReq) {
        // Validate duplicate NIK — only block if ACTIVE or PENDING
        boolean nikActiveOrPending = tenantRepository.existsByNikAndStatusIn(
                tenantReq.getNik(),
                List.of(TenantStatusEntity.ACTIVE, TenantStatusEntity.PENDING));
        if (nikActiveOrPending) {
            throw new DuplicateNikException("NIK sudah terdaftar dengan status aktif/pending: " + tenantReq.getNik());
        }

        // Validate room availability
        boolean roomOccupied = tenantRepository.existsByRoomIdAndStatus(tenantReq.getRoomId(),
                TenantStatusEntity.ACTIVE);
        if (roomOccupied) {
            throw new RoomUnvailableException("Room is already occupied");
        }

        // Create tenant with PENDING status; room will be occupied after payment approval
        TenantEntity tenant = TenantEntity.builder()
                .userId(tenantReq.getUserId())
                .roomId(tenantReq.getRoomId())
                .name(tenantReq.getName())
                .phone(tenantReq.getPhone())
                .email(tenantReq.getEmail())
                .nik(tenantReq.getNik())
                .address(tenantReq.getAddress())
                .occupation(tenantReq.getOccupation())
                .startDate(tenantReq.getStartDate())
                .endDate(tenantReq.getEndDate())
                .monthlyRent(tenantReq.getMonthlyRent())
                .status(TenantStatusEntity.PENDING)
                .build();

        TenantEntity savedTenant = tenantRepository.save(tenant);

        // Do NOT occupy room here; will be done after payment approval
        return mapToTenantRes(savedTenant);
    }

    @Override
    public TenantRes checkOutTenant(Long tenantId) {
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));

        if (tenant.getStatus() == TenantStatusEntity.CHECKOUT) {
            throw new IllegalStateException(
                    "Tenant already checked out");
        }

        tenant.setStatus(TenantStatusEntity.CHECKOUT);
        tenant.setCheckOutDate(LocalDate.now());
        TenantEntity updatedTenant = tenantRepository.save(tenant);

        // Vacate room
        roomServiceClient.vacateRoom(updatedTenant.getRoomId());

        return mapToTenantRes(updatedTenant);
    }

    @Override
    public TenantRes getTenantById(Long tenantId) {
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));
        return mapToTenantRes(tenant);
    }

    @Override
    public List<TenantRes> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(this::mapToTenantRes)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantRes> getActiveTenants() {
        return tenantRepository.findByStatus(TenantStatusEntity.ACTIVE).stream()
                .map(this::mapToTenantRes)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantRes> getTenantsByRoom(Long roomId) {
        return tenantRepository.findByRoomId(roomId).stream()
                .map(this::mapToTenantRes)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantRes> getTenantsByUserId(Long userId) {
        return tenantRepository.findByUserId(userId).stream()
                .map(this::mapToTenantRes)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantRes> searchTenant(
            String keyword) {

        return tenantRepository
                .findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToTenantRes)
                .toList();
    }

    @Override
    public List<TenantRes> getTenantHistory() {
        return tenantRepository
                .findByStatus(
                        TenantStatusEntity.CHECKOUT)
                .stream()
                .map(this::mapToTenantRes)
                .toList();
    }
@Override
public Long countActiveTenants() {

    return tenantRepository.countByStatus(
            TenantStatusEntity.ACTIVE);

}
@Override
public Long countPendingTenants() {

    return tenantRepository.countByStatus(
            TenantStatusEntity.PENDING);

}

@Override
public Long countCheckoutTenants() {

    return tenantRepository.countByStatus(
            TenantStatusEntity.CHECKOUT);

}
    @Override
    public TenantRes updateTenant(
            Long tenantId,
            TenantReq request) {

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(
                        "Tenant not found"));

        tenant.setName(request.getName());
        tenant.setPhone(request.getPhone());
        tenant.setEmail(request.getEmail());
        tenant.setNik(request.getNik());
        tenant.setAddress(request.getAddress());
        tenant.setOccupation(request.getOccupation());

        tenant.setStartDate(request.getStartDate());
        tenant.setEndDate(request.getEndDate());
        tenant.setMonthlyRent(request.getMonthlyRent());

        TenantEntity updatedTenant = tenantRepository.save(tenant);

        return mapToTenantRes(updatedTenant);
    }

    @Override
    public TenantStatisticRes getStatistic() {

        long total = tenantRepository.count();

        long active = tenantRepository
                .findByStatus(
                        TenantStatusEntity.ACTIVE)
                .size();

        long checkedOut = tenantRepository
                .findByStatus(
                        TenantStatusEntity.CHECKOUT)
                .size();

        return TenantStatisticRes.builder()
                .totalTenants(total)
                .activeTenants(active)
                .checkedOutTenants(checkedOut)
                .build();
    }

    @Override
    public void blacklistTenant(Long tenantId) {
        // Fetch tenant
        Optional<TenantEntity> tenantOpt = tenantRepository.findById(tenantId);
        if (!tenantOpt.isPresent()) {
            throw new RuntimeException("Tenant not found");
        }
        TenantEntity tenant = tenantOpt.get();
        tenant.setStatus(TenantStatusEntity.BLACKLIST);
        tenantRepository.save(tenant);
        
        // Make room available
        roomServiceClient.vacateRoom(tenant.getRoomId());
    }

    @Override
    public void approvePayment(Long tenantId, Long roomId) {
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));
        tenant.setStatus(TenantStatusEntity.ACTIVE);
        tenant.setRoomId(roomId);
        tenantRepository.save(tenant);
        // occupy the room via room service client
        roomServiceClient.occupyRoom(roomId);
    }

    private TenantRes mapToTenantRes(TenantEntity tenant) {
        return TenantRes.builder()
                .id(tenant.getId())
                .roomId(tenant.getRoomId())
                .name(tenant.getName())
                .phone(tenant.getPhone())
                .email(tenant.getEmail())
                .startDate(tenant.getStartDate().toString())
                .endDate(tenant.getEndDate().toString())
                .monthlyRent(tenant.getMonthlyRent().toString())
                .status(tenant.getStatus().name())
                .build();
    }

}
