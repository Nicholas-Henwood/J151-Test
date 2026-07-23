package com.vzap.trytons.service.auth;

import com.vzap.trytons.dto.auth.ChangePasswordRequestDTO;
import com.vzap.trytons.dto.auth.ProfileResponseDTO;
import com.vzap.trytons.dto.auth.ProfileUpdateRequestDTO;
import com.vzap.trytons.dto.auth.RegisteredUserRequestDTO;
import com.vzap.trytons.model.auth.RegisteredUser;

import java.util.UUID;

public interface RegisteredUserServices {
    RegisteredUser registerUser(RegisteredUserRequestDTO newUser);
    ProfileResponseDTO getProfile(UUID userId);
    ProfileResponseDTO updateProfile(UUID actorUserId, ProfileUpdateRequestDTO request);
    void changePassword(UUID actorUserId, ChangePasswordRequestDTO request);
}