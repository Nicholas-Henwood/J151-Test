package com.vzap.trytons.dao.leaderboard;

import com.vzap.trytons.enums.LeaderboardScope;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.leaderboard.Leaderboard;
import com.vzap.trytons.model.leaderboard.Ranking;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

public class LeaderboardDAOImpl extends BaseDAO implements LeaderboardDAO {

    private static final Logger LOG = Logger.getLogger(LeaderboardDAOImpl.class.getName());

    @Override
    public Optional<Leaderboard> getLeaderboardByLeagueId(UUID leagueId) {
        String query = "SELECT * FROM leaderboard WHERE leagueId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, leagueId.toString());

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    Leaderboard lb = Leaderboard.builder()
                            .leaderboardId(UUID.fromString(rs.getString("leaderboardId")))
                            .leagueId(UUID.fromString(rs.getString("leagueId")))
                            .season(rs.getString("season"))
                            .scope(LeaderboardScope.valueOf(rs.getString("scope")))
                            .lastUpdated(rs.getObject("lastUpdated", LocalDateTime.class))
                            .build();

                    return Optional.of(lb);
                }
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to get leaderboard by league id " + leagueId, e);
            throw new DataAccessException("Unable to get leaderboard by league id " + leagueId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Ranking> getRankingsByLeaderboardId(UUID leaderboardId) {
        String query = "SELECT * FROM ranking WHERE leaderboardId = ?";
        List<Ranking> rankings = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, leaderboardId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ranking r = Ranking.builder()
                            .rankingId(UUID.fromString(rs.getString("rankingId")))
                            .leaderboardId(UUID.fromString(rs.getString("leaderboardId")))
                            .teamId(UUID.fromString(rs.getString("teamId")))
                            .currentRanking(rs.getInt("currentRanking"))
                            .previousRanking(rs.getObject("previousRanking", Integer.class))
                            .matchesPlayed(rs.getInt("matchesPlayed"))
                            .matchesWon(rs.getInt("matchesWon"))
                            .matchesDrawn(rs.getInt("matchesDrawn"))
                            .matchesLost(rs.getInt("matchesLost"))
                            .pointsFor(rs.getInt("pointsFor"))
                            .pointsAgainst(rs.getInt("pointsAgainst"))
                            .scoreDifference(rs.getInt("scoreDifference"))
                            .leaguePoints(rs.getInt("leaguePoints"))
                            .totalFantasyPoints(rs.getInt("total_fantasy_points"))
                            .updatedAt(rs.getObject("updatedAt", LocalDateTime.class))
                            .build();
                    rankings.add(r);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to get rankings by leaderboard id.", e);
            throw new DataAccessException("Unable to get rankings by leaderboard id.", e);
        }
        return rankings;
    }

    @Override
    public Optional<Ranking> getRankingByTeamId(UUID teamId, UUID leaderboardId) {
        String query = "SELECT * FROM ranking WHERE teamId = ? AND leaderboardId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, teamId.toString());
            ps.setString(2, leaderboardId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ranking r = Ranking.builder()
                            .rankingId(UUID.fromString(rs.getString("rankingId")))
                            .leaderboardId(UUID.fromString(rs.getString("leaderboardId")))
                            .teamId(UUID.fromString(rs.getString("teamId")))
                            .currentRanking(rs.getInt("currentRanking"))
                            .previousRanking(rs.getObject("previousRanking", Integer.class))
                            .matchesPlayed(rs.getInt("matchesPlayed"))
                            .matchesWon(rs.getInt("matchesWon"))
                            .matchesDrawn(rs.getInt("matchesDrawn"))
                            .matchesLost(rs.getInt("matchesLost"))
                            .pointsFor(rs.getInt("pointsFor"))
                            .pointsAgainst(rs.getInt("pointsAgainst"))
                            .scoreDifference(rs.getInt("scoreDifference"))
                            .leaguePoints(rs.getInt("leaguePoints"))
                            .totalFantasyPoints(rs.getInt("total_fantasy_points"))
                            .updatedAt(rs.getObject("updatedAt", LocalDateTime.class))
                            .build();

                    return Optional.of(r);
                }
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to get rankings by teamId.", e);
            throw new DataAccessException("Unable to get rankings by teamId.", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Leaderboard> getLeaderboardById(UUID leaderboardId) {
        String query = "SELECT * FROM leaderboard WHERE leaderboardId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, leaderboardId.toString());

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    UUID resolvedLeagueId;
                    if (rs.getString("leagueId") == null){
                        resolvedLeagueId = null;
                    }else {
                        resolvedLeagueId = UUID.fromString(rs.getString("leagueId"));
                    }
                        Leaderboard lb = Leaderboard.builder()
                                .leaderboardId(UUID.fromString(rs.getString("leaderboardId")))
                                .leagueId(resolvedLeagueId)
                                .season(rs.getString("season"))
                                .scope(LeaderboardScope.valueOf(rs.getString("scope")))
                                .lastUpdated(rs.getObject("lastUpdated", LocalDateTime.class))
                                .build();

                        return Optional.of(lb);
                }
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to get leaderboard by leaderboard id " + leaderboardId, e);
            throw new DataAccessException("Unable to get leaderboard by leaderboard id " + leaderboardId, e);
        }
        return Optional.empty();
    }

