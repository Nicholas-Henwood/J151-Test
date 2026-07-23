package za.ac.vzap.trytons.frontend.util;

import jakarta.enterprise.context.SessionScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.ac.vzap.trytons.frontend.client.auth.LoginResponse;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@SessionScoped
@Getter
@Setter
@NoArgsConstructor

public class SessionAuthContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String ADMIN_ROLE = "ADMINISTRATOR";

    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USERNAME = "username";
    public static final String SESSION_EMAIL = "email";
    public static final String SESSION_ROLE = "role";
    public static final String SESSION_AUTHENTICATED = "authenticated";

    private UUID userId;
    private String username;
    private String email;
    private String role;
    private String token;

    public void signIn(LoginResponse response){
        if(response == null){
            clear();
            return;
        }
        this.userId = response.getUserId();
        this.username = response.getUsername();
        this.email = response.getEmail();
        this.role = response.getRole();
        this.token = response.getToken();
    }

    public void clear(){
        userId = null;
        username = null;
        email = null;
        role = null;
        token = null;
    }

    public boolean isAuthenticated(){
        return userId != null && token != null && !token.isBlank();
    }

    public boolean isAdmin(){
        return isAuthenticated() && ADMIN_ROLE.equalsIgnoreCase(role);
    }

}
