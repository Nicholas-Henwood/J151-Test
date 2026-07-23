package com.vzap.trytons.service.simulation;

import com.vzap.trytons.dao.admin.AdminDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamRoundSelectionDAO;
import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.results.MatchResultDAO;
import com.vzap.trytons.dao.scoring.ScoringRuleDAO;
import com.vzap.trytons.dto.results.MatchResultResponseDTO;
import com.vzap.trytons.dto.simulation.ResimulationRequestDTO;
import com.vzap.trytons.dto.simulation.ResimulationResponseDTO;
import com.vzap.trytons.dto.simulation.SimulationSettingResponseDTO;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.model.fantasyteam.FantasyTeamRoundSelection;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.results.MatchResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ControlledResimulationServiceImpl implements ControlledResimulationService {
    @Inject
    private AdminDAO adminDAO;

    @Inject
    private FixtureDAO fixtureDAO;

    @Inject
    private MatchResultDAO matchResultDAO;

    @Inject
    private MatchSimulationService matchSimulationService;

    @Inject
    private SimulationSettingService simulationSettingService;

    @Inject
    private FantasyRoundDAO fantasyRoundDAO;

    @Inject
    private FantasyTeamRoundSelectionDAO roundSelectionDAO;

    @Inject
    private ScoringRuleDAO scoringRuleDAO;

    private static final int TEAM_SIZE = 20;

    @Override
    public ResimulationResponseDTO resimulateFixture(UUID actorUserId, ResimulationRequestDTO request) {

        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated administrator is required to resimulate a fixture.");
        }

        if (adminDAO.getAdminById(actorUserId).isEmpty()) {
            throw new AuthorisationException("Only administrators may resimulate fixtures.");
        }

        if (request == null) {
            throw new ValidationException("Resimulation details are required.");
        }

        if (request.getFixtureId() == null) {
            throw new ValidationException("Fixture ID is required.");
        }

        if (request.getResimulationReason() == null || request.getResimulationReason().isBlank()) {
            throw new ValidationException("A resimulation reason is required.");
        }

        UUID fixtureId = request.getFixtureId();
        String resimulationReason = request.getResimulationReason().trim();

        Fixture fixture = fixtureDAO.findFixtureById(fixtureId).orElseThrow(() -> new ResourceNotFoundException("Fixture was not found."));

        MatchResult previousResult = matchResultDAO.findCurrentByFixtureId(fixtureId).orElseThrow(() -> new ResourceNotFoundException("No current result exists for the fixture."));

        SimulationSettingResponseDTO settings = simulationSettingService.getActiveSimulationSetting();

        if (!Boolean.TRUE.equals(settings.getAllowResimulation())) {
            throw new BusinessRuleException("Resimulation is disabled in the active simulation settings.");
        }

        if (previousResult.isApproved()) {
            throw new BusinessRuleException("An approved match result cannot be resimulated.");
        }

        int completedResimulations = Math.max(0, previousResult.getSimulationRunNumber() - 1);
        if (completedResimulations >= settings.getMaxResimulations()) {
            throw new BusinessRuleException("The maximum number of resimulations has been reached for this fixture.");
        }

        FantasyRound round = fantasyRoundDAO.getRoundById(fixture.getRoundId()).orElseThrow(() -> new ResourceNotFoundException("Round not found"));

        LocalDateTime now = LocalDateTime.now();

        if (round.getLockDeadline() == null) {
            throw new BusinessRuleException("The round does not have a lock deadline.");
        }

        if (now.isBefore(round.getLockDeadline())) {
            throw new BusinessRuleException("The round has not reached its lock deadline.");
        }

        if ((round.getStatus() != FantasyRoundStatus.LOCKED) && (round.getStatus() != FantasyRoundStatus.IN_PROGRESS)) {
            throw new BusinessRuleException("The fixture can only be simulated when its round is locked.");
        }

        List<FantasyTeamRoundSelection> teamASelections = roundSelectionDAO.getSelectionsByRoundIdAndTeamId(round.getRoundId(), fixture.getTeamAId());
        List<FantasyTeamRoundSelection> teamBSelections = roundSelectionDAO.getSelectionsByRoundIdAndTeamId(round.getRoundId(), fixture.getTeamBId());

        if (teamASelections.size() != TEAM_SIZE) {
            throw new BusinessRuleException("Team A does not have a complete 20-player locked squad.");
        }

        if (teamBSelections.size() != TEAM_SIZE) {
            throw new BusinessRuleException("Team B does not have a complete 20-player locked squad.");
        }

        if (!round.getSeason().equalsIgnoreCase(settings.getSeason())) {
            throw new BusinessRuleException("The active simulation settings do not match the round season.");
        }

        if (scoringRuleDAO.findActiveRules(round.getSeason()).isEmpty()) {
            throw new BusinessRuleException("No active scoring rules exist for this round.");
        }

        int updatedResults = matchResultDAO.markAllFixtureResultsNotCurrent(fixtureId);
        if (updatedResults == 0) {
            throw new DataAccessException("The current match result could not be versioned.", null);
        }

        fixture.setStatus(FixtureStatus.LOCKED);

        if (!fixtureDAO.updateFixture(fixture)) {
            throw new DataAccessException("The fixture could not be prepared for resimulation.", null);
        }

        MatchResultResponseDTO newResult = matchSimulationService.simulateFixture(actorUserId, fixtureId);
        return ResimulationResponseDTO.builder()
                .previousResultId(previousResult.getResultId())
                .newResultId(newResult.getResultId())
                .fixtureId(fixtureId)
                .simulationRunNumber(newResult.getSimulationRunNumber())
                .current(newResult.isCurrent())
                .approved(newResult.isApproved())
                .resimulationReason(resimulationReason)
                .resimulatedAt(newResult.getResultDate())
                .build();
    }

    @Override
    public List<ResimulationResponseDTO> listResimulationsForFixture(UUID fixtureId) {

        if (fixtureId == null) {
            throw new ValidationException("Fixture ID is required.");
        }

        if (fixtureDAO.findFixtureById(fixtureId).isEmpty()) {
            throw new ResourceNotFoundException("Fixture was not found.");
        }

        List<MatchResult> resultHistory = matchResultDAO.findAllByFixtureId(fixtureId);

        List<ResimulationResponseDTO> resimulations = new ArrayList<>();

        for (int i = 1; i < resultHistory.size(); i++) {
            MatchResult previousResult = resultHistory.get(i - 1);
            MatchResult resimulatedResult = resultHistory.get(i);

            ResimulationResponseDTO response = ResimulationResponseDTO.builder()
                            .previousResultId(previousResult.getResultId())
                            .newResultId(resimulatedResult.getResultId())
                            .fixtureId(fixtureId)
                            .simulationRunNumber(resimulatedResult.getSimulationRunNumber())
                            .current(resimulatedResult.isCurrent())
                            .approved(resimulatedResult.isApproved())
                            .resimulationReason(null)
                            .resimulatedAt(resimulatedResult.getResultDate())
                            .build();

            resimulations.add(response);
        }

        return resimulations;
    }
}