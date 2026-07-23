package com.vzap.trytons.service.transfer;

import com.vzap.trytons.dto.transfer.TransferRequestDTO;
import com.vzap.trytons.dto.transfer.TransferResponseDTO;

import java.util.List;

public interface TransferService {
    TransferResponseDTO executeTransfer(String actorUserId, TransferRequestDTO request);
    List<TransferResponseDTO> listTransferHistory(String actorUserId, String teamId);
}