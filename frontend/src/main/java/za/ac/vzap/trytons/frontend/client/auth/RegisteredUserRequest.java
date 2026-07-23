package za.ac.vzap.trytons.frontend.client.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisteredUserRequest {
    private String email;
    private String username;
    private String rawPassword;
}
