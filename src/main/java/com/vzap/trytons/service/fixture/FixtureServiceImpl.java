package com.vzap.trytons.service.fixture;

import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.league.LeagueDAO;
import com.vzap.trytons.dao.league.LeagueMembershipDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.fixture.FixtureRequestDTO;
import com.vzap.trytons.dto.fixture.FixtureResponseDTO;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.league.League;
import com.vzap.trytons.model.league.LeagueMembership;
import com.vzap.trytons.model.auth.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@ApplicationScoped
public class FixtureServiceImpl implements FixtureService {
    @Inject
    private FixtureDAO fixtureDAO;
    @Inject
    private LeagueDAO leagueDAO;
    @Inject
    private FantasyRoundDAO fantasyRoundDAO;
    @Inject
    private FantasyTeamDAO fantasyTeamDAO;
    @Inject
    private LeagueMembershipDAO leagueMembershipDAO;
    @Inject
    private UserDAO userDAO;

    @Override
    public List<FixtureResponseDTO> listFixtures(FixtureStatus status) {
        List<Fixture>fixtures;

        if(status != null) {
            fixtures = fixtureDAO.findByStatus(status);
        }else{
            fixtures = fixtureDAO.getAllFixtures();
        }
        List<FixtureResponseDTO> responseList = new ArrayList<>();
        for(Fixture fixture : fixtures){
            FixtureResponseDTO response = mapToResponse(fixture);
            responseList.add(response);
        }

        return responseList;
    }

    @Override
    public FixtureResponseDTO getFixture(UUID fixtureId) {
        if(fixtureId == null){
            throw new ValidationException("Fixture ID is required");
        }

        Optional<Fixture> fixture = fixtureDAO.findById(fixtureId);

        if(fixture.isEmpty()){
            throw new ResourceNotFoundException("Fixture not found");
        }

        Fixture fix = fixture.get();

        return mapToResponse(fix);
    }

    @Override
    public FixtureResponseDTO createFixture(UUID actorUserId, FixtureRequestDTO request) {
        requireAdmin(actorUserId);
        validateCreateRequest(request);

        if(request.getTeamAId().equals(request.getTeamBId())){
            throw new ValidationException("A fixture cannot pair a team against itself.");
        }

        Optional<League>leagueOptional = leagueDAO.findLeagueById(request.getLeagueId());

        if(leagueOptional.isEmpty()){
            throw new ResourceNotFoundException("League not found");
        }

        League league = leagueOptional.get();

        Optional<FantasyRound>roundOptional = fantasyRoundDAO.getRoundById(request.getRoundId());

        if(roundOptional.isEmpty()){
            throw new ResourceNotFoundException("Round not found");
        }

        FantasyRound round = roundOptional.get();

        if(round.getStatus() == FantasyRoundStatus.COMPLETED || round.getStatus() == FantasyRoundStatus.CANCELLED){
            throw new ValidationException("Fixtures cannot be scheduled against a " + round.getStatus() + " round.");
        }

        Optional<FantasyTeam> optionalTeamA = fantasyTeamDAO.getTeamById(request.getTeamAId());

        if(optionalTeamA.isEmpty()){
            throw new ResourceNotFoundException("Team A not found");
        }

        FantasyTeam teamA = optionalTeamA.get();
        Optional<FantasyTeam> optionalTeamB = fantasyTeamDAO.getTeamById(request.getTeamBId());

        if(optionalTeamB.isEmpty()){
            throw new ResourceNotFoundException("Team B not found");
        }

        FantasyTeam teamB = optionalTeamB.get();

        assertActiveLeagueMember(league.getLeagueId(), teamA.getTeamId(),"Team A");
        assertActiveLeagueMember(league.getLeagueId(), teamB.getTeamId(),"Team B");
        assertNoDuplicatePairing(round.getRoundId(), teamA.getTeamId(), teamB.getTeamId());

        Fixture fixture = new Fixture();
        fixture.setFixtureId(UUID.randomUUID());
        fixture.setLeagueId(league.getLeagueId());
        fixture.setRoundId(round.getRoundId());
        fixture.setTeamAId(teamA.getTeamId());
        fixture.setTeamBId(teamB.getTeamId());
        fixture.setFixtureDate(request.getFixtureDate());
        fixture.setFixtureTime(request.getFixtureTime());
        fixture.setStatus(FixtureStatus.UPCOMING);
        Fixture createdFixture = fixtureDAO.create(fixture);
        if(createdFixture == null){
            throw new DataAccessException("Failed to create Fixture.",null);
        }

        return mapToResponse(createdFixture, teamA.getTeamName(),  teamB.getTeamName());

    }

    @Override
    public FixtureResponseDTO updateFixtureStatus(UUID actorUserId, UUID fixtureId, FixtureStatus status) {
        requireAdmin(actorUserId);

        if(fixtureId == null){
            throw new ValidationException("Fixture ID is required");
        }

        if(status == null){
            throw new ValidationException("Fixture status is required");
        }

        Optional<Fixture> fixtureOptional = fixtureDAO.findById(fixtureId);

        if(fixtureOptional.isEmpty()){
            throw new ResourceNotFoundException("Fixture not found");
        }

        Fixture fixture = fixtureOptional.get();

        if (!isValidStatusTransition(fixture.getStatus(), status)) {
            throw new BusinessRuleException("Cannot change fixture status.");
        }

        fixture.setStatus(status);

        if (status == FixtureStatus.COMPLETED) {
            fixture.setSimulationDate(LocalDateTime.now());
        }

        boolean updated = fixtureDAO.updateFixture(fixture);

        if (!updated) {
            throw new DataAccessException("Failed to update fixture status.", null);
        }

        return mapToResponse(fixture);

    }

