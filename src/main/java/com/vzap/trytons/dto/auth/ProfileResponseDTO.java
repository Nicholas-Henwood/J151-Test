package com.vzap.trytons.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vzap.trytons.enums.RegistrationStatus;
import com.vzap.trytons.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDTO {
    private UUID userId;

    private String email;
    private String username;

    private UserRole role;

    @JsonProperty("isActive")
    private Boolean isActive;

    private String profilePic;

    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginAt;

    private RegistrationStatus registrationStatus;
}
