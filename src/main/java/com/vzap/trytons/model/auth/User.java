package com.vzap.trytons.model.auth;

import com.vzap.trytons.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class User {
    private UUID userId;

    private String email;
    private String passwordHash;
    private String username;

    private UserRole role;

    private Boolean isActive;

    private String profilePic;

    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginAt;
}