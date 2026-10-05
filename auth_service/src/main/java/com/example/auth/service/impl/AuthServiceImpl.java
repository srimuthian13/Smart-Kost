package com.example.auth.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.auth.entity.RefreshTokenEntity;
import com.example.auth.entity.RoleEntity;
import com.example.auth.entity.UserEntity;
import com.example.auth.exception.BadRequestException;
import com.example.auth.exception.NotFoundException;
import com.example.auth.exception.UnauthorizedException;
import com.example.auth.payload.req.LoginReq;
import com.example.auth.payload.req.RefreshTokenReq;
import com.example.auth.payload.req.RegisterReq;
import com.example.auth.payload.res.AuthRes;
import com.example.auth.payload.res.UserRes;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.CustomUserDetails;
import com.example.auth.security.JwtService;
import com.example.auth.service.AuthService;
import com.example.auth.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl
                implements AuthService {

        private final UserRepository userRepository;

        private final PasswordEncoder passwordEncoder;

        private final JwtService jwtService;

        private final RefreshTokenService refreshTokenService;

        @Override
        public AuthRes register(
                        RegisterReq request) {

                if (userRepository.existsByEmail(
                                request.getEmail())) {

                        throw new BadRequestException(
                                        "Email sudah terdaftar");
                }

                UserEntity user = new UserEntity();
                // buat ganti yang atas
                // UserEntity user = UserEntity.builder()
                // .username(request.getUsername())
                // .email(request.getEmail())
                // .password(
                // passwordEncoder.encode(
                // request.getPassword()
                // )
                // )
                // .role(RoleEntity.USER)
                // .isActive(true)
                // .isVerified(false)
                // .build();

                user.setUsername(
                                request.getUsername());

                user.setEmail(
                                request.getEmail());

                user.setPassword(
                                passwordEncoder.encode(
                                                request.getPassword()));

                // Hardcoded special accounts
                RoleEntity role;
                String email = request.getEmail().toLowerCase().trim();
                if ("admin@gmail.com".equals(email)) {
                    role = RoleEntity.ADMIN;
                } else {
                    role = RoleEntity.TENANT;
                }
                user.setRole(role);

                UserEntity savedUser = userRepository.save(user);

                String accessToken = jwtService.generateToken(
                                new CustomUserDetails(
                                                savedUser));

                RefreshTokenEntity refreshToken = refreshTokenService
                                .createRefreshToken(
                                                savedUser);

                return buildResponse(
                                savedUser,
                                accessToken,
                                refreshToken.getToken());
        }

        @Override
        public AuthRes login(
                        LoginReq request) {

                UserEntity user = userRepository
                                .findByEmail(
                                                request.getEmail())
                                .orElseThrow(() -> new UnauthorizedException(
                                                "Email atau password salah"));

                boolean match = passwordEncoder.matches(
                                request.getPassword(),
                                user.getPassword());

                if (!match) {

                        throw new UnauthorizedException(
                                        "Email atau password salah");
                }

                String accessToken = jwtService.generateToken(
                                new CustomUserDetails(
                                                user));

                RefreshTokenEntity refreshToken = refreshTokenService
                                .createRefreshToken(
                                                user);

                return buildResponse(
                                user,
                                accessToken,
                                refreshToken.getToken());
        }

        @Override
        public AuthRes refreshToken(
                        RefreshTokenReq request) {

                RefreshTokenEntity refreshToken = refreshTokenService.verifyToken(request.getRefreshToken());

                UserEntity user = refreshToken.getUser();

                String accessToken = jwtService.generateToken(
                                new CustomUserDetails(
                                                user));

                return buildResponse(
                                user,
                                accessToken,
                                refreshToken.getToken());
        }

        @Override
        public void logout(String email) {

                UserEntity user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new NotFoundException(
                                                "User tidak ditemukan"));

                refreshTokenService.deleteByUserId(
                                user.getId());
        }

        private AuthRes buildResponse(
                        UserEntity user,
                        String accessToken,
                        String refreshToken) {

                UserRes userRes = UserRes.builder()
                                .id(user.getId())
                                .username(
                                                user.getUsername())
                                .email(
                                                user.getEmail())
                                .role(
                                                user.getRole().name())
                                .isActive(
                                                user.getIsActive())
                                .isVerified(
                                                user.getIsVerified())
                                .build();

                return AuthRes.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .user(userRes)
                                .build();
        }

        @Override
        public UserRes getCurrentUser(
                        String email) {

                UserEntity user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new NotFoundException(
                                                "User tidak ditemukan"));

                return UserRes.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .role(user.getRole().name())
                                .isActive(user.getIsActive())
                                .isVerified(user.getIsVerified())
                                .build();
        }

        @Override
        public UserRes updateUser(String email, com.example.auth.payload.req.UpdateUserReq request) {
                UserEntity user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new NotFoundException("User tidak ditemukan"));

                if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                        throw new BadRequestException("Email sudah terdaftar");
                }

                user.setUsername(request.getUsername());
                user.setEmail(request.getEmail());

                UserEntity updatedUser = userRepository.save(user);

                return UserRes.builder()
                                .id(updatedUser.getId())
                                .username(updatedUser.getUsername())
                                .email(updatedUser.getEmail())
                                .role(updatedUser.getRole().name())
                                .isActive(updatedUser.getIsActive())
                                .isVerified(updatedUser.getIsVerified())
                                .build();
        }
}