package com.vzap.trytons.resource.notification;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.notification.NotificationResponseDTO;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.notification.NotificationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@Path("/notifications")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class NotificationResource {
    @Inject
    private NotificationService notificationService;

    @Context
    private ContainerRequestContext request;

    private UUID getCurrentUserId() {
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        return principal.getUserId();
    }

    @GET
    public Response listNotifications(@QueryParam("unreadOnly") Boolean unreadOnly) {
        boolean unreadOnlyFilter = Boolean.TRUE.equals(unreadOnly);

        List<NotificationResponseDTO> notifications = notificationService.getNotificationsForUser(getCurrentUserId(), unreadOnly);

        return Response.ok(notifications)
                .build();
    }

    @GET
    @Path("/unread-count")
    public Response getUnreadCount() {
        int count = notificationService.getUnreadCount(getCurrentUserId());
        return Response.ok(Map.of("unreadCount", count))
                .build();
    }

    @PUT
    @Path("/{notificationId}/read")
    public Response markAsRead(@PathParam("notificationId") UUID notificationId) {
        NotificationResponseDTO updated = notificationService.markAsRead(getCurrentUserId(), notificationId);

        return Response.ok(updated)
                .build();
    }

    @PUT
    @Path("/read-all")
    public Response markAllAsRead() {
        int updatedCount = notificationService.markAllAsRead(getCurrentUserId());

        return Response.ok(Map.of("updatedCount", updatedCount))
                .build();
    }
}