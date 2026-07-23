package com.vzap.trytons.dto.auth;

import com.vzap.trytons.enums.RegistrationStatus;
import com.vzap.trytons.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisteredUserResponseDTO {
    private UUID userId;

    private String username;

    private UserRole role;

    private RegistrationStatus status;
}
