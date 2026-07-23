package com.vzap.trytons.dao.auth;

import com.vzap.trytons.model.auth.RegisteredUser;

import java.util.Optional;
import java.util.UUID;

public interface RegisteredUserDAO {
    Optional<RegisteredUser> getRegisteredUserById(UUID userId);
    Optional<RegisteredUser> updateProfile(RegisteredUser registeredUser);
    boolean deactivateAccount(UUID userId);
    Optional<RegisteredUser> register(RegisteredUser newUser);
}
