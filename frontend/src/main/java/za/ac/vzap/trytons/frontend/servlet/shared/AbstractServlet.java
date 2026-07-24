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

    // Keep the signed-in identity in sync after a profile edit. The sidebar and
    // top nav read the HttpSession "username"/"email" attributes (set at login),
    // so updating the profile without this would leave the old name on screen
    // until the next login. Nulls are ignored so a partial update never blanks a
    // field. Session id is left untouched — this is not a privilege change.
    protected void refreshSessionIdentity(HttpServletRequest req, String username, String email){
        HttpSession session = req.getSession(false);
        if(session == null){
            return;
        }
        if(username != null && !username.isBlank()){
            session.setAttribute(SessionAuthContext.SESSION_USERNAME, username);
            authContext.setUsername(username);
        }
        if(email != null && !email.isBlank()){
            session.setAttribute(SessionAuthContext.SESSION_EMAIL, email);
            authContext.setEmail(email);
        }
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

    // ---- Toast flash ---------------------------------------------------------
    // A one-shot message stashed in the session, rendered once as a toast by
    // /WEB-INF/jspf/toast.jspf on the next page load and then cleared. Pair these
    // with redirectTo() so a successful POST follows the POST-redirect-GET pattern:
    // the toast survives the redirect and a refresh cannot re-submit the form.
    protected static final String FLASH_MESSAGE = "flash.message";
    protected static final String FLASH_TYPE = "flash.type";

    protected void flashSuccess(HttpServletRequest req, String message) {
        setFlash(req, "success", message);
    }

    protected void flashError(HttpServletRequest req, String message) {
        setFlash(req, "error", message);
    }

    protected void flashInfo(HttpServletRequest req, String message) {
        setFlash(req, "info", message);
    }

    private void setFlash(HttpServletRequest req, String type, String message) {
        HttpSession session = req.getSession(true);
        session.setAttribute(FLASH_MESSAGE, message);
        session.setAttribute(FLASH_TYPE, type);
    }

    // Request-scoped toast, for handlers that forward to a view instead of
    // redirecting. Rendered by the same toast.jspf host as the session flash.
    // Prefer flash* + a redirect where the success path already redirects.
    protected static final String TOAST_MESSAGE = "toastMessage";
    protected static final String TOAST_TYPE = "toastType";

    protected void toastSuccess(HttpServletRequest req, String message) {
        setToast(req, "success", message);
    }

    protected void toastError(HttpServletRequest req, String message) {
        setToast(req, "error", message);
    }

    protected void toastInfo(HttpServletRequest req, String message) {
        setToast(req, "info", message);
    }

    private void setToast(HttpServletRequest req, String type, String message) {
        req.setAttribute(TOAST_MESSAGE, message);
        req.setAttribute(TOAST_TYPE, type);
    }

    // Context-relative redirect, e.g. redirectTo(resp, req, "/create-team").
    protected void redirectTo(HttpServletResponse resp, HttpServletRequest req, String contextRelativePath) throws IOException {
        resp.sendRedirect(req.getContextPath() + contextRelativePath);
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
