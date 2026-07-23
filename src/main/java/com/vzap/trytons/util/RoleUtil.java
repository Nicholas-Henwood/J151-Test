package com.vzap.trytons.util;

import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.security.AuthPrincipal;

public final class RoleUtil {
    // Utility class: prevent instantiation.
    private RoleUtil() {
    }

    private static boolean isActive(User user) {
        return Boolean.TRUE.equals(user.getIsActive());
    }

    public static boolean isAdmin(AuthPrincipal principal) {
        return principal != null && principal.getRole() == UserRole.ADMINISTRATOR;
    }

    public static boolean isLoggedIn(User user) {
        return user != null && isActive(user);
    }
}
