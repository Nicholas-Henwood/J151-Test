package za.ac.vzap.trytons.frontend.client.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserSearchResponse {
    private UUID userId;
    private String email;
    private String username;
    private String role;
    @JsonProperty("isActive")
    private boolean isActive;
}