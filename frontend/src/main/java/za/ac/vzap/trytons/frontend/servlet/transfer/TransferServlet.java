package za.ac.vzap.trytons.frontend.servlet.transfer;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import za.ac.vzap.trytons.frontend.client.fixture.DeadlineStatusResponse;
import za.ac.vzap.trytons.frontend.client.fixture.LockStatusResponse;
import za.ac.vzap.trytons.frontend.client.catalog.ClubRestClient;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerResponse;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerRestClient;
import za.ac.vzap.trytons.frontend.client.catalog.PositionRestClient;
import za.ac.vzap.trytons.frontend.client.fantasyteam.FantasyTeamPlayerSelectionResponse;
import za.ac.vzap.trytons.frontend.client.fantasyteam.FantasyTeamRestClient;
import za.ac.vzap.trytons.frontend.client.fantasyteam.ViewOwnTeamResponse;
import za.ac.vzap.trytons.frontend.client.transfer.TransferRecommendationResponse;
import za.ac.vzap.trytons.frontend.client.transfer.TransferRequest;
import za.ac.vzap.trytons.frontend.client.transfer.TransferRequestValidator;
import za.ac.vzap.trytons.frontend.client.transfer.TransferResponse;
import za.ac.vzap.trytons.frontend.client.transfer.TransferRestClient;
import za.ac.vzap.trytons.frontend.client.round.RoundResponse;
import za.ac.vzap.trytons.frontend.client.round.RoundRestClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;


@WebServlet(name = "TransferServlet", urlPatterns = {"/transfers", "/transfers/history"})
public class TransferServlet extends AbstractServlet {

    @Inject
    private TransferRestClient transferRestClient;

    @Inject
    private PlayerRestClient playerRestClient;

    @Inject
    private ClubRestClient clubRestClient;

    @Inject
    private PositionRestClient positionRestClient;

    @Inject
    private FantasyTeamRestClient fantasyTeamRestClient;

    @Inject
    private RoundRestClient roundRestClient;

