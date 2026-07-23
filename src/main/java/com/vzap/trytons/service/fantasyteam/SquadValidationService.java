package com.vzap.trytons.service.fantasyteam;

import com.vzap.trytons.dto.fantasyteam.SquadValidationResultDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SquadValidationService {
    SquadValidationResultDTO  validateSquad(List<UUID> proposedPlayerIds);
}
