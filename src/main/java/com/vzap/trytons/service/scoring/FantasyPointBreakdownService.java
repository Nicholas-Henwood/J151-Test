package com.vzap.trytons.service.scoring;

import com.vzap.trytons.dto.scoring.FantasyPointBreakdownResponseDTO;

import java.util.List;
import java.util.UUID;

public interface FantasyPointBreakdownService {
    FantasyPointBreakdownResponseDTO getBreakdownById(UUID breakdownId);
    List<FantasyPointBreakdownResponseDTO> listBreakdownsForPoints(UUID pointsId);
}