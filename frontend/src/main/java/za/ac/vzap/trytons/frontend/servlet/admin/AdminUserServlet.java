package za.ac.vzap.trytons.frontend.servlet.admin;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.admin.AdminUserRestClient;
import za.ac.vzap.trytons.frontend.client.admin.AdminUserSearchResponse;
import za.ac.vzap.trytons.frontend.client.admin.AdminUserStatusRequest;
import za.ac.vzap.trytons.frontend.client.admin.AdminUserStatusResponse;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "AdminUserServlet", urlPatterns = {"/admin/users"})
public class AdminUserServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-users.jsp";

    @Inject
    private AdminUserRestClient adminUserRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (!requireAdmin(request, response)) {
            return;
        }
        String searchTerm = request.getParameter("searchTerm");
        loadUsers(request, searchTerm);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (!requireAdmin(request, response)) {
            return;
        }

        String searchTerm = request.getParameter("searchTerm");
        Optional<UUID> userId = parseUuid(request.getParameter("userId"));
        Optional <Boolean> isActive = parseBoolean (request.getParameter("isActive"));

        if (userId.isEmpty()){
            request.setAttribute("error", "invalid or missing user id");

        } else if (isActive.isEmpty()) {
            request.setAttribute("error", "invalid or missing status value");

        } else if (updateStatus(request, userId.get(), isActive.get())) {
            String redirect = request.getContextPath() + "/admin/users";
            if (searchTerm != null && !searchTerm.isBlank()) {
                redirect += "?searchTerm=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
            }
            flashSuccess(request, isActive.get() ? "User activated" : "User deactivated");
            response.sendRedirect(redirect);
            return;
        }

        loadUsers(request, searchTerm);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private boolean updateStatus(HttpServletRequest request, UUID userId, boolean isActive){

        AdminUserStatusRequest statusRequest = new AdminUserStatusRequest();
        statusRequest.setActive(isActive);

        Optional <AdminUserStatusResponse> result = adminUserRestClient.updateUserStatus(userId, statusRequest);

        if (result.isPresent()) {
            return true;
        }
        request.setAttribute("error", "Unable to update user status");
        return false;
    }

    private void loadUsers(HttpServletRequest request, String searchTerm) {

        Optional<List<AdminUserSearchResponse>> users = adminUserRestClient.searchUsers(searchTerm);

        if (users.isPresent()) {
            request.setAttribute("users", users.get());
        } else {
            request.setAttribute("error", "Unable to load users");
            request.setAttribute("users", List.of());
        }
        request.setAttribute("searchTerm", searchTerm);
    }

    private Optional<Boolean> parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String trimmed = value.trim();
        if ("true".equalsIgnoreCase(trimmed)) {
            return Optional.of(Boolean.TRUE);
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return Optional.of(Boolean.FALSE);
        }
        return Optional.empty();
    }

    @Override
    public String getServletInfo() {
        return "Admin User Servlet, handles admin user search and activation status updates";
    }

}
