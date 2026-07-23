package com.vzap.trytons.dto.shared;

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
public class ErrorResponseDTO {
    private boolean success;
    private String message;
    private String errorCode;

    public static ErrorResponseDTO of(String message, String errorCode) {
        return new ErrorResponseDTO(false, message, errorCode);
    }

    public static ErrorResponseDTO authRequired(){
        return new ErrorResponseDTO(false, "You need to be logged in.","AUTH_REQUIRED");
    }

    public static ErrorResponseDTO invalidAuthToken(){
        return new ErrorResponseDTO(false, "Invalid or expired token.","INVALID_AUTH_TOKEN");
    }

    public static ErrorResponseDTO adminAuth(){
        return new ErrorResponseDTO(false, "Admin authorisation required", "ADMIN_REQUIRED");
    }

    public static ErrorResponseDTO internalServerError(){
        return new ErrorResponseDTO(false, "Unexpected error occurred", "INTERNAL_SERVER_ERROR");
    }
}