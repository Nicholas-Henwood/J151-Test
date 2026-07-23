package za.ac.vzap.trytons.frontend.client.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ProfileResponse {
    private UUID userId;
    private String email;
    private String username;
    private String role;
    @JsonProperty("isActive")
    private Boolean isActive;
    private String profilePic;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginAt;
    private String registrationStatus;
}
