package com.vzap.trytons.dao.admin;

import com.vzap.trytons.model.admin.Admin;

import java.util.Optional;
import java.util.UUID;

public interface AdminDAO {
    Optional<Admin> getAdminById(UUID userId);
    boolean deactivateUserAccount(UUID userId);
}
