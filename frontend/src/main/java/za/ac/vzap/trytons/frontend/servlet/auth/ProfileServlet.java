package za.ac.vzap.trytons.frontend.servlet.auth;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.auth.ChangePasswordRequest;
import za.ac.vzap.trytons.frontend.client.auth.ProfileRestClient;
import za.ac.vzap.trytons.frontend.client.auth.ProfileResponse;
import za.ac.vzap.trytons.frontend.client.auth.ProfileUpdateRequest;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile", "/profile/change-password"})
public class ProfileServlet extends AbstractServlet {

    private static final String PROFILE_VIEW = "/pages/profile.jsp";
    private static final String CHANGE_PASSWORD_VIEW = "/pages/change-password.jsp";
    private static final String CHANGE_PASSWORD_PATH = "/profile/change-password";

    @Inject
    private ProfileRestClient profileRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        if (CHANGE_PASSWORD_PATH.equals(request.getServletPath())) {
            request.getRequestDispatcher(CHANGE_PASSWORD_VIEW).forward(request, response);
            return;
        }

        Optional<ProfileResponse> profile = profileRestClient.getProfile();
        if (profile.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            request.setAttribute("error", "Unable to load your profile right now");
        } else {
            request.setAttribute("profile", profile.get());
            addDateLabels(request, profile.get());
        }

        request.getRequestDispatcher(PROFILE_VIEW).forward(request, response);
    }

    // registrationDate and lastLoginAt are LocalDateTime, and fmt:formatDate takes a
    // java.util.Date, so the display strings are built here. Blank when never set —
    // lastLoginAt in particular is null until the first login after this field existed.
    private void addDateLabels(HttpServletRequest request, ProfileResponse profile) {
        if (profile.getRegistrationDate() != null) {
            request.setAttribute("registrationDateLabel", profile.getRegistrationDate().format(PROFILE_DATE));
        }
        if (profile.getLastLoginAt() != null) {
            request.setAttribute("lastLoginLabel", profile.getLastLoginAt().format(PROFILE_DATE));
        }
    }

    private static final DateTimeFormatter PROFILE_DATE =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.UK);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        if (CHANGE_PASSWORD_PATH.equals(request.getServletPath())) {
            handleChangePassword(request, response);
        } else {
            handleProfileUpdate(request, response);
        }
    }

    private void handleProfileUpdate(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setUsername(request.getParameter("username"));
        updateRequest.setEmail(request.getParameter("email"));
        updateRequest.setProfilePic(request.getParameter("profilePic"));

        Optional<ProfileResponse> updated = profileRestClient.updateProfile(updateRequest);
        if (updated.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            request.setAttribute("error", "Unable to update your profile");
            profileRestClient.getProfile().ifPresent(p -> {
                request.setAttribute("profile", p);
                addDateLabels(request, p);
            });
            request.getRequestDispatcher(PROFILE_VIEW).forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");

        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setCurrentPassword(currentPassword);
        changeRequest.setNewPassword(newPassword);

        boolean success = profileRestClient.changePassword(changeRequest);
        if (success) {
            response.sendRedirect(request.getContextPath() + "/profile/change-password");
            return;
        }

        if (handleApiFailure(request, response, "Unable to change your password. Please check your current password and try again.")) {
            return;
        }
        request.getRequestDispatcher(CHANGE_PASSWORD_VIEW).forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Profile Servlet, handles viewing/updating the current user's profile and changing password";
    }
}
