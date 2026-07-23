package com.vzap.trytons.service.scoring;

import com.vzap.trytons.dao.scoring.FantasyPointBreakdownDAO;
import com.vzap.trytons.dao.scoring.ScoringRuleDAO;
import com.vzap.trytons.dto.scoring.FantasyPointBreakdownResponseDTO;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.scoring.FantasyPointBreakdown;
import com.vzap.trytons.model.scoring.ScoringRule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FantasyPointBreakdownServiceImpl implements FantasyPointBreakdownService {
    @Inject
    FantasyPointBreakdownDAO fantasyPointBreakdownDAO;

    @Inject
    ScoringRuleDAO scoringRuleDAO;

    @Override
    public FantasyPointBreakdownResponseDTO getBreakdownById(UUID breakdownId) {
        if (breakdownId == null) {
            throw new ValidationException("Breakdown ID is required.");
        }

        FantasyPointBreakdown breakdown = fantasyPointBreakdownDAO.findById(breakdownId).orElseThrow(() -> new ResourceNotFoundException("Fantasy point breakdown not found."));
        return mapToResponse(breakdown);
    }

    @Override
    public List<FantasyPointBreakdownResponseDTO> listBreakdownsForPoints(UUID pointsId) {
        if (pointsId == null) {
            throw new ValidationException("Points ID is required.");
        }

        List<FantasyPointBreakdownResponseDTO> responses = new ArrayList<>();
        for (FantasyPointBreakdown breakdown : fantasyPointBreakdownDAO.findByPointsId(pointsId)) {
            responses.add(mapToResponse(breakdown));
        }
        return responses;
    }

    private FantasyPointBreakdownResponseDTO mapToResponse(FantasyPointBreakdown breakdown) {
        ScoringRule rule = scoringRuleDAO.findById(breakdown.getRuleId()).orElseThrow(() -> new ResourceNotFoundException("Scoring rule not found."));

        return new FantasyPointBreakdownResponseDTO(
                breakdown.getBreakdownId(),
                breakdown.getPointsId(),
                rule.getEventType(),
                breakdown.getPointsEarned(),
                rule.getDescription()
        );
    }
}
