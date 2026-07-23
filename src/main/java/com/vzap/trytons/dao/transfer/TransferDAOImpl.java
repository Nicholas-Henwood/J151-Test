package com.vzap.trytons.dao.transfer;

import com.vzap.trytons.enums.TransferStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.auth.RegisteredUser;
import com.vzap.trytons.model.transfer.Transfer;
import jakarta.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

@Singleton
public class TransferDAOImpl extends BaseDAO implements TransferDAO {

    private static final Logger LOG = Logger.getLogger(TransferDAOImpl.class.getName());

    private static final String SELECT_TRANSFER_WITH_PLAYERS = "SELECT t.*, removed.playerName AS removed_player_name, added.playerName AS added_player_name " +
            "FROM `transfer` t " +
            "JOIN player removed ON t.removed_player_id = removed.playerId " +
            "JOIN player added ON t.added_player_id = added.playerId ";

    @Override
    public Optional<Transfer> saveTransfer(Transfer transfer) {
        String query = "INSERT INTO `transfer` " +
                "(transferId, teamId, roundId, removed_player_id, added_player_id, transferDate, " +
                "removed_player_value, added_player_value, penaltyPoints, status, confirmedAt, created_by_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, transfer.getTransferId().toString());
            ps.setString(2, transfer.getTeamId().toString());
            ps.setString(3, transfer.getRoundId().toString());
            ps.setString(4, transfer.getRemovedPlayerId().toString());
            ps.setString(5, transfer.getAddedPlayerId().toString());
            ps.setTimestamp(6, Timestamp.valueOf(transfer.getTransferDate()));
            ps.setBigDecimal(7, transfer.getRemovedPlayerValue());
            ps.setBigDecimal(8, transfer.getAddedPlayerValue());
            ps.setInt(9, transfer.getPenaltyPoints());
            ps.setString(10, transfer.getStatus().name());

            if (transfer.getConfirmedAt() != null) {
                ps.setTimestamp(11, Timestamp.valueOf(transfer.getConfirmedAt()));
            } else {
                ps.setNull(11, Types.TIMESTAMP);
            }

            ps.setString(12, transfer.getCreatedByUserId().toString());

            if (ps.executeUpdate() == 1) {
                return Optional.of(transfer);
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to save transfer", e);
            throw new DataAccessException("Unable to save transfer", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Transfer> getTransferById(UUID transferId) {
        String query = SELECT_TRANSFER_WITH_PLAYERS + "WHERE t.transferId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, transferId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTransfer(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find transfer by ID", e);
            throw new DataAccessException("Unable to find transfer by ID", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Transfer> getTransfersByTeamId(UUID teamId) {
        String query = SELECT_TRANSFER_WITH_PLAYERS +
                "WHERE t.teamId = ? " +
                "ORDER BY t.transferDate DESC";

        List<Transfer> transfers = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, teamId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transfers.add(mapTransfer(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find transfers for team", e);
            throw new DataAccessException("Unable to find transfers for team", e);
        }

        return transfers;
    }

    @Override
    public List<Transfer> getTransfersByRound(UUID roundId) {
        String query = SELECT_TRANSFER_WITH_PLAYERS +
                "WHERE t.roundId = ? " +
                "ORDER BY t.transferDate DESC";

        List<Transfer> transfers = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, roundId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transfers.add(mapTransfer(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find transfers for round", e);
            throw new DataAccessException("Unable to find transfers for round", e);
        }

        return transfers;
    }

    @Override
    public boolean updateTransferStatus(UUID transferId, TransferStatus transferStatus, LocalDateTime confirmationDate) {
        String query = "UPDATE `transfer` SET status = ?, confirmedAt = ? WHERE transferId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, transferStatus.name());

            if (confirmationDate != null) {
                ps.setTimestamp(2, Timestamp.valueOf(confirmationDate));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }

            ps.setString(3, transferId.toString());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update transfer status", e);
            throw new DataAccessException("Unable to update transfer status", e);
        }
    }

    @Override
    public int countConfirmedTransfers(UUID teamId, UUID roundId) {
        String query = "SELECT COUNT(*) AS confirmedCount " +
                "FROM `transfer` " +
                "WHERE teamId = ? AND roundId = ? AND status = 'CONFIRMED'";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, teamId.toString());
            ps.setString(2, roundId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("confirmedCount");
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to count confirmed transfers", e);
            throw new DataAccessException("Unable to count confirmed transfers", e);
        }

        return 0;
    }

    @Override
    public boolean existsConfirmedTransfer(UUID teamId, UUID roundId, UUID removedPlayerId, UUID addedPlayerId) {
        String query = "SELECT COUNT(*) AS duplicateCount " +
                "FROM `transfer` " +
                "WHERE teamId = ? AND roundId = ? AND removed_player_id = ? AND added_player_id = ? AND status = 'CONFIRMED'";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, teamId.toString());
            ps.setString(2, roundId.toString());
            ps.setString(3, removedPlayerId.toString());
            ps.setString(4, addedPlayerId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("duplicateCount") > 0;
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to check duplicate confirmed transfer", e);
            throw new DataAccessException("Unable to check duplicate confirmed transfer", e);
        }
    }

    private Transfer mapTransfer(ResultSet rs) throws SQLException {
        Transfer transfer = new Transfer();

        transfer.setTransferId(UUID.fromString(rs.getString("transferId")));
        transfer.setTransferDate(rs.getTimestamp("transferDate").toLocalDateTime());
        transfer.setRemovedPlayerValue(rs.getBigDecimal("removed_player_value"));
        transfer.setAddedPlayerValue(rs.getBigDecimal("added_player_value"));
        transfer.setValueDifference(rs.getBigDecimal("valueDifference"));
        transfer.setPenaltyPoints(rs.getInt("penaltyPoints"));
        transfer.setStatus(TransferStatus.valueOf(rs.getString("status")));

        Timestamp confirmedAt = rs.getTimestamp("confirmedAt");
        transfer.setConfirmedAt(confirmedAt != null ? confirmedAt.toLocalDateTime() : null);

        FantasyTeam team = new FantasyTeam();
        team.setTeamId(UUID.fromString(rs.getString("teamId")));
        transfer.setTeamId(team.getTeamId());

        FantasyRound round = new FantasyRound();
        round.setRoundId(UUID.fromString(rs.getString("roundId")));
        transfer.setRoundId(round.getRoundId());

        transfer.setRemovedPlayerId(UUID.fromString(rs.getString("removed_player_id")));
        transfer.setRemovedPlayerName(rs.getString("removed_player_name"));

        transfer.setAddedPlayerId(UUID.fromString(rs.getString("added_player_id")));
        transfer.setAddedPlayerName(rs.getString("added_player_name"));

        RegisteredUser createdBy = new RegisteredUser();
        createdBy.setUserId(UUID.fromString(rs.getString("created_by_user_id")));
        transfer.setCreatedByUserId(createdBy.getUserId());

        return transfer;
    }
}