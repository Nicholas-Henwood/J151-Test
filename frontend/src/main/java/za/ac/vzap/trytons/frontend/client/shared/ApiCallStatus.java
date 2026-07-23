package za.ac.vzap.trytons.frontend.client.shared;

import jakarta.enterprise.context.RequestScoped;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@RequestScoped
@Getter
public class ApiCallStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final int NETWORK_FAILURE = 0;

    private int status;
    private ErrorResponse error;

    public void record(int status, ErrorResponse error) {
        this.status = status;
        this.error = error;
    }

    public void recordNetworkFailure() {
        record(NETWORK_FAILURE, null);
    }

    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }

    public boolean isUnauthorized() {
        return status == 401;
    }

    public boolean isForbidden() {
        return status == 403;
    }

    public boolean isNotFound() {
        return status == 404;
    }

    // Message from the backend ErrorResponse where one was returned, otherwise a status-appropriate fallback.
    public String getMessage(String fallback) {
        if (error != null && error.getMessage() != null && !error.getMessage().isBlank()) {
            return error.getMessage();
        }
        return switch (status) {
            case NETWORK_FAILURE -> "The service is currently unavailable. Please try again.";
            case 400 -> "The submitted details are invalid.";
            case 401 -> "Your session has expired. Please log in again.";
            case 403 -> "You are not authorised to perform this action.";
            case 404 -> "The requested item could not be found.";
            case 409 -> "That action conflicts with existing data.";
            case 422 -> "That action is not allowed by the competition rules.";
            default -> fallback;
        };
    }
}
