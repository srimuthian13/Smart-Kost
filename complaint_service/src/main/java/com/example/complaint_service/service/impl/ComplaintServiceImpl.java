package com.example.complaint_service.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.complaint_service.client.TenantClient;
import com.example.complaint_service.client.TenantResponse;
import com.example.complaint_service.entity.ComplaintEntity;
import com.example.complaint_service.entity.ComplaintStatus;
import com.example.complaint_service.exception.NotFoundException;
import com.example.complaint_service.payload.req.ComplaintReq;
import com.example.complaint_service.payload.res.ComplaintRes;
import com.example.complaint_service.repository.ComplaintRepository;
import com.example.complaint_service.service.ComplaintService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

        private final ComplaintRepository repository;
        private final TenantClient tenantClient;

        @Override
        public ComplaintRes create(Long userId, ComplaintReq request) {

                // Find active tenant for this user
                java.util.List<java.util.Map<String, Object>> tenants = tenantClient.getTenantsByUserId(userId);
                Long activeTenantId = null;
                if (tenants != null) {
                    for (java.util.Map<String, Object> t : tenants) {
                        if ("ACTIVE".equals(t.get("status"))) {
                            activeTenantId = ((Number) t.get("id")).longValue();
                            break;
                        }
                    }
                }
                
                if (activeTenantId == null) {
                        throw new NotFoundException("Active tenant not found for user");
                }

                ComplaintEntity complaint = ComplaintEntity.builder()
                                .tenantId(activeTenantId)
                                .roomId(request.getRoomId())
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .status(ComplaintStatus.PENDING)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                repository.save(complaint);

                return mapToResponse(complaint);
        }

        @Override
        public ComplaintRes findById(Long id) {

                ComplaintEntity complaint = repository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Complaint not found"));

                return mapToResponse(complaint);
        }
@Override
public Long countActiveComplaints() {

    return repository.countByStatus(
            ComplaintStatus.PROCESS);
}
        @Override
        public List<ComplaintRes> findAll() {

                return repository.findAll()
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public List<ComplaintRes> findByUserId(Long userId) {
                java.util.List<java.util.Map<String, Object>> tenants = tenantClient.getTenantsByUserId(userId);
                if (tenants == null || tenants.isEmpty()) {
                    return List.of();
                }
                java.util.List<Long> tenantIds = tenants.stream().map(t -> ((Number) t.get("id")).longValue()).toList();
                
                return repository.findAll().stream()
                        .filter(c -> tenantIds.contains(c.getTenantId()))
                        .map(this::mapToResponse)
                        .toList();
        }

        @Override
        public List<ComplaintRes> findByTenant(Long tenantId) {

                return repository.findByTenantId(tenantId)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public ComplaintRes updateStatus(
                        Long id,
                        ComplaintStatus status) {

                ComplaintEntity complaint = repository.findById(id)
                                .orElseThrow();

                complaint.setStatus(status);

                repository.save(complaint);

                return mapToResponse(complaint);
        }

        private ComplaintRes mapToResponse(
                        ComplaintEntity entity) {

                return ComplaintRes.builder()
                                .id(entity.getId())
                                .tenantId(entity.getTenantId())
                                .roomId(entity.getRoomId())
                                .title(entity.getTitle())
                                .description(entity.getDescription())
                                .status(entity.getStatus())
                                .createdAt(entity.getCreatedAt())
                                .build();
        }

        @Override
        public void delete(Long id) {

                ComplaintEntity complaint = repository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Complaint not found"));

                repository.delete(complaint);
        }
}