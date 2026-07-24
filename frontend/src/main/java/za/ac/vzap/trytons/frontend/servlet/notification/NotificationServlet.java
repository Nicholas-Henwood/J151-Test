package za.ac.vzap.trytons.frontend.servlet.notification;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.notification.NotificationRestClient;
import za.ac.vzap.trytons.frontend.client.notification.NotificationResponse;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "NotificationServlet", urlPatterns = {"/notifications"})
public class NotificationServlet extends AbstractServlet {

    private static final String VIEW = "/pages/notifications.jsp";

    @Inject
    private NotificationRestClient notificationRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        boolean unreadOnly = Boolean.parseBoolean(request.getParameter("unreadOnly"));
        if (!loadNotifications(request, response, unreadOnly)) {
            return;
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "markAsRead" -> {
                if (!markAsRead(request, response)) {
                    return;
                }
            }
            case "markAllAsRead" -> {
                if (!markAllAsRead(request, response)) {
                    return;
                }
            }
            default -> request.setAttribute("error", "Unknown notification action requested");
        }

        if (request.getAttribute("success") != null) {
            flashSuccess(request, (String) request.getAttribute("success"));
            response.sendRedirect(request.getContextPath() + "/notifications");
            return;
        }

        if (!loadNotifications(request, response, false)) {
            return;
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private boolean loadNotifications(HttpServletRequest request, HttpServletResponse response, boolean unreadOnly) throws IOException {
        Optional<List<NotificationResponse>> notifications = notificationRestClient.getNotifications(unreadOnly);
        if (notifications.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return false;
            }
            if (request.getAttribute("error") == null) {
                request.setAttribute("error", "Unable to load notifications right now");
            }
            request.setAttribute("notifications", List.of());
        } else {
            request.setAttribute("notifications", notifications.get());
        }
        request.setAttribute("unreadOnly", unreadOnly);
        decorateFeed(request, (List<NotificationResponse>) request.getAttribute("notifications"));
        return true;
    }

    /**
     * Derives the presentational parts of the feed.
     *
     * createdAt is a LocalDateTime, and fmt:formatDate takes a java.util.Date, so the
     * date buckets and relative times are built here rather than in the JSP. Grouping
     * is done here too, since JSTL cannot bucket a list.
     *
     * The unread count is taken from the list rather than the unread-count endpoint:
     * with the filter off the list holds every notification, and with it on the list is
     * exactly the unread ones, so it is always consistent with what is on screen and
     * costs no extra call.
     */
    private void decorateFeed(HttpServletRequest request, List<NotificationResponse> notifications) {
        List<NotificationResponse> feed = notifications == null ? List.of() : notifications;

        int unread = 0;
        Map<String, String> relativeTimes = new HashMap<>();
        Map<String, List<NotificationResponse>> groups = new LinkedHashMap<>();
        groups.put(TODAY, new ArrayList<>());
        groups.put(YESTERDAY, new ArrayList<>());
        groups.put(EARLIER, new ArrayList<>());

        LocalDate today = LocalDate.now();
        for (NotificationResponse notification : feed) {
            if (notification == null) continue;
            if (!notification.isRead()) unread++;

            LocalDateTime created = notification.getCreatedAt();
            String bucket = EARLIER;
            if (created != null) {
                LocalDate day = created.toLocalDate();
                if (day.isEqual(today)) bucket = TODAY;
                else if (day.isEqual(today.minusDays(1))) bucket = YESTERDAY;

                if (notification.getNotificationId() != null) {
                    relativeTimes.put(notification.getNotificationId().toString(), relativeTime(created));
                }
            }
            groups.get(bucket).add(notification);
        }

        // Drop empty buckets so the page does not render a heading with nothing under it.
        groups.values().removeIf(List::isEmpty);

        request.setAttribute("notificationGroups", groups);
        request.setAttribute("relativeTimes", relativeTimes);
        request.setAttribute("unreadCount", unread);
        // Read by sidebar.jspf; only this page sets it today, so the nav badge appears
        // here. Any other servlet can opt in with the same attribute.
        request.setAttribute("sidebarUnreadCount", unread);
    }

    private static final String TODAY = "Today";
    private static final String YESTERDAY = "Yesterday";
    private static final String EARLIER = "Earlier";

    private static final DateTimeFormatter OLDER_THAN_A_WEEK =
            DateTimeFormatter.ofPattern("d MMM", Locale.UK);

    /** "just now", "12m ago", "3h ago", "2d ago", then an absolute date. */
    private String relativeTime(LocalDateTime created) {
        Duration elapsed = Duration.between(created, LocalDateTime.now());
        if (elapsed.isNegative()) return "just now";

        long minutes = elapsed.toMinutes();
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + "m ago";

        long hours = elapsed.toHours();
        if (hours < 24) return hours + "h ago";

        long days = elapsed.toDays();
        if (days < 7) return days + "d ago";
        return created.format(OLDER_THAN_A_WEEK);
    }

    private boolean markAsRead(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<UUID> notificationId = parseUuid(request.getParameter("notificationId"));
        if (notificationId.isEmpty()) {
            request.setAttribute("error", "A valid notification is required");
            return true;
        }

        Optional<NotificationResponse> updated = notificationRestClient.markAsRead(notificationId.get());
        if (updated.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return false;
            }
            request.setAttribute("error", "Unable to mark notification as read");
        } else {
            request.setAttribute("success", "Notification marked as read");
        }
        return true;
    }

    private boolean markAllAsRead(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<Integer> updatedCount = notificationRestClient.markAllAsRead();
        if (updatedCount.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return false;
            }
            request.setAttribute("error", "Unable to mark all notifications as read");
        } else {
            request.setAttribute("success", updatedCount.get() + " notification(s) marked as read");
        }
        return true;
    }

    @Override
    public String getServletInfo() {
        return "Notification Servlet, handles listing notifications and marking them as read";
    }
}
