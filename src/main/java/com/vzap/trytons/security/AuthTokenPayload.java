package com.vzap.trytons.security;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.UUID;
@Getter
@Builder
public class AuthTokenPayload {
    private UUID userId;

    private Date expiresAt;
}
