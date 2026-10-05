package com.example.tenant_service.repository;
import com.example.tenant_service.entity.TenantEntity;
import com.example.tenant_service.entity.TenantStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TenantRepository extends JpaRepository<TenantEntity, Long> {

    List<TenantEntity> findByStatus(TenantStatusEntity status);

    List<TenantEntity> findByRoomId(Long roomId);

    boolean existsByRoomIdAndStatus(Long roomId, TenantStatusEntity status);

    List<TenantEntity> findByUserId(Long userId);

    List<TenantEntity> findByNameContainingIgnoreCase(String keyword);
    long countByStatus(TenantStatusEntity status);

    // Check if NIK already exists
    boolean existsByNik(String nik);

    // Check if NIK exists with specific statuses (ACTIVE or PENDING)
    boolean existsByNikAndStatusIn(String nik, java.util.Collection<TenantStatusEntity> statuses);
}