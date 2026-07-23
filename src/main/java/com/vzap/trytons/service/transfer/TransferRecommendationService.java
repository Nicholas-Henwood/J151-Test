package com.vzap.trytons.service.transfer;

import com.vzap.trytons.dto.transfer.TransferRecommendationRequestDTO;
import com.vzap.trytons.dto.transfer.TransferRecommendationResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TransferRecommendationService {
    TransferRecommendationResponseDTO recommendTransfers(UUID actorUserId, TransferRecommendationRequestDTO request);
}
