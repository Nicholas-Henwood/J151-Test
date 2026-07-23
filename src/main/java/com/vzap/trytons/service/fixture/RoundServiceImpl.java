package com.vzap.trytons.service.fixture;

import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dto.fixture.RoundResponseDTO;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.model.fixture.FantasyRound;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RoundServiceImpl implements RoundService {
    @Inject
    private FantasyRoundDAO fantasyRoundDAO;

    @Override
    public List<RoundResponseDTO> listRounds() {
        List<RoundResponseDTO> responses = new ArrayList<>();

        for (FantasyRound round : fantasyRoundDAO.getAllRounds()) {
            responses.add(mapToResponse(round));
        }

        return responses;
    }

    @Override
    public List<RoundResponseDTO> listRoundsByStatus(FantasyRoundStatus status) {
        List<RoundResponseDTO> responses = new ArrayList<>();

        for (FantasyRound round : fantasyRoundDAO.getRoundsByStatus(status)) {
            responses.add(mapToResponse(round));
        }

        return responses;
    }

    @Override
    public Optional<RoundResponseDTO> getCurrentOpenRound() {
        return fantasyRoundDAO.getCurrentOpenRound().map(this::mapToResponse);
    }

    private RoundResponseDTO mapToResponse(FantasyRound round) {
        RoundResponseDTO response = new RoundResponseDTO();
        response.setRoundId(round.getRoundId());
        response.setSeason(round.getSeason());
        response.setRoundNumber(round.getRoundNumber());
        response.setOpenDate(round.getOpenDate());
        response.setLockDeadline(round.getLockDeadline());
        response.setEndDate(round.getEndDate());
        response.setStatus(round.getStatus());

        return response;
    }
}
