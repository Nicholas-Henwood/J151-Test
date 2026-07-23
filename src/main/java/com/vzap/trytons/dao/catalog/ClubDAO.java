package com.vzap.trytons.dao.catalog;

import com.vzap.trytons.model.catalog.Club;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@SuppressWarnings("ALL")
public interface ClubDAO {
    Optional<Club> findByClubId(UUID clubId);
    Optional<Club> findByClubName(String clubName);
    List<Club> findAllClubs();
    List<Club> findByLocation(String location);
    public Optional<Club> createClub(Club club);
    public Optional<Club> updateClub(Club club);
    public Optional<Club> updateStatus(UUID clubId, boolean isActive);
}
