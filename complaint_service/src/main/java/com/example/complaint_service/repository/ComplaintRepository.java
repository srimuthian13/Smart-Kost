package com.example.complaint_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.complaint_service.entity.ComplaintEntity;
import com.example.complaint_service.entity.ComplaintStatus;

public interface ComplaintRepository extends JpaRepository<ComplaintEntity, Long> {

    List<ComplaintEntity> findByTenantId(Long tenantId);
Long countByStatus(ComplaintStatus status);
    List<ComplaintEntity> findByStatus(ComplaintStatus status);
}
