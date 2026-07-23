package com.vzap.trytons.service.catalog;

import com.vzap.trytons.dao.catalog.ClubDAO;
import com.vzap.trytons.dto.catalog.ClubRequestDTO;
import com.vzap.trytons.dto.catalog.ClubResponseDTO;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.catalog.Club;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ClubServiceImpl implements ClubService {
    @Inject
    private ClubDAO clubDAO;

    @Override
    public ClubResponseDTO createClub(ClubRequestDTO request) {
        validateClubRequest(request);

        String cleanClubName = request.getClubName().trim();

        clubDAO.findByClubName(cleanClubName).ifPresent(foundClub -> {throw new ConflictException("Club '" + foundClub.getClubName() + "' already exists.");});

        Club club = mapRequestToClub(request);
        club.setClubId(UUID.randomUUID());
        club.setActive(request.isActive());

        Club created = clubDAO.createClub(club).orElseThrow(() -> new DataAccessException("Failed to create club.", null));

        return mapToResponse(created);
    }

    private void validateClubRequest(ClubRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Club details are required.");
        }

        if (request.getClubName() == null || request.getClubName().isBlank()) {
            throw new ValidationException("Club name is required.");
        }
    }

    @Override
    public ClubResponseDTO getClub(UUID clubId) {
        validateClubId(clubId);

        Club club = clubDAO.findByClubId(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club was not found."));

        return mapToResponse(club);
    }

    @Override
    public List<ClubResponseDTO> getAllClubs() {
        return clubDAO.findAllClubs()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ClubResponseDTO updateClub(UUID clubId, ClubRequestDTO request) {
        validateClubId(clubId);
        validateClubRequest(request);
        Club existingClub = clubDAO.findByClubId(clubId).orElseThrow(() -> new ResourceNotFoundException("Club was not found."));

        String cleanClubName = request.getClubName().trim();

        clubDAO.findByClubName(cleanClubName).ifPresent(foundClub -> {
            if (!foundClub.getClubId().equals(existingClub.getClubId())) {
                throw new ConflictException("A club with the name '" + cleanClubName + "' already exists.");
            }
        });

        Club updatedClub = mapRequestToClub(request);
        updatedClub.setClubId(existingClub.getClubId());
        updatedClub.setActive(request.isActive());

        Club updated = clubDAO.updateClub(updatedClub).orElseThrow(() -> new DataAccessException("Failed to update club.", null));

        return mapToResponse(updated);
    }

    private void validateClubId(UUID clubId) {
        if (clubId == null) {
            throw new ValidationException("Club ID is required.");
        }
    }

    private Club mapRequestToClub(ClubRequestDTO request) {
        Club club = new Club();

        club.setClubName(request.getClubName().trim());
        club.setLocation(request.getLocation() != null && !request.getLocation().isBlank() ? request.getLocation().trim() : null);
        club.setHomeVenue(request.getHomeVenue() != null && !request.getHomeVenue().isBlank() ? request.getHomeVenue().trim() : null);

        return club;
    }

    private ClubResponseDTO mapToResponse(Club club) {
        return ClubResponseDTO.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .location(club.getLocation())
                .homeVenue(club.getHomeVenue())
                .isActive(club.isActive())
                .build();
    }
}