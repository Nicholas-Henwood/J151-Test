package com.vzap.trytons.service.catalog;

import com.vzap.trytons.dto.catalog.ClubRequestDTO;
import com.vzap.trytons.dto.catalog.ClubResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ClubService {
    ClubResponseDTO createClub(ClubRequestDTO request);
    ClubResponseDTO getClub(UUID clubId);
    List<ClubResponseDTO> getAllClubs();
    ClubResponseDTO updateClub(UUID clubId, ClubRequestDTO request);
}