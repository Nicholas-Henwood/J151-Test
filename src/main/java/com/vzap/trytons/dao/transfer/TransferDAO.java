package com.vzap.trytons.dao.transfer;

import com.vzap.trytons.enums.TransferStatus;
import com.vzap.trytons.model.transfer.Transfer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferDAO {

    Optional<Transfer> saveTransfer(Transfer transfer);
    Optional<Transfer> getTransferById(UUID transferId);
    List<Transfer> getTransfersByTeamId(UUID teamId);
    List<Transfer> getTransfersByRound(UUID roundId);
    boolean updateTransferStatus(UUID transferId, TransferStatus transferStatus, LocalDateTime confirmationDate);
    int countConfirmedTransfers(UUID teamId, UUID roundId);
    boolean existsConfirmedTransfer(UUID teamId, UUID roundId, UUID removedPlayerId, UUID addedPlayerId);
}