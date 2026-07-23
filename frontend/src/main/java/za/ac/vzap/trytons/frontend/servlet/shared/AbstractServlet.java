package za.ac.vzap.trytons.frontend.servlet.shared;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import za.ac.vzap.trytons.frontend.client.auth.LoginResponse;
import za.ac.vzap.trytons.frontend.client.shared.ApiCallStatus;
import za.ac.vzap.trytons.frontend.util.SessionAuthContext;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(AbstractServlet.class.getName());
    @Inject
    protected SessionAuthContext authContext;

    @Inject
    protected ApiCallStatus apiCallStatus;

    //protected route guard
    protected boolean requireAuthenticated(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(!authContext.isAuthenticated()) {
            LOG.warning("Unauthenticated access attempt to " + req.getRequestURI());
            clearAuthSession(req);
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }


    protected boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(!authContext.isAuthenticated()) {
            LOG.warning("Unauthenticated admin access attempt to " + req.getRequestURI());
            clearAuthSession(req);
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        if(!authContext.isAdmin()) {
            LOG.warning("Non-admin access attempt to " + req.getRequestURI());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorised to view this page.");
            return false;
        }
        return true;
    }

    protected Optional<UUID> parseUuid(String value) {
        if(value == null || value.isBlank()){
            return Optional.empty();
        }
        try{
            return Optional.of(UUID.fromString(value.trim()));
        }catch(IllegalArgumentException e){
            LOG.log(Level.WARNING, "Invalid UUID format: {0} " , value);
            return Optional.empty();
        }
    }
    protected void establishAuthenticatedSession(HttpServletRequest req, LoginResponse loginResponse){
        HttpSession session = req.getSession(true);
        req.changeSessionId();
        authContext.signIn(loginResponse);
        session.setAttribute(SessionAuthContext.SESSION_USER_ID, String.valueOf(loginResponse.getUserId()));
        session.setAttribute(SessionAuthContext.SESSION_USERNAME, loginResponse.getUsername());
        session.setAttribute(SessionAuthContext.SESSION_EMAIL, loginResponse.getEmail());
        session.setAttribute(SessionAuthContext.SESSION_ROLE, loginResponse.getRole());
        session.setAttribute(SessionAuthContext.SESSION_AUTHENTICATED, Boolean.TRUE);
    }

    //used for logout
    protected void clearAuthSession(HttpServletRequest req){
        authContext.clear();
        HttpSession session = req.getSession(false);
        if(session != null){
            session.removeAttribute(SessionAuthContext.SESSION_USER_ID);
            session.removeAttribute(SessionAuthContext.SESSION_USERNAME);
            session.removeAttribute(SessionAuthContext.SESSION_EMAIL);
            session.removeAttribute(SessionAuthContext.SESSION_ROLE);
            session.removeAttribute(SessionAuthContext.SESSION_AUTHENTICATED);
            session.invalidate();
        }
    }


    protected void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String errorMessage, String jspPath) throws ServletException,IOException {
        req.setAttribute("error", errorMessage);
        req.getRequestDispatcher(jspPath).forward(req, resp);
    }

    //Another helper recommended for success
    protected void forwardWithMessage(HttpServletRequest req, HttpServletResponse resp, String message, String jspPath) throws ServletException,IOException {
        req.setAttribute("message", message);
        req.getRequestDispatcher(jspPath).forward(req, resp);
    }

    protected boolean sessionExpiredRedirect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(apiCallStatus.isUnauthorized()) {
            clearAuthSession(req);
            resp.sendRedirect(req.getContextPath() + "/login");
            return true;
        }
        return false;
    }

    protected boolean handleApiFailure(HttpServletRequest req, HttpServletResponse resp, String fallbackMessage) throws IOException {
        if(apiCallStatus.isUnauthorized()) {
            clearAuthSession(req);
            resp.sendRedirect(req.getContextPath() + "/login?expired=1");
            return true;
        }
        req.setAttribute("error", apiCallStatus.getMessage(fallbackMessage));
        return false;
    }

}
