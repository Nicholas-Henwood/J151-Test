package com.vzap.trytons.service.leaderboard;

import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.leaderboard.LeaderboardDAO;
import com.vzap.trytons.dao.league.LeagueDAO;
import com.vzap.trytons.dao.league.LeagueMembershipDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.leaderboard.LeaderboardRefreshResultDTO;
import java.util.UUID;

import com.vzap.trytons.dto.leaderboard.LeaderboardEntryResponseDTO;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.league.League;
import com.vzap.trytons.model.leaderboard.Leaderboard;
import com.vzap.trytons.model.leaderboard.Ranking;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.service.notification.NotificationService;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderboardServiceImpl implements LeaderboardService{

    private static final Logger LOG = Logger.getLogger(LeaderboardServiceImpl.class.getName());

    @Inject
    private LeaderboardDAO leaderboardDAO;
    @Inject
    private LeagueMembershipDAO leagueMembershipDAO;
    @Inject
    private FantasyTeamDAO  fantasyTeamDAO;
    @Inject
    private LeagueDAO leagueDAO;
    @Inject
    private UserDAO userDAO;
    @Inject
    private FantasyRoundDAO fantasyRoundDAO;
    @Inject
    private NotificationService notificationService;

    //New added methods
    //================================================================================================================================================

    @Override
    public LeaderboardRefreshResultDTO refreshLeagueLeaderboard(UUID actorUserId, UUID leagueId) {
        requireLeagueManagerOrAdmin(actorUserId, leagueId);
        Optional<Leaderboard> leaderboard = leaderboardDAO.getLeaderboardByLeagueId(leagueId);
        if (leaderboard.isEmpty()) {
            return LeaderboardRefreshResultDTO.builder()
                    .success(false)
                    .message("No leaderboard exists for this league.")
                    .teamsProcessed(0)
                    .rankingsUpdated(0)
                    .build();
        }
        return refreshRankings(leaderboard.get());
    }

    @Override
    public LeaderboardRefreshResultDTO refreshOverallLeaderboard(UUID actorUserId) {
        requireAdmin(actorUserId);
        String season = currentSeason();
        Optional<Leaderboard> master = leaderboardDAO.getMasterLeaderboard(season);
        if (master.isEmpty()) {
            return LeaderboardRefreshResultDTO.builder()
                    .success(false)
                    .message("No master leaderboard exists for season " + season + ".")
                    .teamsProcessed(0)
                    .rankingsUpdated(0)
                    .build();
        }
        return refreshRankings(master.get());
    }

    @Override
    public List<LeaderboardEntryResponseDTO> getOverallLeaderboard(UUID actorUserId) {
        String season = currentSeason();
        Optional<Leaderboard> master = leaderboardDAO.getMasterLeaderboard(season);
        if (master.isEmpty()) {
            return Collections.emptyList();
        }

        List<LeaderboardEntryResponseDTO> entries = new ArrayList<>();
        for (Ranking ranking : leaderboardDAO.getRankingsByLeaderboardId(master.get().getLeaderboardId())) {
            FantasyTeam team = fantasyTeamDAO.findTeamById(ranking.getTeamId());
            if (team == null) {
                continue;
            }

            String ownerUsername = userDAO.getUserById(team.getOwnerUserId())
                    .map(User::getUsername)
                    .orElse(null);

            entries.add(LeaderboardEntryResponseDTO.builder().teamId(ranking.getTeamId())
                    .teamName(team.getTeamName())
                    .owner(ownerUsername)
                    .rank(ranking.getCurrentRanking())
                    .rankMovement(calculateRankMovement(ranking.getCurrentRanking(), ranking.getPreviousRanking()))
                    .previousRanking(ranking.getPreviousRanking())
                    .matchesPlayed(ranking.getMatchesPlayed())
                    .matchesWon(ranking.getMatchesWon())
                    .matchesDrawn(ranking.getMatchesDrawn())
                    .matchesLost(ranking.getMatchesLost())
                    .pointsFor(ranking.getPointsFor())
                    .pointsAgainst(ranking.getPointsAgainst())
                    .pointsDifference(ranking.getScoreDifference())
                    .leaguePoints(ranking.getLeaguePoints())
                    .totalFantasyPoints(ranking.getTotalFantasyPoints())
                    .build());
        }
        return entries;
    }

    @Override
    public List<LeaderboardEntryResponseDTO> getPublicOverallLeaderboard(int limit) {
        // getOverallLeaderboard does not use the actor id (it reads only the master
        // leaderboard), so a null actor is safe here and keeps this preview user-less.
        List<LeaderboardEntryResponseDTO> all = getOverallLeaderboard(null);
        if (limit > 0 && all.size() > limit) {
            return new ArrayList<>(all.subList(0, limit));
        }
        return all;
    }

    @Override
    public List<LeaderboardEntryResponseDTO> getLeaderboardForLeague(UUID leagueId, UUID requestingUserId) throws AuthorisationException {
        if (!leagueMembershipDAO.existsActiveByLeagueAndUser(leagueId, requestingUserId)){
            throw new AuthorisationException("FORBIDDEN");
        }

        Optional<Leaderboard> leaderboard = leaderboardDAO.getLeaderboardByLeagueId(leagueId);
        if (leaderboard.isEmpty()) {
            return Collections.emptyList();
        }

        List<Ranking> rankingList = leaderboardDAO.getRankingsByLeaderboardId(leaderboard.get().getLeaderboardId());
        List<LeaderboardEntryResponseDTO> leaderboardEntryResponseDTOList = new ArrayList<>();
        for (Ranking ranking : rankingList) {
            FantasyTeam team = fantasyTeamDAO.findTeamById(ranking.getTeamId());
            if (team == null) {
                continue;
            }
            String ownerUsername = userDAO.getUserById(team.getOwnerUserId())
                    .map(User::getUsername)
                    .orElse(null);
            LeaderboardEntryResponseDTO dto = LeaderboardEntryResponseDTO.builder()
                    .teamId(ranking.getTeamId())
                    .teamName(team.getTeamName())
                    .owner(ownerUsername)
                    .rank(ranking.getCurrentRanking())
                    .rankMovement(calculateRankMovement(ranking.getCurrentRanking(), ranking.getPreviousRanking()))
                    .previousRanking(ranking.getPreviousRanking())
                    .matchesPlayed(ranking.getMatchesPlayed())
                    .matchesWon(ranking.getMatchesWon())
                    .matchesDrawn(ranking.getMatchesDrawn())
                    .matchesLost(ranking.getMatchesLost())
                    .pointsFor(ranking.getPointsFor())
                    .pointsAgainst(ranking.getPointsAgainst())
                    .pointsDifference(ranking.getScoreDifference())
                    .leaguePoints(ranking.getLeaguePoints())
                    .totalFantasyPoints(ranking.getTotalFantasyPoints())
                    .build();

            leaderboardEntryResponseDTOList.add(dto);
        }
        return leaderboardEntryResponseDTOList;
    }

    @Override
    public Optional<LeaderboardEntryResponseDTO> getRankingForTeam(UUID teamId, UUID leaderboardId, UUID requestingUserId) throws AuthorisationException {
        if (leaderboardId == null) {
            throw new ValidationException("Leaderboard ID is required.");
        }
        Optional<Leaderboard> l = leaderboardDAO.getLeaderboardById(leaderboardId);
        if (l.isEmpty()) {
            return Optional.empty();
        }
        if (!leagueMembershipDAO.existsActiveByLeagueAndUser(l.get().getLeagueId(), requestingUserId)){
            throw new AuthorisationException("FORBIDDEN");
        }

        Optional<Ranking> r = leaderboardDAO.getRankingByTeamId(teamId, leaderboardId);
        if (r.isPresent()) {
            FantasyTeam team = fantasyTeamDAO.findTeamById(r.get().getTeamId());
            if (team == null) {
                return Optional.empty();
            }
            Ranking ranking = r.get();
            String ownerUsername = userDAO.getUserById(team.getOwnerUserId())
                    .map(User::getUsername)
                    .orElse(null);
            LeaderboardEntryResponseDTO dto = LeaderboardEntryResponseDTO.builder()
                    .teamId(ranking.getTeamId())
                    .teamName(team.getTeamName())
                    .owner(ownerUsername)
                    .rank(ranking.getCurrentRanking())
                    .rankMovement(calculateRankMovement(ranking.getCurrentRanking(), ranking.getPreviousRanking()))
                    .previousRanking(ranking.getPreviousRanking())
                    .matchesPlayed(ranking.getMatchesPlayed())
                    .matchesWon(ranking.getMatchesWon())
                    .matchesDrawn(ranking.getMatchesDrawn())
                    .matchesLost(ranking.getMatchesLost())
                    .pointsFor(ranking.getPointsFor())
                    .pointsAgainst(ranking.getPointsAgainst())
                    .pointsDifference(ranking.getScoreDifference())
                    .leaguePoints(ranking.getLeaguePoints())
                    .totalFantasyPoints(ranking.getTotalFantasyPoints())
                    .build();

            return Optional.of(dto);
        }
        return Optional.empty();
    }

    private LeaderboardRefreshResultDTO refreshRankings(Leaderboard leaderboard) {
        List<Ranking> rankings = leaderboardDAO.getRankingsByLeaderboardId(leaderboard.getLeaderboardId());
        rankings.sort(Comparator.comparingInt(Ranking::getLeaguePoints).reversed()
                .thenComparing(Comparator.comparingInt(Ranking::getScoreDifference).reversed())
                .thenComparing(Comparator.comparingInt(Ranking::getTotalFantasyPoints).reversed()));

        LocalDateTime now = LocalDateTime.now();

        int temporaryBase = rankings.size();
        for (int i = 0; i < rankings.size(); i++) {
            Ranking ranking = rankings.get(i);
            ranking.setPreviousRanking(ranking.getCurrentRanking());
            ranking.setCurrentRanking(temporaryBase + i + 1);
            ranking.setUpdatedAt(now);
            leaderboardDAO.updateRanking(ranking);
        }

        int position = 1;
        for (Ranking ranking : rankings) {
            ranking.setCurrentRanking(position);
            ranking.setUpdatedAt(now);
            leaderboardDAO.updateRanking(ranking);
            position++;
        }

        leaderboard.setLastUpdated(now);
        leaderboardDAO.updateLeaderboard(leaderboard);

        notifyRankChanges(leaderboard, rankings);

        return LeaderboardRefreshResultDTO.builder()
                .success(true)
                .message("Leaderboard refreshed successfully.")
                .teamsProcessed(rankings.size())
                .rankingsUpdated(rankings.size())
                .build();
    }

    private void notifyRankChanges(Leaderboard leaderboard, List<Ranking> rankings) {
        if (leaderboard.getLeagueId() == null) {
            return;
        }
        try {
            for (Ranking ranking : rankings) {
                if (ranking.getTeamId() == null) {
                    continue;
                }
                Integer previousRanking = ranking.getPreviousRanking();
                if (previousRanking != null && previousRanking == ranking.getCurrentRanking()) {
                    continue;
                }
                fantasyTeamDAO.getTeamById(ranking.getTeamId())
                        .map(FantasyTeam::getOwnerUserId)
                        .ifPresent(ownerId -> notificationService.notifyLeaderboardChange(ownerId, leaderboard.getLeagueId(), ranking.getCurrentRanking()));
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send leaderboard-change notifications for leaderboard " + leaderboard.getLeaderboardId(), e);
        }
    }

    private String currentSeason() {
        Optional<FantasyRound> openRound = fantasyRoundDAO.getCurrentOpenRound();
        if (openRound.isPresent()) {
            return openRound.get().getSeason();
        }
        FantasyRound latest = null;
        for (FantasyRound round : fantasyRoundDAO.getAllRounds()) {
            if (latest == null || round.getOpenDate().isAfter(latest.getOpenDate())) {
                latest = round;
            }
        }
        if (latest == null) {
            throw new ResourceNotFoundException("No rounds exist to resolve the current season.");
        }
        return latest.getSeason();
    }

    private void requireLeagueManagerOrAdmin(UUID actorUserId, UUID leagueId) {
        if (actorUserId == null) {
            throw new AuthorisationException("Authentication required.");
        }

        User actor = userDAO.getUserById(actorUserId).orElseThrow(() -> new AuthorisationException("Authentication required."));

        if (actor.getRole() == UserRole.ADMINISTRATOR) {
            return;
        }
        League league = leagueDAO.findLeagueById(leagueId).orElseThrow(() -> new ResourceNotFoundException("League not found."));

        if (!actorUserId.equals(league.getManagerUserId())) {
            throw new AuthorisationException("Only the league manager or an administrator may refresh this leaderboard.");
        }
    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated administrator is required.");
        }

        User actor = userDAO.getUserById(actorUserId).orElseThrow(() -> new AuthorisationException("An authenticated administrator is required."));

        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Only administrators may refresh leaderboards.");
        }
    }

    private Integer calculateRankMovement(int currentRanking, Integer previousRanking) {
        if (previousRanking != null){
            return previousRanking - currentRanking;
        } else return null;
    }
}