package com.example.tenant_service.payload.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantRes {
    private long id;
    private long roomId;
    private String name;
    private String phone;
    private String email;
    private String startDate;
    private String endDate;
    private String monthlyRent;
    private String status;


}
