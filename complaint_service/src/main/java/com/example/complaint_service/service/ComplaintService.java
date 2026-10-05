package com.example.complaint_service.service;

import java.util.List;


import com.example.complaint_service.entity.ComplaintStatus;
import com.example.complaint_service.payload.req.ComplaintReq;
import com.example.complaint_service.payload.res.ComplaintRes;

public interface ComplaintService {

        ComplaintRes create(Long tenantId,ComplaintReq request);

        ComplaintRes findById(Long id);

        List<ComplaintRes> findAll();

        List<ComplaintRes> findByUserId(Long userId);

        List<ComplaintRes> findByTenant(Long tenantId);

        ComplaintRes updateStatus(Long id, ComplaintStatus status);
Long countActiveComplaints();
        void delete(Long id);
}