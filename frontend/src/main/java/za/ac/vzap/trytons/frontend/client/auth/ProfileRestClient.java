package za.ac.vzap.trytons.frontend.client.auth;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;
import za.ac.vzap.trytons.frontend.client.shared.ApiCallStatus;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class ProfileRestClient {

    private static final String PROFILE_PATH = "/users/profile";
    private static final String CHANGE_PASSWORD_PATH = "/users/profile/change-password";

    private static final Logger LOG = Logger.getLogger(ProfileRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    @Inject
    private ApiCallStatus apiCallStatus;

    public Optional<ProfileResponse> getProfile() {
        Optional<ProfileResponse> response = apiClient.get(PROFILE_PATH, ProfileResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get profile.");
        }
        return response;
    }

    public Optional<ProfileResponse> updateProfile(ProfileUpdateRequest request) {
        if (request == null) {
            LOG.log(Level.WARNING, "Profile update request is required to update profile.");
            return Optional.empty();
        }

        Optional<ProfileResponse> response = apiClient.put(PROFILE_PATH, request, ProfileResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to update profile.");
        }
        return response;
    }

    public boolean changePassword(ChangePasswordRequest request) {
        if (request == null || isBlank(request.getCurrentPassword()) || isBlank(request.getNewPassword())) {
            LOG.log(Level.WARNING, "Current and new password are required to change password.");
            return false;
        }

        apiClient.post(CHANGE_PASSWORD_PATH, request, Void.class);
        return apiCallStatus.isSuccess();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
