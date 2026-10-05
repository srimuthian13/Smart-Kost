package com.example.auth.payload.res;

import lombok.Data;
import lombok.Builder;

@Data
@Builder

public class UserRes {

    private Long id;
    private String username;
    private String email;
    private String role;
    private Boolean isActive;
    private Boolean isVerified;
    
}
