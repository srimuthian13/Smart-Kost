package com.example.complaint_service.payload.res;

import java.time.LocalDateTime;

import com.example.complaint_service.entity.ComplaintStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ComplaintRes {
    private Long id;

    private Long tenantId;

    private Long roomId;

    private String title;

    private String description;

    private ComplaintStatus status;

    private LocalDateTime createdAt;
}
