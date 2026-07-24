package za.ac.vzap.trytons.frontend.servlet.auth;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.auth.AuthRestClient;
import za.ac.vzap.trytons.frontend.client.auth.LoginRequest;
import za.ac.vzap.trytons.frontend.client.auth.LoginResponse;
import za.ac.vzap.trytons.frontend.client.auth.RegisteredUserRequest;
import za.ac.vzap.trytons.frontend.client.auth.RegisteredUserResponse;
import za.ac.vzap.trytons.frontend.client.shared.ApiCallStatus;

import java.io.IOException;
import java.util.Optional;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;


@WebServlet (name = "AuthServlet" , urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends AbstractServlet {

    @Inject
    private AuthRestClient authRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "/login" -> request.getRequestDispatcher("/pages/login.jsp").forward(request, response);
            case "/register" -> request.getRequestDispatcher("/pages/register.jsp").forward(request, response);
            case "/logout" -> {
                authRestClient.logout();
                clearAuthSession(request);
                // Signing out returns to the public landing page rather than the login
                // form: the session is gone, so the app root shows the marketing page
                // with its own Log in / Sign up entry points.
                response.sendRedirect(request.getContextPath() + "/");
            }
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        switch (submit) {
            case "login" -> handleLogin(request, response);
            case "register" -> handleRegister(request, response);
            case "logout" -> handleLogout(request, response);
            default -> request.getRequestDispatcher("/pages/login.jsp").forward(request, response);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String identifier = request.getParameter("identifier");
        String password = request.getParameter("password");
        Optional<LoginResponse> loginResponse = authRestClient.login(new LoginRequest(identifier, password));
        if (loginResponse.isPresent()) {
            establishAuthenticatedSession(request, loginResponse.get());
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        String error;
        if (apiCallStatus.isUnauthorized()) {
            error = "Invalid username/email or password.";
        } else if (apiCallStatus.getStatus() == ApiCallStatus.NETWORK_FAILURE || apiCallStatus.getStatus() >= 500) {
            error = "The login service is currently unavailable. Please try again later.";
        } else {
            error = apiCallStatus.getMessage("Login failed. Please try again.");
        }
        request.setAttribute("error", error);
        request.setAttribute("identifier", identifier);
        request.getRequestDispatcher("/pages/login.jsp").forward(request, response);
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String rawPassword = request.getParameter("rawPassword");
        Optional<RegisteredUserResponse> registerResponse = authRestClient.register(new RegisteredUserRequest(email, username, rawPassword));
        if (registerResponse.isPresent()) {
            response.sendRedirect(request.getContextPath() + "/login?registered=1");
            return;
        }
        request.setAttribute("error", apiCallStatus.getMessage("Registration failed."));
        request.setAttribute("email", email);
        request.setAttribute("username", username);
        request.getRequestDispatcher("/pages/register.jsp").forward(request, response);
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authRestClient.logout();
        clearAuthSession(request);
        // Same destination as the GET route: the public landing page.
        response.sendRedirect(request.getContextPath() + "/");
    }

    @Override
    public String getServletInfo() {
        return "Auth Servlet, handles login request and register request and logout request";
    }

}

