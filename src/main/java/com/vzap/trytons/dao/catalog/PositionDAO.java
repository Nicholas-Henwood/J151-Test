package com.vzap.trytons.dao.catalog;
import com.vzap.trytons.model.catalog.Position;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionDAO {
    Optional<Position> findById(UUID positionId);
    Optional<Position> findByName(String positionName);
    Optional<Position> findByPositionCategory(String positionCategory);
    List<Position> findAllPositions();
    public boolean createPosition(Position position);
    public boolean updatePosition(Position position);
    public boolean existsByName(String positionName);
}
