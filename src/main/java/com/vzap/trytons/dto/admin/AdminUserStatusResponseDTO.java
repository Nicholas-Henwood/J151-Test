package com.vzap.trytons.dto.admin;

import com.vzap.trytons.enums.UserRole;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class AdminUserStatusResponseDTO {
    private UUID userId;

    private String email;
    private String username;

    private UserRole role;

    private Boolean isActive;
}
