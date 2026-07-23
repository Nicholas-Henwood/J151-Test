package za.ac.vzap.trytons.frontend.client.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Mirrors the backend ErrorResponseDTO contract: {success, message, errorCode}
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
}