    private boolean isValidStatusTransition(
            FixtureStatus currentStatus,
            FixtureStatus newStatus) {

        if (currentStatus == null || newStatus == null) {
            return false;
        }

        if (currentStatus == FixtureStatus.UPCOMING) {
            return ((newStatus == FixtureStatus.LOCKED) || (newStatus == FixtureStatus.CANCELLED));
        }

        if (currentStatus == FixtureStatus.LOCKED) {
            return ((newStatus == FixtureStatus.SIMULATING) || (newStatus == FixtureStatus.CANCELLED));
        }

        if (currentStatus == FixtureStatus.SIMULATING) {
            return ((newStatus == FixtureStatus.COMPLETED) || (newStatus == FixtureStatus.CANCELLED));
        }

        if (currentStatus == FixtureStatus.COMPLETED) {
            return newStatus == FixtureStatus.PROCESSED;
        }

        return false;
    }

    private void  requireAdmin(UUID actorUserId) {
        if(actorUserId == null){
            throw new ValidationException("An authenticated administrator is required.");
        }

        Optional<User> userOptional = userDAO.getUserById(actorUserId);

        if(userOptional.isEmpty()){
            throw new AuthorisationException("An authenticated administrator is required.");
        }

        User user = userOptional.get();

        if(user.getRole() != UserRole.ADMINISTRATOR){
            throw new AuthorisationException("Only admins can perform this action.");
        }
    }

    private void validateCreateRequest(FixtureRequestDTO request) {
        if(request == null){
            throw new ValidationException("Fixture details are required.");
        }

        if(request.getLeagueId() == null){
            throw new ValidationException("League ID is required.");
        }

        if(request.getRoundId() == null){
            throw new ValidationException("Round ID is required.");
        }

        if(request.getTeamAId() == null){
            throw new ValidationException("Team A is required.");
        }

        if(request.getTeamBId() == null){
            throw new ValidationException("Team B is required.");
        }

        if(request.getFixtureDate() == null){
            throw new ValidationException("Fixture date is required.");
        }

        if(request.getFixtureTime() == null){
            throw new ValidationException("Fixture time is required.");
        }
    }
    private void assertActiveLeagueMember(UUID leagueId,UUID teamId, String teamName){
        List<LeagueMembership> memberships = leagueMembershipDAO.findActiveByLeague(leagueId);
        boolean found = false;

        for(LeagueMembership membership : memberships){
            if(membership.getTeamId().equals(teamId)){
                found = true;
                break;
            }
        }
        if(!found){
            throw new ValidationException(teamName + " is not an active member of this league.");
        }
    }

    private void assertNoDuplicatePairing(UUID roundId, UUID teamAId, UUID teamBId){
        List<Fixture> fixtures = fixtureDAO.findByRoundId(roundId);

        for(Fixture existingFixture : fixtures){
            if(existingFixture.getStatus() == FixtureStatus.CANCELLED){
                continue;
            }
            UUID existingTeamAId = existingFixture.getTeamAId();
            UUID existingTeamBId = existingFixture.getTeamBId();
            if(existingTeamAId.equals(teamBId) || existingTeamBId.equals(teamAId) || existingTeamBId.equals(teamBId) ||existingTeamAId.equals(teamAId)){
                throw new ConflictException("One of the teams already has a fixture in this round.");
            }
        }
    }

    private FixtureResponseDTO mapToResponse(Fixture fixture){
        String teamAName = null;
        String teamBName = null;

        Optional<FantasyTeam> optionalTeamA = fantasyTeamDAO.getTeamById(fixture.getTeamAId());

        if(optionalTeamA.isPresent()){
            teamAName = optionalTeamA.get().getTeamName();
        }

        Optional<FantasyTeam> optionalTeamB = fantasyTeamDAO.getTeamById(fixture.getTeamBId());

        if(optionalTeamB.isPresent()){
            teamBName = optionalTeamB.get().getTeamName();
        }

        return mapToResponse(fixture,teamAName,teamBName);
    }

    private FixtureResponseDTO mapToResponse(Fixture fixture, String teamAName, String teamBName){
        FixtureResponseDTO response = new FixtureResponseDTO();
        response.setFixtureId(fixture.getFixtureId());
        response.setLeagueId(fixture.getLeagueId());
        response.setRoundId(fixture.getRoundId());
        response.setTeamAId(fixture.getTeamAId());
        response.setTeamAName(teamAName);
        response.setTeamBId(fixture.getTeamBId());
        response.setTeamBName(teamBName);
        response.setFixtureDate(fixture.getFixtureDate());
        response.setFixtureTime(fixture.getFixtureTime());
        response.setFixtureStatus(fixture.getStatus());
        response.setSimulationDate(fixture.getSimulationDate());
        response.setCreatedAt(fixture.getCreatedAt());

        return response;
    }
}