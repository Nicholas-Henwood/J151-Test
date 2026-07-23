package com.vzap.trytons.resource.admin;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dao.admin.DatabaseTestDAO;
import com.vzap.trytons.exceptions.DataAccessException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//The single supported database health contract for W4-BUG-BE-19.
@Path("/test/database")
@Authenticated
@AdminOnly
public class DatabaseHealthResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseHealth() {
        DatabaseTestDAO dao = new DatabaseTestDAO();
        Map<String, Object> result = new HashMap<>();

        try {
            List<String> tables = dao.getTableNames();

            if (tables.isEmpty()) {
                result.put("status", "WARNING");
                result.put("message", "Database connection succeeded but no tables were found.");
            } else {
                result.put("status", "SUCCESS");
                result.put("message", "Database connection established.");
            }

            return Response.ok(result).build();
        } catch (SQLException e) {
            throw new DataAccessException("Database health check failed.", e);
        }
    }
}