package com.vzap.trytons.util;

import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.security.AuthPrincipal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleUtilTest {

    @Test
    void adminPrincipalIsAdmin() {
        AuthPrincipal principal = AuthPrincipal.builder()
                .role(UserRole.ADMINISTRATOR)
                .build();

        assertTrue(RoleUtil.isAdmin(principal));
    }

    @Test
    void registeredUserPrincipalIsNotAdmin() {
        AuthPrincipal principal = AuthPrincipal.builder()
                .role(UserRole.REGISTERED_USER)
                .build();

        assertFalse(RoleUtil.isAdmin(principal));
    }
}
