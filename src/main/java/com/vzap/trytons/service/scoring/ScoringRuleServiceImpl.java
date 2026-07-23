package com.vzap.trytons.service.scoring;

import com.vzap.trytons.dao.scoring.ScoringRuleDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.scoring.ScoringRuleRequestDTO;
import com.vzap.trytons.dto.scoring.ScoringRuleResponseDTO;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.scoring.ScoringRule;
import com.vzap.trytons.model.auth.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ScoringRuleServiceImpl implements ScoringRuleService {
    @Inject
    ScoringRuleDAO scoringRuleDAO;

    @Inject
    UserDAO userDAO;

    @Override
    public List<ScoringRuleResponseDTO> listRules(UUID actorUserId, String season) {
        requireAdmin(actorUserId);

        List<ScoringRuleResponseDTO> responses = new ArrayList<>();
        for (ScoringRule rule : scoringRuleDAO.findActiveRules(season)) {
            responses.add(mapToResponse(rule));
        }
        return responses;
    }

    @Override
    public ScoringRuleResponseDTO saveRule(UUID actorUserId, ScoringRuleRequestDTO request) {
        requireAdmin(actorUserId);

        if (request.getRuleId() != null) {
            ScoringRule existing = scoringRuleDAO.findById(request.getRuleId()).orElseThrow(() -> new ResourceNotFoundException("Scoring rule not found."));

            existing.setEventType(request.getEventType());
            existing.setPointsAwarded(request.getPointsAwarded());
            existing.setSeason(request.getSeason());
            existing.setIsActive(request.isActive());
            existing.setIsDeduction(request.getIsDeduction());
            existing.setDescription(request.getDescription());

            ScoringRule updated = scoringRuleDAO.update(existing);
            return mapToResponse(updated);
        }

        scoringRuleDAO.findBySeasonAndEventType(request.getSeason(), request.getEventType()).ifPresent(existingRule -> {
            throw new ConflictException("A scoring rule for this season and event type  already exists.");
        });

        ScoringRule newRule = ScoringRule.builder()
                .eventType(request.getEventType())
                .pointsAwarded(request.getPointsAwarded())
                .season(request.getSeason())
                .isActive(request.isActive())
                .isDeduction(request.getIsDeduction())
                .description(request.getDescription())
                .build();

        ScoringRule saved = scoringRuleDAO.save(newRule);
        return mapToResponse(saved);
    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated administrator is required.");
        }
        User actor = userDAO.getUserById(actorUserId)
                .orElseThrow(() -> new AuthorisationException("An authenticated administrator is required."));
        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Only administrators may manage scoring rules.");
        }
    }

    private ScoringRuleResponseDTO mapToResponse(ScoringRule rule) {
        ScoringRuleResponseDTO response = new ScoringRuleResponseDTO();
        response.setRuleId(rule.getRuleId());
        response.setEventType(rule.getEventType());
        response.setPointsAwarded(rule.getPointsAwarded());
        response.setSeason(rule.getSeason());
        response.setActive(Boolean.TRUE.equals(rule.getIsActive()));
        response.setIsDeduction(rule.getIsDeduction());
        response.setDescription(rule.getDescription());
        return response;
    }
}