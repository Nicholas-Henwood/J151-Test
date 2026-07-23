package za.ac.vzap.trytons.frontend.client.admin;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class AdminUserRestClient {

    private static final String ADMIN_USERS_PATH = "/admin/users";

    private static final Logger LOG = Logger.getLogger(AdminUserRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<AdminUserSearchResponse>> searchUsers(String searchTerm) {
        String path = ADMIN_USERS_PATH;
        if (searchTerm != null && !searchTerm.isBlank()) {
            String encodedSearchTerm = URLEncoder.encode(searchTerm.trim(), StandardCharsets.UTF_8);
            path += "?searchTerm=" + encodedSearchTerm;
        }

        Optional<AdminUserSearchResponse[]> response = apiClient.get(path, AdminUserSearchResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to search users");
        }
        return response.map(users -> new ArrayList<>(Arrays.asList(users)));
    }

    public Optional<AdminUserStatusResponse> updateUserStatus(UUID targetUserId, AdminUserStatusRequest request) {
        if (targetUserId == null || request == null) {
            LOG.log(Level.WARNING, "Target user id and status request are required to update user status.");
            return Optional.empty();
        }

        String path = ADMIN_USERS_PATH + "/" + targetUserId + "/status";
        Optional<AdminUserStatusResponse> response = apiClient.put(path, request, AdminUserStatusResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to update user status");
        }
        return response;
    }
}
