package com.vzap.trytons.service.simulation;

import com.vzap.trytons.dao.admin.AdminDAO;
import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dao.catalog.PositionDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamRoundSelectionDAO;
import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.results.MatchResultDAO;
import com.vzap.trytons.dao.scoring.ScoringRuleDAO;
import com.vzap.trytons.dto.results.MatchResultResponseDTO;
import com.vzap.trytons.dto.results.PlayerStatisticsRequestDTO;
import com.vzap.trytons.dto.simulation.SimulationSettingResponseDTO;
import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.enums.MatchTeamSide;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.util.ScoringCalculator;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.PlayerAvailability;
import com.vzap.trytons.model.catalog.Position;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.fantasyteam.FantasyTeamRoundSelection;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.results.MatchResult;
import com.vzap.trytons.model.results.PlayerStatistics;
import com.vzap.trytons.model.scoring.ScoringRule;
import com.vzap.trytons.service.notification.NotificationService;
import com.vzap.trytons.service.results.PlayerStatisticsService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class MatchSimulationServiceImpl implements MatchSimulationService {
    private static final Logger LOG = Logger.getLogger(MatchSimulationServiceImpl.class.getName());

    @Inject
    private FixtureDAO fixtureDAO;

    @Inject
    private FantasyRoundDAO fantasyRoundDAO;

    @Inject
    private FantasyTeamRoundSelectionDAO roundSelectionDAO;

    @Inject
    private PlayerDAO playerDAO;

    @Inject
    private SimulationSettingService simulationSettingService;

    @Inject
    private MatchResultDAO matchResultDAO;

    @Inject
    private PlayerStatisticsService playerStatisticsService;

    @Inject
    private AdminDAO adminDAO;

    @Inject
    private ScoringRuleDAO scoringRuleDAO;

    @Inject
    PositionDAO positionDAO;

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private NotificationService notificationService;

    private static final int TEAM_SIZE = 20;

    @Override
    public MatchResultResponseDTO simulateFixture(UUID actorUserId, UUID fixtureId) {

        if (actorUserId == null) {
            throw new BusinessRuleException("An administrator is required to simulate a fixture.");
        }

        if (fixtureId == null) {
            throw new BusinessRuleException("Fixture ID is required.");
        }

        Fixture fixture = fixtureDAO.findFixtureById(fixtureId).orElseThrow(() -> new ResourceNotFoundException("Fixture not found"));

        if (adminDAO.getAdminById(actorUserId).isEmpty()) {
            throw new AuthorisationException("Only administrators may simulate fixtures.");
        }

        if (fixture.getStatus() != FixtureStatus.LOCKED) {
            throw new BusinessRuleException("Only a locked fixture can be simulated.");
        }

        if (matchResultDAO.findCurrentByFixtureId(fixtureId).isPresent()) {
            throw new ConflictException("A current result already exists for this fixture.");
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

        List<Player> teamA = new ArrayList<>();
        List<Player> teamB = new ArrayList<>();

        Map<UUID, AvailabilityStatus> availabilityByPlayerIdTeamA = new HashMap<>();
        Map<UUID, AvailabilityStatus> availabilityByPlayerIdTeamB = new HashMap<>();

        for (FantasyTeamRoundSelection selectionA : teamASelections) {
            Player player = playerDAO.getPlayerById(selectionA.getPlayerId()).orElseThrow(() -> new ResourceNotFoundException("A locked player for Team A could not be found."));

            PlayerAvailability availability = playerDAO.getCurrentAvailability(player.getPlayerId()).orElseThrow(() -> new BusinessRuleException("Team A player does not have a current availability."));

            teamA.add(player);
            availabilityByPlayerIdTeamA.put(player.getPlayerId(), availability.getStatus());
        }

        for (FantasyTeamRoundSelection selectionB : teamBSelections) {
            Player player = playerDAO.getPlayerById(selectionB.getPlayerId()).orElseThrow(() -> new ResourceNotFoundException("A locked player for Team B could not be found."));
            PlayerAvailability availability = playerDAO.getCurrentAvailability(player.getPlayerId()).orElseThrow(() -> new BusinessRuleException("Team B player does not have a current availability."));
            teamB.add(player);
            availabilityByPlayerIdTeamB.put(player.getPlayerId(), availability.getStatus());
        }

        SimulationSettingResponseDTO settings = simulationSettingService.getActiveSimulationSetting();

        if (!round.getSeason().equalsIgnoreCase(settings.getSeason())) {
            throw new BusinessRuleException("The active simulation settings do not match the round season.");
        }

        List<ScoringRule> scoringRules = scoringRuleDAO.findActiveRules(round.getSeason());

        if (scoringRules.isEmpty()) {
            throw new BusinessRuleException("No active scoring rules exist for this round.");
        }
        int simulationRunNumber = matchResultDAO.getNextSimulationRunNumber(fixtureId);

        long randomSeed = (long) fixtureId.hashCode() + simulationRunNumber;
        Random random = new Random(randomSeed);

        double teamBalanceA = calculateTeamBalance(teamA, availabilityByPlayerIdTeamA);
        double teamBalanceB = calculateTeamBalance(teamB, availabilityByPlayerIdTeamB);

        Map<UUID, Double> teamAPerformanceByPlayerId = new HashMap<>();
        Map<UUID, Double> teamBPerformanceByPlayerId = new HashMap<>();

        for (Player player : teamA) {
            AvailabilityStatus availability = availabilityByPlayerIdTeamA.get(player.getPlayerId());
            teamAPerformanceByPlayerId.put(player.getPlayerId(), calculateWeightedPerformance(player, availability, settings, teamBalanceA, random));
        }
        for (Player player : teamB) {
            AvailabilityStatus availability = availabilityByPlayerIdTeamB.get(player.getPlayerId());
            teamBPerformanceByPlayerId.put(player.getPlayerId(), calculateWeightedPerformance(player, availability, settings, teamBalanceB, random));
        }

        UUID resultId = UUID.randomUUID();
        LocalDateTime simulationDate = LocalDateTime.now();

        List<PlayerStatistics> teamAStatistics = new ArrayList<>();
        List<PlayerStatistics> teamBStatistics = new ArrayList<>();

        for (Player player : teamA) {
            AvailabilityStatus availability = availabilityByPlayerIdTeamA.get(player.getPlayerId());
            double weightedPerformance = teamAPerformanceByPlayerId.get(player.getPlayerId());
            PlayerStatistics statistics = generatePlayerStatistics(resultId, fixture.getTeamAId(), simulationDate, player, availability, weightedPerformance, random);
            teamAStatistics.add(statistics);
        }

        for (Player player : teamB) {
            AvailabilityStatus availability = availabilityByPlayerIdTeamB.get(player.getPlayerId());
            double weightedPerformance = teamBPerformanceByPlayerId.get(player.getPlayerId());
            PlayerStatistics statistics = generatePlayerStatistics(resultId, fixture.getTeamBId(), simulationDate, player, availability, weightedPerformance, random);
            teamBStatistics.add(statistics);
        }

        int teamATries = calculateTeamTries(teamAStatistics);
        int teamBTries = calculateTeamTries(teamBStatistics);

        Player teamAKicker = selectDesignatedKicker(teamA, availabilityByPlayerIdTeamA);
        Player teamBKicker = selectDesignatedKicker(teamB, availabilityByPlayerIdTeamB);

        applyKickingStatistics(teamAStatistics, teamAKicker, teamAPerformanceByPlayerId.get(teamAKicker.getPlayerId()), teamATries, random);
        applyKickingStatistics(teamBStatistics, teamBKicker, teamBPerformanceByPlayerId.get(teamBKicker.getPlayerId()), teamBTries, random);

        int teamAScore = calculateTeamScore(teamAStatistics, scoringRules);
        int teamBScore = calculateTeamScore(teamBStatistics, scoringRules);

        MatchTeamSide winnerSide = null;
        boolean draw = (teamAScore == teamBScore);

        if (teamAScore > teamBScore) {
            winnerSide = MatchTeamSide.TEAM_A;
        } else if (teamBScore > teamAScore) {
            winnerSide = MatchTeamSide.TEAM_B;
        }

        MatchResult result = MatchResult.builder()
                .resultId(resultId)
                .fixtureId(fixtureId)
                .settingsId(settings.getSettingsId())
                .simulationRunNumber(simulationRunNumber)
                .teamAScore(teamAScore)
                .teamBScore(teamBScore)
                .winnerSide(winnerSide)
                .isDraw(draw)
                .approved(false)
                .isCurrent(true)
                .resultDate(simulationDate)
                .approvedByAdminUserId(null)
                .build();

        MatchResult savedResult = matchResultDAO.save(result);

        for (PlayerStatistics statistics : teamAStatistics) {
            playerStatisticsService.captureStatistic(actorUserId, statsToRequestDTO(statistics));
        }

        for (PlayerStatistics statistics : teamBStatistics) {
            playerStatisticsService.captureStatistic(actorUserId, statsToRequestDTO(statistics));
        }

        fixture.setSimulationDate(simulationDate);
        fixture.setStatus(FixtureStatus.COMPLETED);
        fixtureDAO.updateFixture(fixture);

        notifySimulatedResult(fixture);

        return resultToMatchResultDTO(savedResult, fixture);
    }

    private void notifySimulatedResult(Fixture fixture) {
        try {
            notifyTeamOwnerOfResult(fixture.getTeamAId(), fixture.getFixtureId());
            notifyTeamOwnerOfResult(fixture.getTeamBId(), fixture.getFixtureId());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send simulated-result notifications for fixture " + fixture.getFixtureId(), e);
        }
    }

    private void notifyTeamOwnerOfResult(UUID teamId, UUID fixtureId) {
        if (teamId == null) {
            return;
        }
        fantasyTeamDAO.getTeamById(teamId)
                .map(FantasyTeam::getOwnerUserId)
                .ifPresent(ownerId -> notificationService.notifySimulatedResult(ownerId, fixtureId));
    }

    private MatchResultResponseDTO resultToMatchResultDTO(MatchResult result, Fixture fixture) {
        return MatchResultResponseDTO.builder()
                .resultId(result.getResultId())
                .fixtureId(result.getFixtureId())
                .teamAId(fixture.getTeamAId())
                .teamBId(fixture.getTeamBId())
                .simulationRunNumber(result.getSimulationRunNumber())
                .teamAScore(result.getTeamAScore())
                .teamBScore(result.getTeamBScore())
                .winnerSide(result.getWinnerSide())
                .isDraw(result.isDraw())
                .approved(result.isApproved())
                .isCurrent(result.isCurrent())
                .resultDate(result.getResultDate())
                .approvedByAdminUserId(result.getApprovedByAdminUserId())
                .build();
    }

    private PlayerStatisticsRequestDTO statsToRequestDTO(PlayerStatistics statistics) {
        return PlayerStatisticsRequestDTO.builder()
                .resultId(statistics.getResultId())
                .teamId(statistics.getTeamId())
                .playerId(statistics.getPlayerId())
                .tries(statistics.getTries())
                .assists(statistics.getAssists())
                .tackles(statistics.getTackles())
                .missedTackles(statistics.getMissedTackles())
                .conversions(statistics.getConversions())
                .penalties(statistics.getPenalties())
                .metersGained(statistics.getMetersGained())
                .yellowCards(statistics.getYellowCards())
                .redCards(statistics.getRedCards())
                .build();
    }

    private Player selectDesignatedKicker(List<Player> players, Map<UUID, AvailabilityStatus> availabilityByPlayerId) {
        Player designatedKicker = null;

        for (Player player : players) {
            AvailabilityStatus availability = availabilityByPlayerId.get(player.getPlayerId());
            boolean canPlay = availability != AvailabilityStatus.SUSPENDED && availability != AvailabilityStatus.UNAVAILABLE;

            if (canPlay && (designatedKicker == null || player.getKickingAbility() > designatedKicker.getKickingAbility())) {
                designatedKicker = player;
            }
        }

        if (designatedKicker == null) {
            throw new BusinessRuleException("The team does not have an available designated kicker.");
        }

        return designatedKicker;
    }

    private int calculateTeamTries(List<PlayerStatistics> statistics) {
        int totalTries = 0;

        for (PlayerStatistics playerStatistics : statistics) {
            totalTries += playerStatistics.getTries();
        }

        return totalTries;
    }

    private PlayerStatistics generatePlayerStatistics(UUID resultId, UUID teamId, LocalDateTime statisticDate, Player player, AvailabilityStatus availability, double weightedPerformance, Random random) {

        int tries = 0;
        int assists = 0;
        int tackles = 0;
        int missedTackles = 0;
        int conversions = 0;
        int penalties = 0;
        int metersGained = 0;
        int yellowCards = 0;
        int redCards = 0;

        boolean canPlay = (availability != AvailabilityStatus.SUSPENDED && availability != AvailabilityStatus.UNAVAILABLE);

        if (canPlay) {
            Position position = positionDAO.findById(player.getPositionId()).orElseThrow(() -> new ResourceNotFoundException("The player's position could not be found."));
            String positionName = position.getPositionName();

            double attackingPerformance = calculateAttackingPerformance(player, weightedPerformance);
            double defensivePerformance = calculateDefensivePerformance(player, weightedPerformance);
            double disciplinePerformance = calculateDisciplinePerformance(player, weightedPerformance);

            int tryOpportunities = getRandomOpportunities(random, 0, 3);
            double tryChance = attackingPerformance / 300.0 * getTryPositionMultiplier(positionName);

            if (tryChance > 1.0) {
                tryChance = 1.0;
            }

            for (int opportunity = 0; opportunity < tryOpportunities; opportunity++) {
                if (random.nextDouble() < tryChance) {
                    tries++;
                }
            }

            int assistOpportunities = getRandomOpportunities(random, 0, 3);
            double assistChance = attackingPerformance / 375.0 * getAssistPositionMultiplier(positionName);

            if (assistChance > 1.0) {
                assistChance = 1.0;
            }

            for (int opportunity = 0; opportunity < assistOpportunities; opportunity++) {
                if (random.nextDouble() < assistChance) {
                    assists++;
                }
            }

            int tackleOpportunities = getRandomOpportunities(random, 5, 15);
            double tackleChance = defensivePerformance / 100.0 * getTacklePositionMultiplier(positionName);

            if (tackleChance > 1.0) {
                tackleChance = 1.0;
            }

            for (int opportunity = 0; opportunity < tackleOpportunities; opportunity++) {
                if (random.nextDouble() < tackleChance) {
                    tackles++;
                }
            }

            int missedTackleOpportunities = getRandomOpportunities(random, 0, 5);
            double missedTackleRisk = (100.0 - defensivePerformance) / 100.0;
            double missedTackleChance = missedTackleRisk * getMissedTacklePositionMultiplier(positionName);

            if (missedTackleChance > 1.0) {
                missedTackleChance = 1.0;
            }

            for (int opportunity = 0; opportunity < missedTackleOpportunities; opportunity++) {
                if (random.nextDouble() < missedTackleChance) {
                    missedTackles++;
                }
            }

            int carryOpportunities = getRandomOpportunities(random, 2, 10);
            double metersMultiplier = getMetersPositionMultiplier(positionName);

            for (int opportunity = 0; opportunity < carryOpportunities; opportunity++) {
                double carryVariation = 0.75 + random.nextDouble() * 0.50;
                int metersForCarry = (int) Math.round(attackingPerformance / 10.0 * metersMultiplier * carryVariation);
                metersGained += metersForCarry;
            }

            int cardOpportunities = getRandomOpportunities(random, 0, 4);
            double cardRisk = (100.0 - disciplinePerformance) / 100.0;
            double cardMultiplier = getCardPositionMultiplier(positionName);
            double yellowCardChance = cardRisk * cardMultiplier * 0.03;
            double redCardChance = cardRisk * cardMultiplier * 0.005;

            for (int opportunity = 0; opportunity < cardOpportunities; opportunity++) {
                if (random.nextDouble() < redCardChance) {
                    redCards = 1;
                    break;
                }

                if (random.nextDouble() < yellowCardChance) {
                    yellowCards = 1;
                    break;
                }
            }
        }

        return PlayerStatistics.builder().statId(UUID.randomUUID())
                .resultId(resultId)
                .teamId(teamId)
                .playerId(player.getPlayerId())
                .tries(tries)
                .assists(assists)
                .tackles(tackles)
                .missedTackles(missedTackles)
                .conversions(conversions)
                .penalties(penalties)
                .metersGained(metersGained)
                .yellowCards(yellowCards)
                .redCards(redCards)
                .statisticDate(statisticDate)
                .correctedByAdminUserId(null)
                .correctionReason(null)
                .correctedAt(null)
                .build();
    }

    private void applyKickingStatistics(List<PlayerStatistics> statistics, Player kicker, double weightedPerformance, int teamTries, Random random) {
        double kickingPerformance = calculateKickingPerformance(kicker, weightedPerformance);
        double kickSuccessChance = Math.max(0.0, Math.min(1.0, kickingPerformance / 100.0));

        int conversions = 0;

        for (int attempt = 0; attempt < teamTries; attempt++) {
            if (random.nextDouble() < kickSuccessChance) {
                conversions++;
            }
        }

        int penalties = 0;
        int penaltyOpportunities = getRandomOpportunities(random, 0, 3);

        for (int opportunity = 0; opportunity < penaltyOpportunities; opportunity++) {
            if (random.nextDouble() < kickSuccessChance) {
                penalties++;
            }
        }

        for (int index = 0; index < statistics.size(); index++) {
            PlayerStatistics currentStatistics = statistics.get(index);

            if (currentStatistics.getPlayerId().equals(kicker.getPlayerId())) {
                PlayerStatistics updatedStatistics = PlayerStatistics.builder()
                        .statId(currentStatistics.getStatId())
                        .resultId(currentStatistics.getResultId())
                        .teamId(currentStatistics.getTeamId())
                        .playerId(currentStatistics.getPlayerId())
                        .tries(currentStatistics.getTries())
                        .assists(currentStatistics.getAssists())
                        .tackles(currentStatistics.getTackles())
                        .missedTackles(currentStatistics.getMissedTackles())
                        .conversions(conversions)
                        .penalties(penalties)
                        .metersGained(currentStatistics.getMetersGained())
                        .yellowCards(currentStatistics.getYellowCards())
                        .redCards(currentStatistics.getRedCards())
                        .statisticDate(currentStatistics.getStatisticDate())
                        .correctedByAdminUserId(currentStatistics.getCorrectedByAdminUserId())
                        .correctionReason(currentStatistics.getCorrectionReason())
                        .correctedAt(currentStatistics.getCorrectedAt())
                        .build();

                statistics.set(index, updatedStatistics);
                return;
            }
        }

        throw new BusinessRuleException("The designated kicker's statistics could not be found.");
    }

    private int calculateTeamScore(List<PlayerStatistics> statistics, List<ScoringRule> scoringRules) {
        int score = 0;

        for (PlayerStatistics playerStatistics : statistics) {
            score += ScoringCalculator.calculate(playerStatistics, scoringRules).totalPoints();
        }

        return score;
    }

    private int getRandomOpportunities(Random random, int minimum, int maximum) {
        return minimum + random.nextInt(maximum - minimum + 1);
    }

    private double getCardPositionMultiplier(String positionName) {
        if (positionName == null) {
            return 1.0;
        }

        if (positionName.equalsIgnoreCase("Loose Forward")) {
            return 1.25;
        }

        if (positionName.equalsIgnoreCase("Prop") || positionName.equalsIgnoreCase("Hooker")) {
            return 1.15;
        }

        if (positionName.equalsIgnoreCase("Lock")) {
            return 1.10;
        }

        if (positionName.equalsIgnoreCase("Centre")) {
            return 1.00;
        }

        if (positionName.equalsIgnoreCase("Scrum Half") || positionName.equalsIgnoreCase("Fly Half")) {
            return 0.90;
        }

        if (positionName.equalsIgnoreCase("Wing")) {
            return 0.85;
        }

        if (positionName.equalsIgnoreCase("Fullback")) {
            return 0.80;
        }

        return 1.0;
    }

    private double getMetersPositionMultiplier(String positionName) {
        if (positionName == null) {
            return 1.0;
        }

        if (positionName.equalsIgnoreCase("Wing")) {
            return 1.40;
        }

        if (positionName.equalsIgnoreCase("Fullback")) {
            return 1.30;
        }

        if (positionName.equalsIgnoreCase("Centre")) {
            return 1.20;
        }

        if (positionName.equalsIgnoreCase("Loose Forward")) {
            return 1.10;
        }

        if (positionName.equalsIgnoreCase("Fly Half") || positionName.equalsIgnoreCase("Scrum Half")) {
            return 1.00;
        }

        if (positionName.equalsIgnoreCase("Hooker")) {
            return 0.85;
        }

        if (positionName.equalsIgnoreCase("Prop") || positionName.equalsIgnoreCase("Lock")) {
            return 0.80;
        }

        return 1.0;
    }

    private double getMissedTacklePositionMultiplier(String positionName) {
        if (positionName == null) {
            return 1.0;
        }

        if (positionName.equalsIgnoreCase("Centre")) {
            return 1.25;
        }

        if (positionName.equalsIgnoreCase("Wing")) {
            return 1.20;
        }

        if (positionName.equalsIgnoreCase("Fly Half")) {
            return 1.15;
        }

        if (positionName.equalsIgnoreCase("Fullback")) {
            return 1.10;
        }

        if (positionName.equalsIgnoreCase("Scrum Half")) {
            return 1.00;
        }

        if (positionName.equalsIgnoreCase("Loose Forward")) {
            return 0.95;
        }

        if (positionName.equalsIgnoreCase("Prop") || positionName.equalsIgnoreCase("Hooker")) {
            return 0.90;
        }

        if (positionName.equalsIgnoreCase("Lock")) {
            return 0.85;
        }

        return 1.0;
    }

    private double getTacklePositionMultiplier(String positionName) {
        if (positionName == null) {
            return 1.0;
        }

        if (positionName.equalsIgnoreCase("Loose Forward")) {
            return 1.40;
        }

        if (positionName.equalsIgnoreCase("Hooker")) {
            return 1.25;
        }

        if (positionName.equalsIgnoreCase("Lock")) {
            return 1.20;
        }

        if (positionName.equalsIgnoreCase("Prop")) {
            return 1.15;
        }

        if (positionName.equalsIgnoreCase("Centre")) {
            return 1.10;
        }

        if (positionName.equalsIgnoreCase("Scrum Half")) {
            return 0.95;
        }

        if (positionName.equalsIgnoreCase("Fly Half")) {
            return 0.90;
        }

        if (positionName.equalsIgnoreCase("Wing") || positionName.equalsIgnoreCase("Fullback")) {
            return 0.80;
        }

        return 1.0;
    }

    private double getAssistPositionMultiplier(String positionName) {
        if (positionName == null) {
            return 1.0;
        }

        if (positionName.equalsIgnoreCase("Fly Half")) {
            return 1.40;
        }

        if (positionName.equalsIgnoreCase("Scrum Half")) {
            return 1.30;
        }

        if (positionName.equalsIgnoreCase("Centre")) {
            return 1.20;
        }

        if (positionName.equalsIgnoreCase("Fullback")) {
            return 1.10;
        }

        if (positionName.equalsIgnoreCase("Wing") || positionName.equalsIgnoreCase("Loose Forward")) {
            return 0.90;
        }

        if (positionName.equalsIgnoreCase("Hooker")) {
            return 0.80;
        }

        if (positionName.equalsIgnoreCase("Prop") || positionName.equalsIgnoreCase("Lock")) {
            return 0.70;
        }

        return 1.0;
    }

    private double getTryPositionMultiplier(String positionName) {
        if (positionName == null) {
            return 1.0;
        }

        if (positionName.equalsIgnoreCase("Wing")) {
            return 1.40;
        }

        if (positionName.equalsIgnoreCase("Hooker")) {
            return 1.25;
        }

        if (positionName.equalsIgnoreCase("Centre") || positionName.equalsIgnoreCase("Fullback")) {
            return 1.20;
        }

        if (positionName.equalsIgnoreCase("Loose Forward")) {
            return 1.00;
        }

        if (positionName.equalsIgnoreCase("Scrum Half") || positionName.equalsIgnoreCase("Fly Half")) {
            return 0.90;
        }

        if (positionName.equalsIgnoreCase("Lock")) {
            return 0.80;
        }

        if (positionName.equalsIgnoreCase("Prop")) {
            return 0.75;
        }

        return 1.0;
    }

    private double calculateAttackingPerformance(Player player, double weightedPerformance) {
        return player.getAttackingAbility() * 0.60 + weightedPerformance * 0.40;
    }

    private double calculateDefensivePerformance(Player player, double weightedPerformance) {
        return player.getDefensiveAbility() * 0.60 + weightedPerformance * 0.40;
    }

    private double calculateKickingPerformance(Player player, double weightedPerformance) {
        return player.getKickingAbility() * 0.60 + weightedPerformance * 0.40;
    }

    private double calculateDisciplinePerformance(Player player, double weightedPerformance) {
        return player.getDiscipline() * 0.80 + weightedPerformance * 0.20;
    }

    private double calculateRandomMultiplier(Player player, double randomnessWeight, Random random) {
        double randomDirection = (random.nextDouble() * 2.0) - 1.0;
        double consistencyMultiplier = (100.0 - player.getConsistency()) / 100.0;
        double randomVariation = randomDirection * randomnessWeight * consistencyMultiplier;
        return 1.0 + randomVariation;
    }

    private double calculateWeightedPerformance(Player player, AvailabilityStatus availability, SimulationSettingResponseDTO settings, double teamBalance, Random random) {
        double abilityWeight = settings.getPlayerAbilityWeight().doubleValue();
        double formWeight = settings.getPlayerFormWeight().doubleValue();
        double teamBalanceWeight = settings.getTeamBalanceWeight().doubleValue();
        double randomnessWeight = settings.getRandomVariationWeight().doubleValue();

        Double playerAbility = getPlayerAbility(availability, player);

        if (playerAbility == null) {
            return 0.0;
        }

        double abilityPerformance = playerAbility * abilityWeight;
        double formPerformance = player.getCurrentForm() * formWeight;
        double teamBalancePerformance = teamBalance * teamBalanceWeight;
        double basePerformance = abilityPerformance + formPerformance + teamBalancePerformance;

        double randomMultiplier = calculateRandomMultiplier(player, randomnessWeight, random);
        double fitnessMultiplier = player.getFitness() / 100.0;
        double weightedPerformance = basePerformance * randomMultiplier * fitnessMultiplier;

        return Math.max(0.0, Math.min(100.0, weightedPerformance));
    }

    private double calculateTeamBalance(List<Player> players, Map<UUID, AvailabilityStatus> availabilityByPlayerId) {
        double totalTeamAbility = 0.0;
        if (players.isEmpty()) {
            return 0.0;
        }
        for (Player player : players) {
            AvailabilityStatus availability = availabilityByPlayerId.get(player.getPlayerId());
            Double playerAbility = getPlayerAbility(availability, player);
            if (playerAbility == null) continue;
            totalTeamAbility += playerAbility;
        }
        return totalTeamAbility / players.size();
    }

    private static Double getPlayerAbility(AvailabilityStatus availability, Player player) {
        double playerAbility;

        if (availability == AvailabilityStatus.UNAVAILABLE || availability == AvailabilityStatus.SUSPENDED) {
            return null;
        }

        int totalStats = player.getAttackingAbility() + player.getDefensiveAbility() + player.getKickingAbility();
        if (availability == AvailabilityStatus.INJURED) {
            playerAbility = totalStats / 6.0;
        } else {
            playerAbility = totalStats / 3.0;
        }
        return playerAbility;
    }
}
