package com.vzap.trytons.service.results;

import com.vzap.trytons.dto.results.TeamScoreUpdateResultDTO;

import java.util.UUID;

public interface TeamScoreService {
    TeamScoreUpdateResultDTO updateTeamScoresForFixture(String fixtureId);
    TeamScoreUpdateResultDTO recalculateTeamTotals(String teamId, String season);
}