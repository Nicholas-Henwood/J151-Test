package com.vzap.trytons.security;

import com.vzap.trytons.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.security.Principal;
import java.util.UUID;
@Getter
@AllArgsConstructor
@Builder
public class AuthPrincipal implements Principal {
    private UUID userId;

    private String username;
    private String email;

    private UserRole role;

    @Override
    public String getName() {
        return username;
    }
}
