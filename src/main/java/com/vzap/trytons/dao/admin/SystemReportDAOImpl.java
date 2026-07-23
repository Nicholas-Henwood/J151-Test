package com.vzap.trytons.dao.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.enums.SystemReportType;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.admin.SystemReport;
import jakarta.inject.Singleton;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class SystemReportDAOImpl extends BaseDAO implements SystemReportDAO {
    private static final Logger LOG = Logger.getLogger(SystemReportDAOImpl.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private SystemReport mapRow(ResultSet rs) throws SQLException {
        String adminUserId = rs.getString("generated_by_admin_user_id");
        Timestamp generatedAtTimestamp = rs.getTimestamp("generatedAt");

        SystemReport systemReport = new SystemReport();
        systemReport.setReportId(UUID.fromString(rs.getString("reportId")));
        systemReport.setGeneratedByAdminUserId(adminUserId == null ? null : UUID.fromString(adminUserId));
        systemReport.setReportType(SystemReportType.valueOf(rs.getString("reportType")));
        systemReport.setReportTitle(rs.getString("reportTitle"));
        systemReport.setParametersJson(parseJsonToMap(rs.getString("parametersJson")));
        systemReport.setResultJson(parseJsonToMap(rs.getString("resultJson")));
        systemReport.setGeneratedAt(generatedAtTimestamp == null ? null : generatedAtTimestamp.toLocalDateTime());
        return systemReport;
    }

    @Override
    public SystemReport save(SystemReport systemReport) {
        String query = "INSERT INTO systemReport"+
                    "(reportId, generated_by_admin_user_id, reportType, reportTitle, parametersJson, resultJson, generatedAt)"+
                "VALUES(?, ?, ?, ?, ?, ?, ?)";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,systemReport.getReportId().toString());
            ps.setString(2,systemReport.getGeneratedByAdminUserId() == null ? null : systemReport.getGeneratedByAdminUserId().toString());
            ps.setString(3,systemReport.getReportType().toString());
            ps.setString(4,systemReport.getReportTitle());
            ps.setString(5,mapToJson(systemReport.getParametersJson()));
            ps.setString(6,mapToJson(systemReport.getResultJson()));
            LocalDateTime generatedAt = systemReport.getGeneratedAt() != null ? systemReport.getGeneratedAt() : LocalDateTime.now();
            ps.setTimestamp(7,Timestamp.valueOf(generatedAt));
            ps.executeUpdate();
            return  systemReport;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to save the report.", e);
            throw new DataAccessException("Cannot save the report",e);
        }
    }

    @Override
    public Optional<SystemReport> findById(UUID reportId) {
        String query = """
                SELECT reportId, generated_by_admin_user_id, reportType, reportTitle, parametersJson, resultJson, generatedAt
                FROM systemReport
                WHERE reportId = ?""";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,reportId.toString());
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next()){
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find the report.", e);
            throw new DataAccessException("Unable to find the report",e);
        }
    }

    @Override
    public List<SystemReport> findAll() {
        String query = """
                SELECT reportId, generated_by_admin_user_id, reportType, reportTitle, parametersJson, resultJson, generatedAt
                        FROM systemReport
                        ORDER BY generatedAt DESC;
        """;
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery()){
            List<SystemReport> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find the reports.", e);
            throw new DataAccessException("Unable to find the reports",e);
        }
    }

    @Override
    public List<SystemReport> findByType(SystemReportType reportType) {
        String query = """
        SELECT reportId, generated_by_admin_user_id, reportType, reportTitle, parametersJson, resultJson, generatedAt
                    FROM systemReport
                    WHERE reportType = ?
                    ORDER BY generatedAt DESC;
        """;
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
        ps.setString(1,reportType.toString());
        try(ResultSet rs = ps.executeQuery()){
            List<SystemReport> Reportlist = new ArrayList<>();
            while(rs.next()){
                Reportlist.add(mapRow(rs));
            }
            return Reportlist;
        }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find the reports by type.", e);
            throw new DataAccessException("Unable to find the reports",e);
        }
    }

    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON column to Map", e);
        }
    }
    private String mapToJson(Map<String, Object> values) {
        if (values == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialise Map to JSON column", e);
        }
    }
}
