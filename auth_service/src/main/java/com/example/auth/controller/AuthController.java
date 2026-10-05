package com.example.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.auth.payload.req.LoginReq;
import com.example.auth.payload.req.RefreshTokenReq;
import com.example.auth.payload.req.RegisterReq;
import com.example.auth.payload.res.AuthRes;
import com.example.auth.payload.res.UserRes;
import com.example.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthRes> register(
            @Valid @RequestBody RegisterReq request) {

        AuthRes response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRes> login(
            @Valid @RequestBody LoginReq request) {

        AuthRes response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthRes> refreshToken(
            @Valid @RequestBody RefreshTokenReq request) {

        AuthRes response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication) {

        String email = authentication.getName();

        authService.logout(email);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserRes me(Authentication authentication) {

        return authService.getCurrentUser(
                authentication.getName());
    }

    @PutMapping("/update")
    public ResponseEntity<UserRes> update(
            Authentication authentication,
            @Valid @RequestBody com.example.auth.payload.req.UpdateUserReq request) {

        String email = authentication.getName();
        UserRes response = authService.updateUser(email, request);
        return ResponseEntity.ok(response);
    }
}