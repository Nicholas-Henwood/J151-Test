package com.vzap.trytons.dao.fantasyteam;

import com.vzap.trytons.model.fantasyteam.FantasyTeamRoundSelection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FantasyTeamRoundSelectionDAO {
    Optional<FantasyTeamRoundSelection> createRoundSelection(FantasyTeamRoundSelection selection);
    int createRoundSelections(List<FantasyTeamRoundSelection> selections);
    Optional<FantasyTeamRoundSelection> getRoundSelectionById(UUID selectionId);
    List<FantasyTeamRoundSelection> getSelectionsByRoundId(UUID roundId);
    List<FantasyTeamRoundSelection> getSelectionsByRoundIdAndTeamId(UUID roundId, UUID teamId);
    List<UUID> getTeamIdsWithSnapshotsForRound(UUID roundId);
    boolean snapshotsExistForRound(UUID roundId);
    boolean snapshotExistsForTeamInRound(UUID roundId, UUID teamId);
    int countSelectionsByRoundId(UUID roundId);
    int countSelectionsByRoundIdAndTeamId(UUID roundId, UUID teamId);
}