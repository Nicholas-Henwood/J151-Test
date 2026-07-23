package com.vzap.trytons.service.admin;

import com.vzap.trytons.dao.admin.LogDAO;
import com.vzap.trytons.dao.admin.SystemReportDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamPlayerDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.leaderboard.LeaderboardDAO;
import com.vzap.trytons.dao.league.LeagueDAO;
import com.vzap.trytons.dao.results.MatchResultDAO;
import com.vzap.trytons.dao.scoring.FantasyPointsDAO;
import com.vzap.trytons.dao.transfer.TransferDAO;
import com.vzap.trytons.dto.admin.SystemReportRequestDTO;
import com.vzap.trytons.dto.admin.SystemReportResponseDTO;
import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.enums.SystemReportType;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.admin.Log;
import com.vzap.trytons.model.admin.LogActionCount;
import com.vzap.trytons.model.admin.SystemReport;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.fantasyteam.PlayerSelectionCount;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.leaderboard.Leaderboard;
import com.vzap.trytons.model.leaderboard.Ranking;
import com.vzap.trytons.model.league.League;
import com.vzap.trytons.model.results.MatchResult;
import com.vzap.trytons.model.scoring.PlayerPointSummary;
import com.vzap.trytons.model.transfer.Transfer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class SystemReportServiceImpl implements SystemReportService {
    @Inject
    private SystemReportDAO systemReportDAO;
    @Inject
    private UserDAO userDAO;
    @Inject
    private LeagueDAO leagueDAO;
    @Inject
    private PlayerDAO playerDAO;
    @Inject
    private FixtureDAO fixtureDAO;
    @Inject
    private MatchResultDAO matchResultDAO;
    @Inject
    private TransferDAO transferDAO;
    @Inject
    private LeaderboardDAO leaderboardDAO;
    @Inject
    private FantasyPointsDAO fantasyPointsDAO;
    @Inject
    private FantasyTeamPlayerDAO fantasyTeamPlayerDAO;
    @Inject
    private LogDAO logDAO;

    @Override
    public SystemReportResponseDTO generateReport(UUID actorUserId, SystemReportRequestDTO request) {
        validateAdministrator(actorUserId);
        validateRequest(request);

        Map<String,Object> parameters = request.getParametersJson() != null ? request.getParametersJson(): new LinkedHashMap<>();
        Map<String, Object> resultData = collectReportData(request.getReportType(),parameters);

        SystemReport systemReport = new SystemReport();

        systemReport.setReportId(UUID.randomUUID());
        systemReport.setGeneratedByAdminUserId(actorUserId);
        systemReport.setReportType(request.getReportType());
        systemReport.setReportTitle(request.getReportTitle().trim());
        systemReport.setParametersJson(parameters);
        systemReport.setResultJson(resultData);
        systemReport.setGeneratedAt(LocalDateTime.now());

        return mapToResponse(systemReportDAO.save(systemReport));
    }

    @Override
    public List<SystemReportResponseDTO> listReports(UUID actorUserId) {
        validateAdministrator(actorUserId);

        List<SystemReportResponseDTO> responses = new ArrayList<>();

        for(SystemReport systemReport : systemReportDAO.findAll()){
            responses.add(mapToResponse(systemReport));
        }

        return responses;
    }

    @Override
    public SystemReportResponseDTO getReportById(UUID actorUserId, UUID reportId) {
        validateAdministrator(actorUserId);
        if(reportId == null){
            throw new ValidationException("Report id is required.");
        }
        SystemReport systemReport = systemReportDAO.findById(reportId).orElseThrow(() -> new ResourceNotFoundException("systemReport"));
        return mapToResponse(systemReport);
    }

    private void validateAdministrator(UUID actorUserId) {

        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated administrator ID is required.");
        }

        User user = userDAO.getUserById(actorUserId).orElseThrow(() -> new AuthorisationException("The authenticated administrator could not be found."));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthorisationException("The authenticated administrator account is inactive.");
        }

        if (user.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Administrator access is required.");
        }
    }
    private void validateRequest(SystemReportRequestDTO request) {

        if (request == null) {
            throw new ValidationException(" A system reports are required.");
        }

        if (request.getReportType() == null) {
            throw new ValidationException("A report type is required.");
        }

        if (request.getReportTitle() == null || request.getReportTitle().trim().isEmpty()) {
            throw new ValidationException("A report title is required.");
        }

        if (request.getReportTitle().trim().length() > 150) {
            throw new ValidationException("Report title cannot be longer than 150 characters.");
        }

    }
    private Map<String, Object> collectReportData(SystemReportType reportType, Map<String, Object> parameters) {
        return switch (reportType) {
            case ACTIVE_USERS -> buildActiveUsersReport();
            case ACTIVE_LEAGUES -> buildActiveLeaguesReport();
            case UNAVAILABLE_PLAYERS -> buildUnavailablePlayersReport();
            case COMPLETED_FIXTURES -> buildCompletedFixturesReport();
            case FIXTURE_RESULTS -> buildFixtureResultsReport();
            case TRANSFER_ACTIVITY -> buildTransferActivityReport(parameters);
            case TOP_FANTASY_TEAMS -> buildTopFantasyTeamsReport(parameters);
            case TOP_RUGBY_PLAYERS -> buildTopRugbyPlayersReport(parameters);
            case MOST_SELECTED_PLAYERS -> buildMostSelectedPlayersReport(parameters);
            case SYSTEM_ACTIVITY -> buildSystemActivityReport(parameters);
            case LEAGUE_CHAT_ACTIVITY -> throw new ValidationException("League chat activity reports are not yet supported.");
        };
    }

    private Map<String, Object> buildActiveUsersReport() {
        List<User> users = userDAO.getActiveUsers();

        List<Map<String, Object>> items = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", user.getUserId().toString());
            item.put("username", user.getUsername());
            item.put("role", user.getRole() != null ? user.getRole().toString() : null);
            items.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("activeUserCount", users.size());
        data.put("users", items);
        return data;
    }

    private Map<String, Object> buildActiveLeaguesReport() {
        List<League> activeLeagues = leagueDAO.findAllLeagues().stream()
                .filter(league -> Boolean.TRUE.equals(league.getIsActive()))
                .toList();

        List<Map<String, Object>> items = new ArrayList<>();
        for (League league : activeLeagues) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("leagueId", league.getLeagueId().toString());
            item.put("leagueName", league.getLeagueName());
            item.put("leagueType", league.getLeagueType() != null ? league.getLeagueType().toString() : null);
            item.put("maxMembers", league.getMaxMembers());
            items.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("activeLeagueCount", activeLeagues.size());
        data.put("leagues", items);
        return data;
    }

    private Map<String, Object> buildUnavailablePlayersReport() {
        List<Player> players = playerDAO.searchPlayers(
                null, null, null, null, null, null, null,
                AvailabilityStatus.UNAVAILABLE, null);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Player player : players) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("playerId", player.getPlayerId().toString());
            item.put("playerName", player.getPlayerName());
            item.put("clubId", player.getClubId() != null ? player.getClubId().toString() : null);
            items.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("unavailablePlayerCount", players.size());
        data.put("players", items);

        return data;
    }

    private Map<String, Object> buildCompletedFixturesReport() {
        List<Fixture> fixtures = fixtureDAO.findByStatus(FixtureStatus.COMPLETED);
        List<Map<String, Object>> items = new ArrayList<>();

        for (Fixture fixture : fixtures) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("fixtureId", fixture.getFixtureId().toString());
            item.put("leagueId", fixture.getLeagueId() != null ? fixture.getLeagueId().toString() : null);
            item.put("roundId", fixture.getRoundId() != null ? fixture.getRoundId().toString() : null);
            item.put("fixtureDate", fixture.getFixtureDate() != null ? fixture.getFixtureDate().toString() : null);
            items.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("completedFixtureCount", fixtures.size());
        data.put("fixtures", items);
        return data;
    }

    private Map<String, Object> buildFixtureResultsReport() {
        List<Fixture> completedFixtures = fixtureDAO.findByStatus(FixtureStatus.COMPLETED);
        List<Map<String, Object>> items = new ArrayList<>();

        for (Fixture fixture : completedFixtures) {
            Optional<MatchResult> resultOpt = matchResultDAO.findCurrentByFixtureId(fixture.getFixtureId());
            if (resultOpt.isEmpty()) {
                continue;
            }
            MatchResult result = resultOpt.get();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("fixtureId", fixture.getFixtureId().toString());
            item.put("teamAScore", result.getTeamAScore());
            item.put("teamBScore", result.getTeamBScore());
            item.put("winnerSide", result.getWinnerSide() != null ? result.getWinnerSide().toString() : null);
            item.put("isDraw", result.isDraw());
            items.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fixtureResultCount", items.size());
        data.put("results", items);

        return data;
    }

    private Map<String, Object> buildTransferActivityReport(Map<String, Object> parameters) {
        UUID roundId = extractRequiredUuid(parameters, "roundId");

        List<Transfer> transfers = transferDAO.getTransfersByRound(roundId);
        List<Map<String, Object>> items = new ArrayList<>();

        for (Transfer transfer : transfers) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("transferId", transfer.getTransferId().toString());
            item.put("teamId", transfer.getTeamId() != null ? transfer.getTeamId().toString() : null);
            item.put("removedPlayerId", transfer.getRemovedPlayerId() != null ? transfer.getRemovedPlayerId().toString() : null);
            item.put("addedPlayerId", transfer.getAddedPlayerId() != null ? transfer.getAddedPlayerId().toString() : null);
            item.put("status", transfer.getStatus() != null ? transfer.getStatus().toString() : null);
            item.put("penaltyPoints", transfer.getPenaltyPoints());
            items.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("transferCount", transfers.size());
        data.put("transfers", items);

        return data;
    }

    private Map<String, Object> buildTopFantasyTeamsReport(Map<String, Object> parameters) {
        String season = extractRequiredString(parameters, "season");
        int limit = extractLimit(parameters, 10);

        Leaderboard leaderboard = leaderboardDAO.getMasterLeaderboard(season).orElseThrow(() -> new ResourceNotFoundException("leaderboard"));

        List<Ranking> rankings = leaderboardDAO.getRankingsByLeaderboardId(leaderboard.getLeaderboardId()).stream()
                .sorted(Comparator.comparingInt(Ranking::getCurrentRanking))
                .limit(limit)
                .toList();

        List<Map<String, Object>> items = new ArrayList<>();
        for (Ranking ranking : rankings) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("teamId", ranking.getTeamId().toString());
            item.put("currentRanking", ranking.getCurrentRanking());
            item.put("totalFantasyPoints", ranking.getTotalFantasyPoints());
            item.put("leaguePoints", ranking.getLeaguePoints());
            items.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("season", season);
        data.put("teamCount", items.size());
        data.put("teams", items);

        return data;
    }

    private Map<String, Object> buildTopRugbyPlayersReport(Map<String, Object> parameters) {
        int limit = extractLimit(parameters, 10);
        List<PlayerPointSummary> summaries = fantasyPointsDAO.findTopPlayerByFinalPoints(limit);

        List<Map<String, Object>> items = new ArrayList<>();
        for (PlayerPointSummary summary : summaries) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("playerId", summary.getPlayerId().toString());
            item.put("totalPoints", summary.getTotalPoints());
            items.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("playerCount", items.size());
        data.put("players", items);
        return data;
    }

    private Map<String, Object> buildMostSelectedPlayersReport(Map<String, Object> parameters) {
        int limit = extractLimit(parameters, 10);
        List<PlayerSelectionCount> counts = fantasyTeamPlayerDAO.findMostSelectedPlayers(limit);
        List<Map<String, Object>> items = new ArrayList<>();

        for (PlayerSelectionCount count : counts) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("playerId", count.getPlayerId().toString());
            item.put("selectionCount", count.getSelectionCount());
            items.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("playerCount", items.size());
        data.put("players", items);

        return data;
    }

    private Map<String, Object> buildSystemActivityReport(Map<String, Object> parameters) {
        int limit = extractLimit(parameters, 20);

        List<Log> recentLogs = logDAO.findRecentLogs(limit);
        List<LogActionCount> actionCounts = logDAO.countByActionType();
        List<Map<String, Object>> logItems = new ArrayList<>();

        for (Log log : recentLogs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("logId", log.getLogId().toString());
            item.put("actionType", log.getActionType());
            item.put("description", log.getDescription());
            item.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().toString() : null);
            logItems.add(item);
        }

        List<Map<String, Object>> countItems = new ArrayList<>();
        for (LogActionCount count : actionCounts) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("actionType", count.getActionType());
            item.put("actionCount", count.getActionCount());
            countItems.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recentLogCount", logItems.size());
        data.put("recentLogs", logItems);
        data.put("actionTypeCounts", countItems);
        return data;
    }

    private String extractRequiredString(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value == null || value.toString().trim().isEmpty()) {
            throw new ValidationException("A " + key + " parameter is required for this report type.");
        }
        return value.toString().trim();
    }

    private UUID extractRequiredUuid(Map<String, Object> parameters, String key) {
        String value = extractRequiredString(parameters, key);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(key + " must be a valid UUID.");
        }
    }

    private int extractLimit(Map<String, Object> parameters, int defaultLimit) {
        Object value = parameters.get("limit");
        if (value == null) {
            return defaultLimit;
        }
        try {
            int limit = Integer.parseInt(value.toString().trim());
            if (limit <= 0) {
                throw new ValidationException("limit must be greater than zero.");
            }
            return limit;
        } catch (NumberFormatException e) {
            throw new ValidationException("limit must be a valid integer.");
        }
    }

    private SystemReportResponseDTO mapToResponse(SystemReport systemReport) {
        return SystemReportResponseDTO.builder()
                .reportId(systemReport.getReportId())
                .reportType(systemReport.getReportType())
                .reportTitle(systemReport.getReportTitle())
                .parametersJson(systemReport.getParametersJson())
                .resultJson(systemReport.getResultJson())
                .generatedAt(systemReport.getGeneratedAt())
                .generatedByAdminUserId(systemReport.getGeneratedByAdminUserId())
                .build();
    }

}
