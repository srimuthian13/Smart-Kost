package com.example.auth.service;

import com.example.auth.payload.req.LoginReq;
import com.example.auth.payload.req.RegisterReq;
import com.example.auth.payload.req.RefreshTokenReq;
import com.example.auth.payload.res.AuthRes;
import com.example.auth.payload.res.UserRes;

public interface AuthService {

    AuthRes register(RegisterReq request);

    AuthRes login(LoginReq request);

    AuthRes refreshToken(RefreshTokenReq request);

    void logout(String email);

    UserRes getCurrentUser(String email);

    UserRes updateUser(String email, com.example.auth.payload.req.UpdateUserReq request);
}