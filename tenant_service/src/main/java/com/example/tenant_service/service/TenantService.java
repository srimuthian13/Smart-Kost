package com.example.tenant_service.service;

import com.example.tenant_service.payload.req.TenantReq;
import com.example.tenant_service.payload.res.TenantRes;
import com.example.tenant_service.payload.res.TenantStatisticRes;

import java.util.List;

public interface TenantService {

    /**
     * Approve payment and activate tenant, occupy room.
     * @param tenantId the tenant id
     * @param roomId the room id
     */
    void approvePayment(Long tenantId, Long roomId);

    TenantRes createTenant(TenantReq tenantReq);

    TenantRes checkOutTenant(Long tenantId);

    TenantRes getTenantById(Long tenantId);

    TenantRes updateTenant(Long tenantId, TenantReq request);

    List<TenantRes> getAllTenants();

    List<TenantRes> getActiveTenants();

    List<TenantRes> getTenantsByRoom(Long roomId);

    List<TenantRes> getTenantsByUserId(Long userId);

    List<TenantRes> searchTenant(String keyword);

    List<TenantRes> getTenantHistory();

    Long countActiveTenants();

    Long countPendingTenants();

    Long countCheckoutTenants();

    TenantStatisticRes getStatistic();

    /**
     * Blacklist a tenant without vacating the room.
     */
    void blacklistTenant(Long tenantId);
}
