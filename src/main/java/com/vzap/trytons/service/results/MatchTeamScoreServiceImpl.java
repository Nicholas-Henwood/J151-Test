package com.vzap.trytons.service.results;

import com.vzap.trytons.dao.results.MatchTeamScoreDAO;
import com.vzap.trytons.dto.results.MatchTeamScoreResponseDTO;
import com.vzap.trytons.enums.MatchTeamSide;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.results.MatchTeamScore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class MatchTeamScoreServiceImpl implements MatchTeamScoreService {
    @Inject
    private MatchTeamScoreDAO matchTeamScoreDAO;


    @Override
    public MatchTeamScoreResponseDTO getMatchTeamScoreById(UUID scoreId) {

        if (scoreId == null) {
            throw new ValidationException("Score ID is required.");
        }

        MatchTeamScore score = matchTeamScoreDAO.findById(scoreId)
                .orElseThrow(() -> new ResourceNotFoundException("Match team score was not found."));

        return mapToResponse(score);
    }

    @Override
    public List<MatchTeamScoreResponseDTO> listMatchTeamScoresForResult(UUID resultId) {

        if (resultId == null) {
            throw new ValidationException("Result ID is required.");
        }

        return matchTeamScoreDAO.findByResultId(resultId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MatchTeamScoreResponseDTO getMatchTeamScoreForResultSide(UUID resultId, MatchTeamSide teamSide) {

        if (resultId == null || teamSide == null) {
            throw new ValidationException("Result ID and team side are required.");
        }

        MatchTeamScore score = matchTeamScoreDAO.findByResultIdAndTeamSide(resultId, teamSide).orElseThrow(() -> new ResourceNotFoundException("Match team score was not found for that result and side."));

        return mapToResponse(score);
    }

    private MatchTeamScoreResponseDTO mapToResponse(MatchTeamScore score) {
        return new MatchTeamScoreResponseDTO(
                score.getScoreId(),
                score.getResultId(),
                score.getTeamId(),
                score.getTeamSide(),
                score.getPlayerPoints(),
                score.getCaptainBonus(),
                score.getTransferPenalty(),
                score.getTotalScore(),
                score.getCalculatedAt()
        );
    }
}