package za.ac.vzap.trytons.frontend.client.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {
    private String username;
    private String email;
    private String profilePic;
}
