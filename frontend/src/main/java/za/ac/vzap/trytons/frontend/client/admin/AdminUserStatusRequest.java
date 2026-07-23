package za.ac.vzap.trytons.frontend.client.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserStatusRequest {
    @JsonProperty("isActive")
    private boolean isActive;
}