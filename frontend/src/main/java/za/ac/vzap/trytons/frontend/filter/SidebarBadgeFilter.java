package za.ac.vzap.trytons.frontend.filter;

import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import za.ac.vzap.trytons.frontend.client.notification.NotificationRestClient;
import za.ac.vzap.trytons.frontend.util.SessionAuthContext;

import java.io.IOException;

/**
 * Makes the sidebar's unread-notification badge available on every page, not
 * just the notifications page.
 *
 * The sidebar shows the badge from a "sidebarUnreadCount" request attribute, but
 * only NotificationServlet set it, so the badge appeared only while viewing
 * notifications. This filter sets the same attribute for every authenticated
 * page request, so the count is visible throughout the app. Static assets and
 * unauthenticated requests are skipped, so the count is fetched once per page
 * navigation rather than per resource. A servlet that computes its own count
 * (the notifications page) still overrides this value, since the filter runs
 * first.
 */
@WebFilter("/*")
public class SidebarBadgeFilter implements Filter {

    @Inject
    private SessionAuthContext authContext;

    @Inject
    private NotificationRestClient notificationRestClient;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && shouldPopulate(httpRequest)) {
            try {
                notificationRestClient.getUnreadCount()
                        .ifPresent(count -> httpRequest.setAttribute("sidebarUnreadCount", count));
            } catch (RuntimeException e) {
                // The badge is non-essential; never let its lookup break the page.
            }
        }
        chain.doFilter(request, response);
    }

    private boolean shouldPopulate(HttpServletRequest request) {
        if (authContext == null || !authContext.isAuthenticated()) {
            return false;
        }
        // Only rendered pages need the badge; skip the static asset requests.
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith("/assets/");
    }
}
