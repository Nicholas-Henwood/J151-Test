package com.vzap.trytons.service.catalog;

import com.vzap.trytons.dao.catalog.PositionDAO;
import com.vzap.trytons.dto.catalog.PositionRequestDTO;
import com.vzap.trytons.dto.catalog.PositionResponseDTO;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.catalog.Position;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PositionServiceImpl implements PositionService {
    @Inject
    private PositionDAO positionDAO;

    @Override
    public PositionResponseDTO createPosition(PositionRequestDTO request) {
        validatePositionRequest(request);

        String cleanPositionName = request.getPositionName().trim();

        if (positionDAO.existsByName(cleanPositionName)) {
            throw new ConflictException("A position with the name '" + cleanPositionName + "' already exists.");
        }

        Position position = mapRequestToPosition(request);
        position.setPositionId(UUID.randomUUID());

        boolean created = positionDAO.createPosition(position);

        if (!created) {
            throw new DataAccessException("Failed to create position.", null);
        }

        return mapToResponse(position);
    }

    @Override
    public PositionResponseDTO getPosition(UUID positionId) {
        validatePositionId(positionId);

        Position position = positionDAO.findById(positionId).orElseThrow(() -> new ResourceNotFoundException("Position was not found."));

        return mapToResponse(position);
    }

    @Override
    public List<PositionResponseDTO> getAllPositions() {
        return positionDAO.findAllPositions()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PositionResponseDTO updatePosition(UUID positionId, PositionRequestDTO request) {

        validatePositionId(positionId);
        validatePositionRequest(request);

        Position existingPosition = positionDAO.findById(positionId).orElseThrow(() -> new ResourceNotFoundException("Position was not found."));

        String cleanPositionName = request.getPositionName().trim();

        positionDAO.findByName(cleanPositionName).ifPresent(foundPosition -> {
            if (!foundPosition.getPositionId().equals(existingPosition.getPositionId())) {
                throw new ConflictException("A position with the name '" + cleanPositionName + "' already exists.");
            }
        });

        Position updatedPosition = mapRequestToPosition(request);

        updatedPosition.setPositionId(existingPosition.getPositionId());

        boolean updated = positionDAO.updatePosition(updatedPosition);

        if (!updated) {
            throw new DataAccessException("Failed to update position.", null);
        }
        return mapToResponse(updatedPosition);
    }

    private void validatePositionRequest(PositionRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Position details are required.");
        }

        if (request.getPositionName() == null || request.getPositionName().isBlank()) {
            throw new ValidationException("Position name is required.");
        }

        if (request.getPositionCategory() == null || request.getPositionCategory().isBlank()) {
            throw new ValidationException("Position category is required.");
        }

        if (request.getMinRequired() < 0) {
            throw new ValidationException("Minimum required players cannot be negative.");
        }

        if (request.getMaxAllowed() < 0) {
            throw new ValidationException("Maximum allowed players cannot be negative.");
        }

        if (request.getMinRequired() > request.getMaxAllowed()) {
            throw new ValidationException("Minimum required players cannot be greater than maximum allowed players.");
        }
    }

    private void validatePositionId(UUID positionId) {
        if (positionId == null) {
            throw new ValidationException("Position ID is required.");
        }
    }

    private Position mapRequestToPosition(PositionRequestDTO request) {
        Position position = new Position();

        position.setPositionName(request.getPositionName().trim());
        position.setPositionCategory(request.getPositionCategory().trim());
        position.setMinRequired(request.getMinRequired());
        position.setMaxAllowed(request.getMaxAllowed());

        return position;
    }

    private PositionResponseDTO mapToResponse(Position position) {
        PositionResponseDTO response = new PositionResponseDTO();

        response.setPositionId(position.getPositionId());
        response.setPositionName(position.getPositionName());
        response.setPositionCategory(position.getPositionCategory());
        response.setMinRequired(position.getMinRequired());
        response.setMaxAllowed(position.getMaxAllowed());

        return response;
    }
}