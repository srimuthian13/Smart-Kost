package com.example.complaint_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.complaint_service.entity.ComplaintStatus;
import com.example.complaint_service.payload.req.ComplaintReq;
import com.example.complaint_service.service.ComplaintService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaint API")
public class ComplaintController {

        private final ComplaintService service;

        @Operation(summary = "Create Complaint")
        @PostMapping
        public ResponseEntity<?> create(
                        @RequestHeader("X-USER-ID") Long userId,
                        @RequestBody ComplaintReq request) {

                return ResponseEntity.ok(
                                service.create(
                                                userId,
                                                request));
        }

        @GetMapping
        public ResponseEntity<?> findAll(
                        @RequestHeader(value = "X-USER-ROLE", required = false) String role,
                        @RequestHeader(value = "X-USER-ID", required = false) Long userId) {

                if ("ADMIN".equals(role)) {
                        return ResponseEntity.ok(service.findAll());
                } else if ("TENANT".equals(role) && userId != null) {
                        return ResponseEntity.ok(service.findByUserId(userId));
                } else {
                        return ResponseEntity.status(403).body("Access Denied");
                }
        }

        @GetMapping("/tenant/{tenantId}")
        public ResponseEntity<?> findByTenant(
                        @PathVariable Long tenantId) {

                return ResponseEntity.ok(
                                service.findByTenant(tenantId));
        }

        @Operation(summary = "Update Complaint Status")
        @PutMapping("/{id}/status")
        public ResponseEntity<?> updateStatus(
                        @PathVariable Long id,
                        @RequestParam ComplaintStatus status) {

                return ResponseEntity.ok(
                                service.updateStatus(id, status));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<?> delete(
                        @PathVariable Long id) {

                service.delete(id);

                return ResponseEntity.ok(
                                "Complaint deleted");
        }

        @GetMapping("/{id}")
        public ResponseEntity<?> findById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                service.findById(id));
        }

        @GetMapping("/active/count")
public ResponseEntity<java.util.Map<String, Long>> countActiveComplaints() {
    return ResponseEntity.ok(java.util.Collections.singletonMap("value", service.countActiveComplaints()));
}
}