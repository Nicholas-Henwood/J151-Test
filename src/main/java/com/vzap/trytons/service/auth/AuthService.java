package com.vzap.trytons.service.auth;

import com.vzap.trytons.dto.auth.AuthStatusResponseDTO;
import com.vzap.trytons.dto.auth.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO authenticate(String identifier, String password);
    String logout();
    AuthStatusResponseDTO getAuthStatus(String requestingUserId);
}
