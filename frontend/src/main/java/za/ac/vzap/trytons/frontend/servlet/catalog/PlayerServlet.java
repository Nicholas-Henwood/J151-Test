package za.ac.vzap.trytons.frontend.servlet.catalog;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.catalog.ClubResponse;
import za.ac.vzap.trytons.frontend.client.catalog.ClubRestClient;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerAvailabilityRequest;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerAvailabilityResponse;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerRequest;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerResponse;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerRestClient;
import za.ac.vzap.trytons.frontend.client.catalog.PositionResponse;
import za.ac.vzap.trytons.frontend.client.catalog.PositionRestClient;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

@WebServlet(name ="PlayerServlet", urlPatterns = {"/players", "/player", "/player/create" , "/player/update", "/player/availability"} )
public class PlayerServlet extends AbstractServlet {
    @Inject
    private PlayerRestClient playerRestClient;
    @Inject
    private ClubRestClient clubRestClient;
    @Inject
    private PositionRestClient positionRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        boolean playersList = "players".equals(submit) || (submit.isEmpty() && "/players".equals(servletPath));
        boolean playerDetail = "player".equals(submit) || (submit.isEmpty() && "/player".equals(servletPath));

        String destination;
        if (playersList) {
            destination = renderPlayersList(request);
        } else if (playerDetail) {
            destination = renderPlayerDetail(request);
        } else {
            destination = "/index.jsp";
        }
        request.getRequestDispatcher(destination).forward(request, response);
    }

    private String renderPlayersList(HttpServletRequest request) {
        String search = request.getParameter("search");
        UUID clubId = parseUuid(request.getParameter("clubId")).orElse(null);
        UUID positionId = parseUuid(request.getParameter("positionId")).orElse(null);
        Optional<List<PlayerResponse>> players = playerRestClient.listPlayers(search, clubId, positionId);
        if (players.isPresent()) {
            request.setAttribute("players", players.get());
        } else {
            request.setAttribute("error", "Unable to load players");
            request.setAttribute("players", List.of());
        }

        Optional<List<ClubResponse>> clubs = clubRestClient.listClubs();
        request.setAttribute("clubs", clubs.orElse(List.of()));

        Optional<List<PositionResponse>> positions = positionRestClient.getAllPositions();
        request.setAttribute("positions", positions.orElse(List.of()));

        request.setAttribute("clubNamesById", buildClubNameLookup());
        request.setAttribute("positionNamesById", buildPositionNameLookup());

        request.setAttribute("searchTerm", search);
        request.setAttribute("selectedClubId", clubId);
        request.setAttribute("selectedPositionId", positionId);

        return "/pages/players.jsp";
    }

    private String renderPlayerDetail(HttpServletRequest request) {
        Optional<UUID> playerId = parseUuid(request.getParameter("playerId"));
        if (playerId.isEmpty()) {
            request.setAttribute("error", "Invalid or missing player id");
            return renderPlayersList(request);
        }
        Optional<PlayerResponse> player = playerRestClient.getPlayer(playerId.get());
        if (player.isPresent()) {
            request.setAttribute("player", player.get());
            request.setAttribute("clubNamesById", buildClubNameLookup());
            request.setAttribute("positionNamesById", buildPositionNameLookup());
            // Drives the forward/back tint on the position pill; PlayerResponse carries
            // only a position id.
            request.setAttribute("positionCategoriesById", buildPositionCategoryLookup());
            // Overall rating = mean of the six abilities, for the hero ring. Presentational,
            // computed here so the JSP does not have to round a six-term average.
            PlayerResponse p = player.get();
            int overall = Math.round((p.getAttackingAbility() + p.getDefensiveAbility() + p.getKickingAbility()
                    + p.getDiscipline() + p.getConsistency() + p.getFitness()) / 6f);
            request.setAttribute("overallRating", overall);
            return "/pages/player.jsp";
        }
        request.setAttribute("error", "Player not found");
        return renderPlayersList(request);
    }

    private Map<UUID, String> buildClubNameLookup() {
        Map<UUID, String> lookup = new HashMap<>();
        clubRestClient.listClubs().ifPresent(clubs -> clubs.forEach(club -> lookup.put(club.getClubId(), club.getClubName())));
        return lookup;
    }

    private Map<UUID, String> buildPositionNameLookup() {
        Map<UUID, String> lookup = new HashMap<>();
        positionRestClient.getAllPositions().ifPresent(positions -> positions.forEach(position -> lookup.put(position.getPositionId(), position.getPositionName())));
        return lookup;
    }

    private Map<UUID, String> buildPositionCategoryLookup() {
        Map<UUID, String> lookup = new HashMap<>();
        positionRestClient.getAllPositions().ifPresent(positions -> positions.forEach(position -> lookup.put(position.getPositionId(), position.getPositionCategory())));
        return lookup;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAdmin(request, response)) return;
        String servletPath = request.getServletPath();
        String submit = request.getParameter("submit");
        if (submit == null){
            submit = "";
        }
        String destination;
        if ("player/create".equals(submit) || "/player/create".equals(servletPath)) {
            destination = handleCreatePlayer(request);
        } else if ("player/update".equals(submit) || "/player/update".equals(servletPath)) {
            destination = handleUpdatePlayer(request);
        } else if ("player/availability".equals(submit) || "/player/availability".equals(servletPath)) {
            destination = handleSetAvailability(request);
        } else {
            destination = "/index.jsp";
        }
        request.getRequestDispatcher(destination).forward(request, response);
    }

    private String handleCreatePlayer(HttpServletRequest request) {
        PlayerRequest playerRequest = buildPlayerRequest(request);
        Optional<PlayerResponse> created = playerRestClient.createPlayer(playerRequest);
        if(created.isPresent()){
            request.setAttribute("player", created.get());
            return "/pages/player.jsp";
        }
        request.setAttribute("error", "Unable to create player");
        return "/pages/player.jsp";
    }

    private String handleUpdatePlayer(HttpServletRequest request) {
        Optional<UUID> playerId = parseUuid(request.getParameter("playerId"));
        if (playerId.isEmpty()) {
            request.setAttribute("error", "Invalid or missing player id");
            return "/pages/player.jsp";
        }
        PlayerRequest playerRequest = buildPlayerRequest(request);
        Optional<PlayerResponse> updated = playerRestClient.updatePlayer(playerId.get(), playerRequest);
        if(updated.isPresent()){
            request.setAttribute("player", updated.get());
            return "/pages/player.jsp";
        }
        request.setAttribute("error", "Unable to update player");
        return "/pages/player.jsp";
    }

    private String handleSetAvailability(HttpServletRequest request) {
        Optional<UUID> playerId = parseUuid(request.getParameter("playerId"));
        if (playerId.isEmpty()) {
            request.setAttribute("error", "Invalid or missing player id");
            return "/pages/player.jsp";
        }
        PlayerAvailabilityRequest availabilityRequest = buildAvailabilityRequest(request);
        Optional<PlayerAvailabilityResponse> saved = playerRestClient.setAvailability(playerId.get(), availabilityRequest);

        Optional<PlayerResponse> player = playerRestClient.getPlayer(playerId.get());
        player.ifPresent(value -> request.setAttribute("player", value));

        if (saved.isPresent()) {
            request.setAttribute("availability", saved.get());
            request.setAttribute("availabilityMessage", "Availability updated.");
        } else {
            request.setAttribute("error", "Unable to update player availability");
        }
        return "/pages/player.jsp";
    }

    private PlayerAvailabilityRequest buildAvailabilityRequest(HttpServletRequest request) {
        PlayerAvailabilityRequest availabilityRequest = new PlayerAvailabilityRequest();
        availabilityRequest.setStatus(request.getParameter("status"));
        availabilityRequest.setEffectiveDate(parseDate(request.getParameter("effectiveDate")));
        availabilityRequest.setEndDate(parseDate(request.getParameter("endDate")));
        availabilityRequest.setNotes(request.getParameter("notes"));
        return availabilityRequest;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private PlayerRequest buildPlayerRequest(HttpServletRequest request) {
        PlayerRequest playerRequest = new PlayerRequest();
        playerRequest.setPlayerName(request.getParameter("playerName"));
        playerRequest.setValue(parseDecimal(request.getParameter("value")));
        playerRequest.setAttackingAbility(parseInt(request.getParameter("attackingAbility")));
        playerRequest.setDefensiveAbility(parseInt(request.getParameter("defensiveAbility")));
        playerRequest.setKickingAbility(parseInt(request.getParameter("kickingAbility")));
        playerRequest.setDiscipline(parseInt(request.getParameter("discipline")));
        playerRequest.setConsistency(parseInt(request.getParameter("consistency")));
        playerRequest.setFitness(parseInt(request.getParameter("fitness")));
        playerRequest.setCurrentForm(parseInt(request.getParameter("currentForm")));

        parseUuid(request.getParameter("clubId")).ifPresent(playerRequest::setClubId);
        parseUuid(request.getParameter("positionId")).ifPresent(playerRequest::setPositionId);
        return playerRequest;
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

}
