package com.vzap.trytons.service.history;

import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.leaderboard.LeaderboardDAO;
import com.vzap.trytons.dao.results.MatchResultDAO;
import com.vzap.trytons.dao.results.MatchTeamScoreDAO;
import com.vzap.trytons.dto.history.UserPointsHistoryResponseDTO;
import com.vzap.trytons.dto.history.WeeklyPerformanceResponseDTO;
import com.vzap.trytons.enums.MatchTeamSide;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.leaderboard.Ranking;
import com.vzap.trytons.model.results.MatchResult;
import com.vzap.trytons.model.results.MatchTeamScore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;

@ApplicationScoped
public class UserHistoryServiceImpl implements UserHistoryService {
    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private FixtureDAO fixtureDAO;

    @Inject
    private MatchResultDAO matchResultDAO;

    @Inject
    private MatchTeamScoreDAO matchTeamScoreDAO;

    @Inject
    private LeaderboardDAO leaderboardDAO;

    @Inject
    private FantasyRoundDAO fantasyRoundDAO;

    @Override
    public UserPointsHistoryResponseDTO getUserPointsHistory(UUID actorUserId) {

        validateActorUserId(actorUserId);
        FantasyTeam fantasyTeam = fantasyTeamDAO.getTeamByOwner(actorUserId).orElseThrow(() -> new ResourceNotFoundException("No fantasy team was found for this user."));
        List<WeeklyPerformanceResponseDTO> rounds = buildWeeklyPerformance(fantasyTeam);

        int totals = rounds.stream()
                .mapToInt(WeeklyPerformanceResponseDTO::getPointsScored)
                .sum();

        return UserPointsHistoryResponseDTO.builder()
                .totals(totals)
                .rounds(rounds)
                .ranking(resolveMasterRanking(fantasyTeam))
                .build();
    }

    private Integer resolveMasterRanking(FantasyTeam fantasyTeam) {
        String season = currentSeason();
        if (season == null) {
            return null;
        }
        return leaderboardDAO.getMasterLeaderboard(season)
                .flatMap(master -> leaderboardDAO.getRankingByTeamId(fantasyTeam.getTeamId(), master.getLeaderboardId()))
                .map(Ranking::getCurrentRanking)
                .orElse(null);
    }


    private String currentSeason() {
        Optional<FantasyRound> openRound = fantasyRoundDAO.getCurrentOpenRound();
        if (openRound.isPresent()) {
            return openRound.get().getSeason();
        }

        FantasyRound latest = null;
        for (FantasyRound round : fantasyRoundDAO.getAllRounds()) {
            if (round == null || round.getOpenDate() == null) {
                continue;
            }
            if (latest == null || round.getOpenDate().isAfter(latest.getOpenDate())) {
                latest = round;
            }
        }
        return latest == null ? null : latest.getSeason();
    }

    @Override
    public List<WeeklyPerformanceResponseDTO> getWeeklyPerformance(UUID actorUserId) {
        validateActorUserId(actorUserId);

        FantasyTeam fantasyTeam = fantasyTeamDAO.getTeamByOwner(actorUserId).orElseThrow(() -> new ResourceNotFoundException("No fantasy team was found for this user."));

        return buildWeeklyPerformance(fantasyTeam);
    }


    private List<WeeklyPerformanceResponseDTO> buildWeeklyPerformance(FantasyTeam fantasyTeam) {
        if (fantasyTeam == null || fantasyTeam.getTeamId() == null) {
            throw new ResourceNotFoundException("A valid fantasy team is required.");
        }

        UUID teamId = fantasyTeam.getTeamId();

        List<Fixture> fixtures = fixtureDAO.findByTeamId(teamId);

        List<WeeklyPerformanceResponseDTO> weeklyPerformance = new ArrayList<>();

        for (Fixture fixture : fixtures) {

            Optional<MatchResult> resultOptional = matchResultDAO.findCurrentByFixtureId(fixture.getFixtureId());
            if (resultOptional.isPresent()) {

                MatchResult result = resultOptional.get();
                MatchTeamSide teamSide = resolveTeamSide(fixture, teamId);
                Optional<MatchTeamScore> scoreOptional = matchTeamScoreDAO.findByResultIdAndTeamSide(result.getResultId(), teamSide);
                if (scoreOptional.isPresent()) {

                    MatchTeamScore teamScore = scoreOptional.get();
                    String outcome = resolveOutcome(result, teamSide);
                    WeeklyPerformanceResponseDTO performance = WeeklyPerformanceResponseDTO.builder()
                            .roundId(fixture.getRoundId())
                            .fixtureId(fixture.getFixtureId())
                            .pointsScored(teamScore.getTotalScore())
                            .result(outcome)
                            .build();
                    weeklyPerformance.add(performance);
                }
            }
        }

        return weeklyPerformance;
    }


    private void validateActorUserId(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated user ID is required.");
        }
    }
    private MatchTeamSide resolveTeamSide(Fixture fixture, UUID teamId) {
        if (teamId.equals(fixture.getTeamAId())) {
            return MatchTeamSide.TEAM_A;
        }

        if (teamId.equals(fixture.getTeamBId())) {
            return MatchTeamSide.TEAM_B;
        }
        throw new ResourceNotFoundException("The fantasy team does not belong to this fixture.");
    }

    private String resolveOutcome(MatchResult result, MatchTeamSide teamSide) {
        if (result.isDraw()) {
            return "DRAW";
        }

        MatchTeamSide winnerSide = result.getWinnerSide();
        if (winnerSide == null) {
            throw new ResourceNotFoundException("The match result does not have a winning side.");
        }

        return winnerSide == teamSide ? "WIN" : "LOSS";
    }
}

