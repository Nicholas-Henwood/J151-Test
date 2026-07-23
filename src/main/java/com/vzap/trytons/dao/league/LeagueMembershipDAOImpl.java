package com.vzap.trytons.dao.league;
import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.league.League;
import com.vzap.trytons.model.league.LeagueMembership;
import com.vzap.trytons.model.auth.RegisteredUser;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.UUID;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

public class LeagueMembershipDAOImpl extends BaseDAO implements LeagueMembershipDAO {

    private static final Logger LOGGER = Logger.getLogger(LeagueMembershipDAOImpl.class.getName());
    private static final String BASE_FIELDS = "membershipId, leagueId, registered_user_id, teamId, isActive, joinDate";
    private static final String RESPONSE_JOIN = " FROM leagueMembership lm JOIN league l ON l.leagueId = lm.leagueId";
    private static final String RESPONSE_FIELDS = "l.leagueId, l.leagueName, l.description, l.leagueType, l.creationDate";

    @Override
    public LeagueMembership createMembership(UUID leagueId, UUID userId, UUID teamId) {
        UUID newId = UUID.randomUUID();
        LocalDateTime joinDate = LocalDateTime.now();

        String sql = "INSERT INTO leagueMembership (membershipId, leagueId, registered_user_id, teamId, isActive, " +
                "joinDate)" + " VALUES (?, ?, ?, ?, TRUE, ?)";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)){
            stmt.setString(1, newId.toString());
            stmt.setString(2, leagueId.toString());
            stmt.setString(3, userId.toString());
            stmt.setString(4, teamId.toString());
            stmt.setTimestamp(5, Timestamp.valueOf(joinDate));
            stmt.executeUpdate();
        } catch (SQLException e){
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The league membership could not be created because it conflicts with an existing record."
                );
            }

            String message = e.getMessage();
            if (message != null && message.contains("uk_leagueMembership_user")) {
                throw new ConflictException("This user is already a member of this league.");
            }
            if (message != null && message.contains("uk_leagueMembership_team")) {
                throw new ConflictException("This team is already a member of this league.");
            }

            LOGGER.log(Level.SEVERE, "Failed to create membership for league" + leagueId + " user" + userId
                    + " team" + teamId, e);
            throw new DataAccessException("Failed to create membership for league" + leagueId + " user" + userId
                    + " team" + teamId, e);
        }

        League league = new League();
        league.setLeagueId(leagueId);

        RegisteredUser registeredUser = new RegisteredUser();
        registeredUser.setUserId(userId);

        FantasyTeam fantasyTeam = new FantasyTeam();
        fantasyTeam.setTeamId(teamId);

        LeagueMembership membership = new LeagueMembership();
        membership.setMembershipId(newId);
        membership.setIsActive(true);
        membership.setJoinDate(joinDate);
        membership.setLeagueId(leagueId);
        membership.setRegisteredUserId(userId);
        membership.setTeamId(teamId);

        return membership;
    }

    @Override
    public Optional<LeagueMembership> findById(UUID membershipId){
        String sql = "SELECT " + BASE_FIELDS + " FROM leagueMembership WHERE membershipId = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, membershipId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {return Optional.empty();
                }
                return Optional.of(rowToMembership(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to fetch membership" + " " + membershipId, e);
            throw new DataAccessException("Failed to fetch membership " + membershipId, e);
        }
    }

    @Override
    public List<LeagueMembership> findActiveByUser(UUID userId) {
        String sql = "SELECT " + BASE_FIELDS + " FROM leagueMembership WHERE registered_user_id = ? AND isActive = TRUE ORDER BY joinDate DESC";

        List<LeagueMembership> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, userId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rowToMembership(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to fetch  active memberships for user " + userId, e);
            throw new DataAccessException("Failed to fetch  active memberships for user " + userId, e);
        }
        return list;
    }

    @Override
    public List<LeagueMembership> findActiveByLeague(UUID leagueId) {
        String sql = "SELECT " + BASE_FIELDS + " FROM leagueMembership WHERE leagueId = ? AND isActive = TRUE ORDER BY joinDate ASC";

        List<LeagueMembership> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rowToMembership(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to fetch  active memberships for league " + leagueId, e);
            throw new DataAccessException("Failed to fetch  active memberships for league " + leagueId, e);
        }
        return list;
    }

    private League rowToLeague(ResultSet rs) throws SQLException {
        League league = new League();
        league.setLeagueId(parseUuid(rs.getString("leagueId"), "leagueId"));
        league.setLeagueName(rs.getString("leagueName"));
        league.setDescription(rs.getString("description"));
        league.setLeagueType(LeagueType.valueOf(rs.getString("leagueType")));
        league.setCreationDate(parseTimestamp(rs.getTimestamp("creationDate"), "creationDate"));
        return league;
    }

    @Override
    public List<League> findLeaguesByUser(UUID userId) {
        String sql = "SELECT " + RESPONSE_FIELDS + RESPONSE_JOIN
                + " WHERE lm.registered_user_id = ? AND lm.isActive = TRUE ORDER BY lm.joinDate DESC";

        List<League> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, userId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rowToLeague(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to fetch league responses for user " + userId, e);
            throw new DataAccessException("Failed to fetch league responses for user " + userId, e);
        }
        return list;
    }

    @Override
    public List<League> findLeaguesByLeague(UUID leagueId) {
        String sql = "SELECT " + RESPONSE_FIELDS + RESPONSE_JOIN
                + " WHERE lm.leagueId = ? AND lm.isActive = TRUE ORDER BY lm.joinDate DESC";

        List<League> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rowToLeague(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to fetch league responses for league " + leagueId, e);
            throw new DataAccessException("Failed to fetch league responses for league " + leagueId, e);
        }
        return list;
    }

    @Override
    public boolean existsActiveByLeagueAndUser(UUID leagueId, UUID userId) {
        String sql = "SELECT COUNT(*) FROM leagueMembership WHERE leagueId = ? AND registered_user_id = ? AND isActive = TRUE";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueId.toString());
            stmt.setString(2, userId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to check active membership for league " + leagueId + " user "
                    + userId, e);
            throw new DataAccessException(leagueId + " user " + userId, e);
        }
    }

    @Override
    public int countActiveMembers(UUID leagueId) {
        String sql = "SELECT COUNT(*) FROM leagueMembership WHERE leagueId = ? AND isActive = TRUE";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to count active members for league " + leagueId, e);
            throw new DataAccessException("Failed to count active members for league " + leagueId, e);
        }
    }

    @Override
    public boolean deactivateMembership(UUID membershipId) {
        String sql = "UPDATE leagueMembership SET isActive = FALSE WHERE membershipId = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, membershipId.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to deactivate membership " + membershipId, e);
            throw new DataAccessException("Failed to deactivate membership " + membershipId, e);
        }
    }

    private LeagueMembership rowToMembership(ResultSet rs) throws SQLException {
        LeagueMembership mem = new LeagueMembership();
        mem.setMembershipId(parseUuid(rs.getString("membershipId"), "membershipId"));
        mem.setIsActive(rs.getBoolean("isActive"));
        mem.setJoinDate(parseTimestamp(rs.getTimestamp("joinDate"), "joinDate"));

        League league = new League();
        league.setLeagueId(parseUuid(rs.getString("leagueId"), "leagueId"));

        mem.setLeagueId(parseUuid(rs.getString("leagueId"), "leagueId"));

        RegisteredUser registeredUser = new RegisteredUser();
        registeredUser.setUserId(parseUuid(rs.getString("registered_user_id"), "registered_user_id"));

        mem.setRegisteredUserId(parseUuid(rs.getString("registered_user_id"), "registered_user_id"));

        FantasyTeam fantasyTeam = new FantasyTeam();
        fantasyTeam.setTeamId(parseUuid(rs.getString("teamId"), "teamId"));

        mem.setTeamId(parseUuid(rs.getString("teamId"), "teamId"));
        return mem;
    }

    private UUID parseUuid(String str, String columnName) throws SQLException {
        if (str == null) {
            throw new SQLException("Unexpected null value for column " + columnName);
        }
        try {
            return UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            throw new SQLException("incorrect UUID in column " + columnName + "  " + str, e);
        }
    }

    private LocalDateTime parseTimestamp(Timestamp ts, String columnName) throws SQLException {
        if (ts == null) {
            throw new SQLException("Unexpected null value for column " + columnName);
        }
        return ts.toLocalDateTime();
    }
}