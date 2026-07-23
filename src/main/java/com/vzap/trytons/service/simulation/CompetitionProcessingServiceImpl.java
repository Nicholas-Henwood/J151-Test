package com.vzap.trytons.service.simulation;

import com.vzap.trytons.dao.admin.AdminDAO;
import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dto.leaderboard.LeaderboardRefreshResultDTO;
import com.vzap.trytons.dto.simulation.CompetitionProcessingSummaryDTO;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.exceptions.ApplicationException;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.service.fixture.DeadlineLockService;
import com.vzap.trytons.service.leaderboard.LeaderboardService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CompetitionProcessingServiceImpl implements CompetitionProcessingService {
    @Inject
    private AdminDAO adminDAO;

    @Inject
    private DeadlineLockService deadlineLockService;

    @Inject
    private FantasyRoundDAO fantasyRoundDAO;

    @Inject
    private FixtureDAO fixtureDAO;

    @Inject
    private MatchSimulationService matchSimulationService;

    @Inject
    private MatchProcessingService matchProcessingService;

    @Inject
    private LeaderboardService leaderboardService;

    @Override
    public CompetitionProcessingSummaryDTO processDueWork(UUID actorUserId) {
        requireAdmin(actorUserId);

        int roundsLocked = 0;
        int fixturesSimulated = 0;
        int fixturesProcessed = 0;
        int leaderboardsRefreshed = 0;
        int skipped = 0;
        int errors = 0;

        List<String> errorMessages = new ArrayList<>();
        List<String> skippedMessages = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        List<FantasyRound> openRounds = fantasyRoundDAO.getRoundsByStatus(FantasyRoundStatus.OPEN);
        for (FantasyRound round : openRounds) {
            if (round.getLockDeadline() != null && !now.isBefore(round.getLockDeadline())) {
                try {
                    deadlineLockService.lockRound(actorUserId, round.getRoundId(), "Automatic lock: round deadline has passed.");
                    roundsLocked++;
                } catch (ApplicationException e) {
                    skipped++;
                    skippedMessages.add("Round " + round.getRoundId() + " [" + e.getErrorCode() + "]: " + e.getMessage());
                } catch (Exception e) {
                    errors++;
                    errorMessages.add("Round " + round.getRoundId() + ": " + e.getMessage());
                }
            }
        }

        List<Fixture> lockedFixtures = fixtureDAO.findByStatus(FixtureStatus.LOCKED);
        for (Fixture fixture : lockedFixtures) {
            try {
                matchSimulationService.simulateFixture(actorUserId, fixture.getFixtureId());
                fixturesSimulated++;
            } catch (ApplicationException e) {
                skipped++;
                skippedMessages.add("Simulate fixture " + fixture.getFixtureId() + " [" + e.getErrorCode() + "]: " + e.getMessage());
            } catch (Exception e) {
                errors++;
                errorMessages.add("Fixture " + fixture.getFixtureId() + ": " + e.getMessage());
            }
        }

        List<Fixture> completedFixtures = fixtureDAO.findByStatus(FixtureStatus.COMPLETED);
        for (Fixture fixture : completedFixtures) {
            try {
                matchProcessingService.processCompletedFixture(actorUserId, fixture.getFixtureId());
                fixturesProcessed++;
                leaderboardsRefreshed++;
            } catch (ApplicationException e) {
                skipped++;
                skippedMessages.add("Process fixture " + fixture.getFixtureId() + " [" + e.getErrorCode() + "]: " + e.getMessage());
            } catch (Exception e) {
                errors++;
                errorMessages.add("Fixture " + fixture.getFixtureId() + ": " + e.getMessage());
            }
        }

        try {
            LeaderboardRefreshResultDTO overallRefresh = leaderboardService.refreshOverallLeaderboard(actorUserId);
            if (overallRefresh != null && overallRefresh.isSuccess()) {
                leaderboardsRefreshed++;
            }
        } catch (ApplicationException e) {
            skipped++;
            skippedMessages.add("Overall leaderboard refresh [" + e.getErrorCode() + "]: " + e.getMessage());
        } catch (Exception e) {
            errors++;
            errorMessages.add("Overall leaderboard refresh: " + e.getMessage());
        }

        return CompetitionProcessingSummaryDTO.builder()
                .processedAt(now)
                .roundsLocked(roundsLocked)
                .fixturesSimulated(fixturesSimulated)
                .fixturesProcessed(fixturesProcessed)
                .leaderboardsRefreshed(leaderboardsRefreshed)
                .skipped(skipped)
                .errors(errors)
                .errorMessages(errorMessages)
                .skippedMessages(skippedMessages)
                .build();
    }

    private void requireAdmin(UUID actorUserId) {
        if (adminDAO.getAdminById(actorUserId).isEmpty()) {
            throw new AuthorisationException("Only administrators may process competition due work.");
        }
    }
}
