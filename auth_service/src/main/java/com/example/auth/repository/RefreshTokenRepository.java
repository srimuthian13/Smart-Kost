package com.example.auth.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import com.example.auth.entity.RefreshTokenEntity;


@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    // untuk refresh token
    Optional<RefreshTokenEntity> findByToken(String token);

    // untuk dapet token baru milik user
    Optional<RefreshTokenEntity> findByUser_Id(Long userId);

    // agar token lama gak bisa di pake lagi
    @Modifying
    @Transactional
    void deleteByUser_Id(Long userId);

}
