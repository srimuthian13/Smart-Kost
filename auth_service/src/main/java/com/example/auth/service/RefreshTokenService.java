package com.example.auth.service;

import com.example.auth.entity.UserEntity;
import com.example.auth.entity.RefreshTokenEntity;

public interface RefreshTokenService {
    RefreshTokenEntity createRefreshToken(UserEntity user);
    RefreshTokenEntity verifyToken(String token);

    void deleteByUserId(Long userId);
}
