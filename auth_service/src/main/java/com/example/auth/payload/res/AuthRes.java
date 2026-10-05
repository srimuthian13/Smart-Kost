package com.example.auth.payload.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthRes {
    private String accessToken;
    private String refreshToken;
    private UserRes user;
}
