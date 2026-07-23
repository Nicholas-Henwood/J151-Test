package com.vzap.trytons.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequestDTO {
    @NotBlank(message = "Current password can not be blank")
    private String currentPassword;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 8, message = "New password needs to be at least 8 characters long")
    private String newPassword;
}
