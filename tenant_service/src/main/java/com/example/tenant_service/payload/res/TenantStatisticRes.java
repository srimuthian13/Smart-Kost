package com.example.tenant_service.payload.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantStatisticRes {
    private Long totalTenants;
    private Long activeTenants;
    private Long checkedOutTenants;
}