    /** Mirrors TransferServiceImpl.FREE_TRANSFERS_PER_ROUND. */
    private static final int FREE_TRANSFERS_PER_ROUND = 1;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAuthenticated(request, response)) return;
        String destination = switch (request.getServletPath()) {
            case "/transfers/history" -> {
                String teamId = getTeamId(request);

                if (teamId == null || teamId.isBlank()) {
                    request.setAttribute("error", "Team id is required to view transfer history");
                    request.setAttribute("history", List.of());
                } else {
                    Optional<List<TransferResponse>> history = transferRestClient.getTransferHistory(teamId);

                    if (history.isPresent()) {
                        request.setAttribute("history", history.get());
                        decorateHistory(request, history.get());
                    } else {
                        request.setAttribute("error", "Unable to load transfer history");
                        request.setAttribute("history", List.of());
                    }
                }

                yield "/pages/transfer-history.jsp";
            }

            case "/transfers" -> {
                loadTransferPage(request);
                yield "/pages/transfers.jsp";
            }

            default -> "/index.jsp";
        };

        request.getRequestDispatcher(destination).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAuthenticated(request, response)) return;
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }

        switch (submit) {
            case "", "transfer", "executeTransfer" -> {
                TransferRequest transferRequest = buildTransferRequest(request);

                if (!TransferRequestValidator.isValid(transferRequest)) {
                    request.setAttribute("error", "Please select a valid player to remove and a different player to add");
                    loadTransferPage(request);
                    request.getRequestDispatcher("/pages/transfers.jsp").forward(request, response);
                    return;
                }

                Optional<TransferResponse> transferResponse = transferRestClient.executeTransfer(transferRequest);

                if (transferResponse.isPresent()) {
                    flashSuccess(request, "Transfer completed");
                    String teamId = getTeamId(request);
                    String roundId = getRoundId(request);
                    String redirectUrl = request.getContextPath() + "/transfers?transferred=1"
                            + (teamId != null ? "&teamId=" + teamId : "")
                            + (roundId != null ? "&roundId=" + roundId : "");
                    response.sendRedirect(redirectUrl);
                    return;
                }

                if (handleApiFailure(request, response, "Transfer could not be completed")) return;
                request.setAttribute("transferError", "The transfer may be blocked by lock status, affordability, or squad rules");
                loadTransferPage(request);
                request.getRequestDispatcher("/pages/transfers.jsp").forward(request, response);
            }

            default -> request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }

    /**
     * The summary stats and date labels the history page needs beyond the raw list.
     * The net value change and penalty/confirmed totals are sums, which JSTL cannot do;
     * transferDate/confirmationDate are LocalDateTime, which fmt:formatDate cannot take.
     */
    private void decorateHistory(HttpServletRequest request, List<TransferResponse> history) {
        java.math.BigDecimal netValue = java.math.BigDecimal.ZERO;
        int penaltyTotal = 0;
        int confirmedCount = 0;
        Map<String, String> dateLabels = new HashMap<>();

        for (TransferResponse transfer : history) {
            if (transfer == null) continue;
            if (transfer.getValueDifference() != null) {
                netValue = netValue.add(transfer.getValueDifference());
            }
            penaltyTotal += transfer.getPenaltyPoints();
            if ("CONFIRMED".equalsIgnoreCase(transfer.getStatus())) {
                confirmedCount++;
            }
            // transferDate, else confirmationDate; the page shows "Not recorded" when neither.
            java.time.LocalDateTime when = transfer.getTransferDate() != null
                    ? transfer.getTransferDate() : transfer.getConfirmationDate();
            if (transfer.getTransferId() != null && when != null) {
                dateLabels.put(transfer.getTransferId().toString(), when.format(HISTORY_DATE));
            }
        }

        request.setAttribute("netValue", netValue);
        request.setAttribute("penaltyTotal", penaltyTotal);
        request.setAttribute("confirmedCount", confirmedCount);
        request.setAttribute("historyDateLabels", dateLabels);
    }

    private static final java.time.format.DateTimeFormatter HISTORY_DATE =
            java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", java.util.Locale.UK);

    private void loadTransferPage(HttpServletRequest request) {
        String roundId = getRoundId(request);

        Optional<List<PlayerResponse>> players = playerRestClient.listPlayers(null, null, null);

        if (players.isPresent()) {
            request.setAttribute("players", players.get());
            request.setAttribute("candidatePlayers", players.get());
        } else {
            request.setAttribute("error", "Unable to load available players");
            request.setAttribute("players", List.of());
            request.setAttribute("candidatePlayers", List.of());
        }

        request.setAttribute("clubNamesById", buildClubNameLookup());
        request.setAttribute("positionNamesById", buildPositionNameLookup());
        request.setAttribute("positionCategoryByName", buildPositionCategoryByName());

        if (roundId != null && !roundId.isBlank()) {
            Optional<LockStatusResponse> lockStatus = transferRestClient.getLockStatus(roundId);

            if (lockStatus.isPresent()) {
                request.setAttribute("lockStatus", lockStatus.get());
            } else {
                request.setAttribute("lockError", "Unable to load round lock status");
            }

            Optional<DeadlineStatusResponse> deadlineStatus = transferRestClient.getDeadlineStatus(roundId);
            deadlineStatus.ifPresent(status -> request.setAttribute("deadlineStatus", status));
        }

        String teamId = getTeamId(request);

        Optional<TransferRecommendationResponse> recommendations = transferRestClient.getTransferRecommendation(teamId, null);

        if (recommendations.isPresent() && recommendations.get().getRecommendations() != null) {
            request.setAttribute("transferRecommendations", recommendations.get().getRecommendations());
        } else {
            request.setAttribute("transferRecommendations", List.of());
        }

        request.setAttribute("teamId", teamId);
        request.setAttribute("roundId", roundId);
        request.setAttribute("freeTransfersLeft", freeTransfersLeft(teamId, roundId));
        currentRound(request);

        loadSquad(request, teamId);
    }

    private void loadSquad(HttpServletRequest request, String teamId) {
        Optional<UUID> parsedTeamId = parseUuid(teamId);
        if (parsedTeamId.isEmpty()) {
            request.setAttribute("squad", List.of());
            return;
        }

        Optional<ViewOwnTeamResponse> team = fantasyTeamRestClient.viewOwnTeam(parsedTeamId.get());
        if (team.isPresent()) {
            ViewOwnTeamResponse ownTeam = team.get();
            List<FantasyTeamPlayerSelectionResponse> squad = ownTeam.getPlayers();
            request.setAttribute("squad", squad != null ? squad : List.of());
            request.setAttribute("remainingBudget", ownTeam.getRemainingBudget());
        } else {
            request.setAttribute("squad", List.of());
            if (!apiCallStatus.isSuccess()) {
                request.setAttribute("squadError", "Unable to load your current squad");
            }
        }
    }

    private Map<UUID, String> buildClubNameLookup() {
        Map<UUID, String> lookup = new HashMap<>();
        clubRestClient.listClubs().ifPresent(clubs -> clubs.forEach(club -> lookup.put(club.getClubId(), club.getClubName())));
        return lookup;
    }

    /**
     * Position category keyed by NAME, not id: the squad rows come back as
     * FantasyTeamPlayerSelectionResponse, which carries positionName only. Drives the
     * forward/back tint on the position pills.
     */
    private Map<String, String> buildPositionCategoryByName() {
        Map<String, String> lookup = new HashMap<>();
        positionRestClient.getAllPositions().ifPresent(positions -> positions.forEach(
                position -> lookup.put(position.getPositionName(), position.getPositionCategory())));
        return lookup;
    }

    private Map<UUID, String> buildPositionNameLookup() {
        Map<UUID, String> lookup = new HashMap<>();
        positionRestClient.getAllPositions().ifPresent(positions -> positions.forEach(position -> lookup.put(position.getPositionId(), position.getPositionName())));
        return lookup;
    }

    private TransferRequest buildTransferRequest(HttpServletRequest request) {
        TransferRequest transferRequest = new TransferRequest();

        parseUuid(getTeamId(request)).ifPresent(transferRequest::setTeamId);
        parseUuid(getRoundId(request)).ifPresent(transferRequest::setRoundId);
        parseUuid(request.getParameter("removedPlayerId")).ifPresent(transferRequest::setRemovedPlayerId);
        parseUuid(request.getParameter("addedPlayerId")).ifPresent(transferRequest::setAddedPlayerId);
        transferRequest.setPenaltyConfirmed(parseCheckbox(request.getParameter("penaltyConfirmed")));

        return transferRequest;
    }

    private String getTeamId(HttpServletRequest request) {
        String teamId = request.getParameter("teamId");

        if (teamId != null && !teamId.isBlank()) {
            return teamId;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object sessionTeamId = session.getAttribute("teamId");
        if (sessionTeamId != null) {
            return sessionTeamId.toString();
        }

        // Nothing writes "teamId" to the session either, so a visit to /transfers without
        // ?teamId= used to show a permanently empty squad. The team is the caller's own.
        return fantasyTeamRestClient.getMyTeam()
                .map(team -> team.getTeamId() == null ? null : team.getTeamId().toString())
                .orElse(null);
    }

    private String getRoundId(HttpServletRequest request) {
        String roundId = request.getParameter("roundId");

        if (roundId != null && !roundId.isBlank()) {
            return roundId;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object sessionRoundId = session.getAttribute("roundId");
        if (sessionRoundId != null) {
            return sessionRoundId.toString();
        }

        // Nothing writes "roundId" to the session, so without this the page never had a
        // round: lock status, the deadline and the free-transfer count were all skipped.
        return currentRound(request).map(RoundResponse::getRoundId).orElse(null);
    }

    /** The open round, fetched once per request and cached on it. */
    private Optional<RoundResponse> currentRound(HttpServletRequest request) {
        Object cached = request.getAttribute("round");
        if (cached instanceof RoundResponse round) {
            return Optional.of(round);
        }
        Optional<RoundResponse> round = roundRestClient.getCurrentOpenRound();
        round.ifPresent(value -> {
            request.setAttribute("round", value);
            // lockDeadline is a LocalDateTime and fmt:formatDate takes java.util.Date,
            // so the display string is built here rather than in the JSP.
            if (value.getLockDeadline() != null) {
                request.setAttribute("lockDeadlineLabel",
                        value.getLockDeadline().format(DEADLINE_FORMAT));
            }
        });
        return round;
    }

    private static final java.time.format.DateTimeFormatter DEADLINE_FORMAT =
            java.time.format.DateTimeFormatter.ofPattern("d MMM, HH:mm", java.util.Locale.UK);

    /**
     * Free transfers left this round.
     *
     * The backend allows FREE_TRANSFERS_PER_ROUND (1) before a
     * PENALTY_POINTS_PER_EXTRA_TRANSFER (-4) penalty applies, and decides that from
     * countConfirmedTransfers(teamId, roundId). Nothing exposes that count, so it is
     * recomputed here from the team's transfer history for the same round — the page
     * needs it to know whether to ask for penalty confirmation.
     */
    private int freeTransfersLeft(String teamId, String roundId) {
        if (teamId == null || teamId.isBlank() || roundId == null || roundId.isBlank()) {
            return FREE_TRANSFERS_PER_ROUND;
        }
        long used = transferRestClient.getTransferHistory(teamId).orElse(List.of()).stream()
                .filter(transfer -> transfer.getRoundId() != null
                        && roundId.equals(transfer.getRoundId().toString())
                        && !"REVERSED".equalsIgnoreCase(String.valueOf(transfer.getStatus())))
                .count();
        return (int) Math.max(0, FREE_TRANSFERS_PER_ROUND - used);
    }

    private boolean parseCheckbox(String value) {
        return "on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    @Override
    public String getServletInfo() {
        return "Transfer Servlet, handles transfer page requests, transfer submissions, and transfer history";
    }
}