package com.example.auth.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.auth.entity.RefreshTokenEntity;
import com.example.auth.entity.UserEntity;
import com.example.auth.exception.UnauthorizedException;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.service.RefreshTokenService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

        private final RefreshTokenRepository refreshTokenRepository;

        private static final long REFRESH_DAYS = 7;

        @Override
        public RefreshTokenEntity createRefreshToken(
                        UserEntity user) {

                RefreshTokenEntity refreshToken = refreshTokenRepository
                                .findByUser_Id(user.getId())
                                .orElse(
                                                RefreshTokenEntity.builder()
                                                                .user(user)
                                                                .build());

                refreshToken.setToken(
                                UUID.randomUUID().toString());

                refreshToken.setExpiryDate(
                                LocalDateTime.now().plusDays(7));

                refreshToken.setRevoked(false);

                return refreshTokenRepository.save(
                                refreshToken);
        }

        @Override
        public RefreshTokenEntity verifyToken(String token) {

                RefreshTokenEntity refreshToken = refreshTokenRepository
                                .findByToken(token)
                                .orElseThrow(() -> new UnauthorizedException(
                                                "Refresh token tidak valid"));

                if (refreshToken.getRevoked()) {
                        throw new UnauthorizedException(
                                        "Refresh token sudah dicabut");
                }

                if (refreshToken.getExpiryDate()
                                .isBefore(LocalDateTime.now())) {

                        throw new UnauthorizedException(
                                        "Refresh token expired");
                }

                return refreshToken;
        }

        @Override
        public void deleteByUserId(
                        Long userId) {

                refreshTokenRepository
                                .deleteByUser_Id(userId);
        }
}