    @Override
    public void saveRanking(Ranking ranking) {
        String query = "INSERT INTO ranking (rankingId, leaderboardId, teamId, currentRanking, previousRanking, matchesPlayed, matchesWon, matchesDrawn, matchesLost, pointsFor, pointsAgainst, leaguePoints, total_fantasy_points, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, ranking.getRankingId().toString());
            ps.setString(2, ranking.getLeaderboardId().toString());
            ps.setString(3, ranking.getTeamId().toString());
            ps.setInt(4, ranking.getCurrentRanking());
            ps.setObject(5, ranking.getPreviousRanking());
            ps.setInt(6, ranking.getMatchesPlayed());
            ps.setInt(7, ranking.getMatchesWon());
            ps.setInt(8, ranking.getMatchesDrawn());
            ps.setInt(9, ranking.getMatchesLost());
            ps.setInt(10, ranking.getPointsFor());
            ps.setInt(11, ranking.getPointsAgainst());
            ps.setInt(12, ranking.getLeaguePoints());
            ps.setInt(13, ranking.getTotalFantasyPoints());
            ps.setString(14, ranking.getUpdatedAt().toString());

            ps.executeUpdate();

        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to save ranking.", e);
            throw new DataAccessException("Unable to save ranking.", e);
        }
    }

    @Override
    public void updateRanking(Ranking ranking) {
        String query = "UPDATE ranking SET currentRanking = ?, previousRanking = ?, matchesPlayed = ?, matchesWon = ?, matchesDrawn = ?, matchesLost = ?, pointsFor = ?, pointsAgainst = ?, leaguePoints = ?, total_fantasy_points = ?, updatedAt = ? WHERE rankingId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setInt(1, ranking.getCurrentRanking());
            ps.setObject(2, ranking.getPreviousRanking());
            ps.setInt(3, ranking.getMatchesPlayed());
            ps.setInt(4, ranking.getMatchesWon());
            ps.setInt(5, ranking.getMatchesDrawn());
            ps.setInt(6, ranking.getMatchesLost());
            ps.setInt(7, ranking.getPointsFor());
            ps.setInt(8, ranking.getPointsAgainst());
            ps.setInt(9, ranking.getLeaguePoints());
            ps.setInt(10, ranking.getTotalFantasyPoints());
            ps.setString(11, ranking.getUpdatedAt().toString());
            ps.setString(12, ranking.getRankingId().toString());

            ps.executeUpdate();

        }catch (SQLException e){
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The ranking could not be updated because the team is not a member of the league."
                );
            }

            LOG.log(Level.SEVERE, "Unable to update ranking.", e);
            throw new DataAccessException("Unable to update ranking.", e);
        }
    }

    @Override
    public void deleteRankingByLeaderboardId(UUID leaderboardId) {
        String query ="DELETE FROM ranking WHERE leaderboardId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, leaderboardId.toString());

            ps.executeUpdate();

        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to delete ranking.", e);
            throw new DataAccessException("Unable to delete ranking.", e);
        }

    }

    @Override
    public void updateLeaderboard(Leaderboard leaderboard) {
        String query = "UPDATE leaderboard SET season = ?, scope = ?, lastUpdated = ? WHERE leaderboardId = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, leaderboard.getSeason());
            ps.setString(2, leaderboard.getScope().toString());
            ps.setString(3, leaderboard.getLastUpdated().toString());
            ps.setString(4, leaderboard.getLeaderboardId().toString());

            ps.executeUpdate();

        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to update leaderboard.", e);
            throw new DataAccessException("Unable to update leaderboard.", e);
        }
    }

    @Override
    public void saveLeaderboard(Leaderboard leaderboard) {
        String query = "INSERT INTO leaderboard (leaderboardId, leagueId, season, scope, lastUpdated) VALUES (?, ?, ?, ?, ?)";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, leaderboard.getLeaderboardId().toString());
            if (leaderboard.getLeagueId() == null) {
                ps.setNull(2, Types.VARCHAR);
            }else {
                ps.setString(2, leaderboard.getLeagueId().toString());
            }
            ps.setString(3, leaderboard.getSeason());
            ps.setString(4, leaderboard.getScope().toString());
            ps.setString(5, leaderboard.getLastUpdated().toString());

            ps.executeUpdate();

        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to save leaderboard.", e);
            throw new DataAccessException("Unable to save leaderboard.", e);
        }
    }

    @Override
    public Optional<Leaderboard> getMasterLeaderboard(String season) {
        String query = "SELECT * FROM leaderboard WHERE scope = 'MASTER' AND season = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, season);

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    UUID resolvedLeagueId;
                    if (rs.getString("leagueId") == null){
                        resolvedLeagueId = null;
                    }else {
                        resolvedLeagueId = UUID.fromString(rs.getString("leagueId"));
                    }
                    Leaderboard lb = Leaderboard.builder()
                            .leaderboardId(UUID.fromString(rs.getString("leaderboardId")))
                            .leagueId(resolvedLeagueId)
                            .season(rs.getString("season"))
                            .scope(LeaderboardScope.valueOf(rs.getString("scope")))
                            .lastUpdated(rs.getObject("lastUpdated", LocalDateTime.class))
                            .build();

                    return Optional.of(lb);
                }
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to get master leaderboard by season " + season, e);
            throw new DataAccessException("Unable to get master leaderboard by season " + season, e);
        }
        return Optional.empty();
    }
}