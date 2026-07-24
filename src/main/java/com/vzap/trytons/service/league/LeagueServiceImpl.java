package com.vzap.trytons.service.league;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.league.LeagueDAO;
import com.vzap.trytons.dao.league.LeagueMembershipDAO;
import com.vzap.trytons.dto.league.JoinLeagueRequestDTO;
import com.vzap.trytons.dto.league.JoinLeagueResponseDTO;
import com.vzap.trytons.dto.league.LeagueMemberResponseDTO;
import com.vzap.trytons.dto.league.LeagueRequestDTO;
import com.vzap.trytons.dto.league.LeagueResponseDTO;
import com.vzap.trytons.dto.publicpreview.PublicLeaguePreviewDTO;
import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.league.League;
import com.vzap.trytons.model.league.LeagueMembership;
import com.vzap.trytons.service.notification.NotificationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class LeagueServiceImpl implements LeagueService {
    private static final Logger LOG = Logger.getLogger(LeagueServiceImpl.class.getName());

    @Inject
    private LeagueDAO leagueDAO;

    @Inject
    private LeagueMembershipDAO membershipDAO;

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private NotificationService notificationService;

    @Override
    public LeagueResponseDTO createLeague(LeagueRequestDTO request, UUID currentUserId) {
        validateCreateLeagueRequest(request, currentUserId);

        String leagueName = request.getLeagueName().trim();

        if (leagueDAO.findLeagueByName(leagueName).isPresent()) {
            throw new ConflictException("A league with this name already exists.");
        }

        FantasyTeam team = fantasyTeamDAO.getTeamByOwner(currentUserId).orElseThrow(() -> new BusinessRuleException("You must create a fantasy team before creating a league."));

        League league = new League();
        league.setLeagueId(UUID.randomUUID());
        league.setLeagueName(leagueName);
        league.setDescription(request.getDescription());
        league.setLeagueType(request.getLeagueType());
        league.setMaxMembers(request.getMaxMembers());
        league.setIsActive(true);

        if (request.getLeagueType() == LeagueType.PRIVATE) {
            league.setLeagueCode(generateLeagueCode());
        }

        League savedLeague = leagueDAO.createLeague(league);

        if (savedLeague == null || savedLeague.getLeagueId() == null) {
            throw new BusinessRuleException("The league could not be created.");
        }

        LeagueMembership managerMembership = membershipDAO.createMembership(
                savedLeague.getLeagueId(), currentUserId, team.getTeamId());

        if (managerMembership == null || managerMembership.getMembershipId() == null) {
            throw new BusinessRuleException("The league manager membership could not be created.");
        }

        boolean managerAssigned = leagueDAO.assignManager(savedLeague.getLeagueId(), currentUserId);

        if (!managerAssigned) {
            throw new BusinessRuleException("The league manager could not be assigned.");
        }

        return toResponse(requireLeague(savedLeague.getLeagueId()));
    }

    @Override
    public LeagueResponseDTO getLeague(UUID leagueId, UUID currentUserId) {
        if (leagueId == null) {
            throw new ValidationException("League ID is required.");
        }

        if (currentUserId == null) {
            throw new ValidationException("Current user ID is required.");
        }

        League league = requireLeague(leagueId);

        if (league.getLeagueType() == LeagueType.PRIVATE && !isLeagueMember(leagueId, currentUserId)) {
            throw new AuthorisationException("You are not permitted to view this private league.");
        }

        return toResponse(league);
    }

    @Override
    public boolean isLeagueMember(UUID leagueId, UUID userId) {
        if (leagueId == null || userId == null) {
            return false;
        }

        return membershipDAO.existsActiveByLeagueAndUser(leagueId, userId);
    }

    @Override
    public List<LeagueResponseDTO> getAllLeagues(UUID currentUserId) {
        if (currentUserId == null) {
            throw new ValidationException("Current user ID is required.");
        }

        boolean actorIsAdmin = isAdmin(currentUserId);
        List<League> leagues = leagueDAO.findAllLeagues();
        List<LeagueResponseDTO> responses = new ArrayList<>();

        for (League league : leagues) {
            if (actorIsAdmin || league.getLeagueType() == LeagueType.PUBLIC || isLeagueMember(league.getLeagueId(), currentUserId)) {
                responses.add(toResponse(league));
            }
        }

        return responses;
    }

    @Override
    public List<PublicLeaguePreviewDTO> getPublicLeaguePreviews(int limit) {
        List<PublicLeaguePreviewDTO> previews = new ArrayList<>();
        for (League league : leagueDAO.findAllLeagues()) {
            if (league.getLeagueType() != LeagueType.PUBLIC) {
                continue;
            }
            if (Boolean.FALSE.equals(league.getIsActive())) {
                continue;
            }
            previews.add(PublicLeaguePreviewDTO.builder()
                    .leagueName(league.getLeagueName())
                    .description(league.getDescription())
                    .maxMembers(league.getMaxMembers())
                    .memberCount(membershipDAO.countActiveMembers(league.getLeagueId()))
                    .build());
            if (limit > 0 && previews.size() >= limit) {
                break;
            }
        }
        return previews;
    }

    @Override
    public JoinLeagueResponseDTO joinLeague(JoinLeagueRequestDTO request, UUID currentUserId) {
        if (request == null) {
            throw new ValidationException("Join-league request cannot be null.");
        }
        if (currentUserId == null) {
            throw new ValidationException("Current user ID is required.");
        }

        if (request.getLeagueId() == null
                && request.getLeagueCode() != null && !request.getLeagueCode().isBlank()) {
            leagueDAO.findLeagueByLeagueCode(request.getLeagueCode().trim())
                    .ifPresent(found -> request.setLeagueId(found.getLeagueId()));
        }
      
        if (request.getTeamId() == null) {
            fantasyTeamDAO.getTeamByOwner(currentUserId)
                    .ifPresent(team -> request.setTeamId(team.getTeamId()));
        }

        validateJoinLeagueRequest(request, currentUserId);

        League league = requireLeague(request.getLeagueId());

        FantasyTeam team = fantasyTeamDAO.getTeamById(request.getTeamId()).orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found."));

        if (team.getOwnerUserId() == null || !currentUserId.equals(team.getOwnerUserId())) {
            throw new AuthorisationException("The selected fantasy team does not belong to the current user.");
        }

        if (membershipDAO.existsActiveByLeagueAndUser(league.getLeagueId(), currentUserId)) {
            throw new ConflictException("You are already a member of this league.");
        }

        if (league.getLeagueType() == LeagueType.PRIVATE) {
            validatePrivateLeagueCode(league, request.getLeagueCode());
        }

        if (membershipDAO.countActiveMembers(league.getLeagueId()) >= league.getMaxMembers()) {
            throw new ConflictException("This league is already full.");
        }

        LeagueMembership membership = membershipDAO.createMembership(
                league.getLeagueId(), currentUserId, request.getTeamId());

        if (membership == null || membership.getMembershipId() == null) {
            throw new BusinessRuleException("The league membership could not be created.");
        }

        notifyLeagueJoin(league, currentUserId, team);

        return JoinLeagueResponseDTO.builder()
                .leagueId(league.getLeagueId())
                .leagueName(league.getLeagueName())
                .message("Joined the league successfully.")
                .membershipId(membership.getMembershipId())
                .build();
    }

    @Override
    public List<LeagueMemberResponseDTO> listMembers(String actorUserId, String leagueId) {
        UUID actorId = parseUserId(actorUserId);
        UUID parsedLeagueId = parseLeagueId(leagueId);

        requireLeague(parsedLeagueId);

        if (!isAdmin(actorId) && !isLeagueMember(parsedLeagueId, actorId)) {
            throw new AuthorisationException("You must be a league member to view its members.");
        }

        List<LeagueMembership> memberships = membershipDAO.findActiveByLeague(parsedLeagueId);
        List<LeagueMemberResponseDTO> responses = new ArrayList<>();

        for (LeagueMembership membership : memberships) {
            responses.add(toMemberResponse(membership));
        }

        return responses;
    }

    @Override
    public void removeMember(String actorUserId, String leagueId, String membershipId) {
        UUID actorId = parseUserId(actorUserId);
        UUID parsedLeagueId = parseLeagueId(leagueId);
        UUID parsedMembershipId = parseMembershipId(membershipId);

        League league = requireLeague(parsedLeagueId);
        requireLeagueManager(league, actorId);

        LeagueMembership membership = membershipDAO.findById(parsedMembershipId).orElseThrow(() -> new ResourceNotFoundException("League membership not found."));

        if (!parsedLeagueId.equals(membership.getLeagueId())) {
            throw new ResourceNotFoundException("League membership not found.");
        }

        if (!Boolean.TRUE.equals(membership.getIsActive())) {
            throw new ConflictException("The league membership is not active.");
        }

        if (league.getManagerUserId() != null && league.getManagerUserId().equals(membership.getRegisteredUserId())) {

            throw new BusinessRuleException("The league manager cannot be removed.");
        }

        if (!membershipDAO.deactivateMembership(parsedMembershipId)) {
            throw new BusinessRuleException("The league membership could not be removed.");
        }

        notifyLeagueRemoval(league, membership);
    }

    @Override
    public String getLeagueCode(String actorUserId, String leagueId) {
        UUID actorId = parseUserId(actorUserId);
        UUID parsedLeagueId = parseLeagueId(leagueId);

        League league = requireLeague(parsedLeagueId);
        requireLeagueManager(league, actorId);

        if (league.getLeagueType() != LeagueType.PRIVATE) {
            throw new BusinessRuleException("Public leagues do not have private join codes.");
        }

        if (league.getLeagueCode() == null || league.getLeagueCode().isBlank()) {
            throw new ResourceNotFoundException("The league code was not found.");
        }

        return league.getLeagueCode();
    }

    private void validateCreateLeagueRequest(LeagueRequestDTO request, UUID currentUserId) {
        if (request == null) {
            throw new ValidationException("League request cannot be null.");
        }

        if (currentUserId == null) {
            throw new ValidationException("Current user ID is required.");
        }

        if (request.getLeagueName() == null || request.getLeagueName().isBlank()) {
            throw new ValidationException("League name is required.");
        }

        if (request.getLeagueType() == null) {
            throw new ValidationException("League type is required.");
        }

        if (request.getMaxMembers() <= 1) {
            throw new ValidationException("Maximum members must be greater than 1.");
        }
    }

    private void validateJoinLeagueRequest(JoinLeagueRequestDTO request, UUID currentUserId) {
        if (request == null) {
            throw new ValidationException("Join-league request cannot be null.");
        }

        if (currentUserId == null) {
            throw new ValidationException("Current user ID is required.");
        }

        if (request.getLeagueId() == null) {
            throw new ValidationException("That league could not be found. Check the join code and try again.");
        }

        if (request.getTeamId() == null) {
            throw new ValidationException("You need a fantasy team before you can join a league.");
        }
    }

    private void validatePrivateLeagueCode(League league, String suppliedCode) {
        String actualCode = league.getLeagueCode();

        if (suppliedCode == null || suppliedCode.isBlank() || actualCode == null || !actualCode.equalsIgnoreCase(suppliedCode.trim())) {
            throw new AuthorisationException("The private league code is invalid.");
        }
    }

    private League requireLeague(UUID leagueId) {
        return leagueDAO.findLeagueById(leagueId).orElseThrow(() -> new ResourceNotFoundException("League not found."));
    }

    private void requireLeagueManager(League league, UUID actorUserId) {
        if (league.getManagerUserId() == null || !actorUserId.equals(league.getManagerUserId())) {
            throw new AuthorisationException("Only the league manager may perform this action.");
        }
    }

    private UUID parseUserId(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Current user ID is required.");
        }

        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("Current user ID must be a valid UUID.");
        }
    }

    private UUID parseLeagueId(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("League ID is required.");
        }

        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("League ID must be a valid UUID.");
        }
    }

    private UUID parseMembershipId(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Membership ID is required.");
        }

        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("Membership ID must be a valid UUID.");
        }
    }

    private String generateLeagueCode() {
        String code;

        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        } while (leagueDAO.existsByLeagueCode(code));

        return code;
    }

    private LeagueMemberResponseDTO toMemberResponse(LeagueMembership membership) {

        LeagueMemberResponseDTO response = new LeagueMemberResponseDTO();
        response.setMembershipId(membership.getMembershipId());
        response.setUserId(membership.getRegisteredUserId());
        response.setUserDisplayName(userDAO.getUserById(membership.getRegisteredUserId()).map(User::getUsername).orElse("Unknown User"));
        response.setTeamId(membership.getTeamId());
        response.setTeamDisplayName(fantasyTeamDAO.getTeamById(membership.getTeamId()).map(FantasyTeam::getTeamName).orElse("Unknown Team"));
        response.setJoinDate(membership.getJoinDate());
        response.setIsActive(membership.getIsActive());
        return response;
    }

    private boolean isAdmin(UUID userId) {
        if (userId == null) {
            return false;
        }
        Optional<User> user = userDAO.getUserById(userId);
        return user.isPresent() && user.get().getRole() == UserRole.ADMINISTRATOR;
    }

    private void notifyLeagueJoin(League league, UUID newMemberUserId, FantasyTeam team) {
        try {
            String memberBody = "You joined the league \"" + league.getLeagueName() + "\".";
            notificationService.notifyLeagueMembershipEvent(newMemberUserId, league.getLeagueId(), memberBody);

            if (league.getManagerUserId() != null && !league.getManagerUserId().equals(newMemberUserId)) {
                String teamName = team != null && team.getTeamName() != null ? team.getTeamName() : "A new team";
                String managerBody = teamName + " joined your league \"" + league.getLeagueName() + "\".";
                notificationService.notifyLeagueMembershipEvent(league.getManagerUserId(), league.getLeagueId(), managerBody);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send league-join notifications for league " + league.getLeagueId(), e);
        }
    }

    private void notifyLeagueRemoval(League league, LeagueMembership membership) {
        try {
            UUID removedUserId = membership.getRegisteredUserId();
            if (removedUserId != null) {
                String removedBody = "You have been removed from the league \"" + league.getLeagueName() + "\".";
                notificationService.notifyLeagueMembershipEvent(removedUserId, league.getLeagueId(), removedBody);
            }

            if (league.getManagerUserId() != null && !league.getManagerUserId().equals(removedUserId)) {
                String managerBody = "A member was removed from your league \"" + league.getLeagueName() + "\".";
                notificationService.notifyLeagueMembershipEvent(league.getManagerUserId(), league.getLeagueId(), managerBody);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send league-removal notifications for league " + league.getLeagueId(), e);
        }
    }

    private LeagueResponseDTO toResponse(League league) {

        return LeagueResponseDTO.builder()
                .leagueId(league.getLeagueId())
                .managerUserId(league.getManagerUserId())
                .leagueName(league.getLeagueName())
                .description(league.getDescription())
                .leagueType(league.getLeagueType())
                .creationDate(league.getCreationDate())
                .isActive(league.getIsActive())
                .maxMembers(league.getMaxMembers())
                .leagueCode(league.getLeagueCode())
                .build();
    }
}