package com.vzap.trytons.service.catalog;

import com.vzap.trytons.dto.catalog.PositionRequestDTO;
import com.vzap.trytons.dto.catalog.PositionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PositionService {
    PositionResponseDTO createPosition(PositionRequestDTO request);
    PositionResponseDTO getPosition(UUID positionId);
    List<PositionResponseDTO> getAllPositions();
    PositionResponseDTO updatePosition(UUID positionId, PositionRequestDTO request);
}