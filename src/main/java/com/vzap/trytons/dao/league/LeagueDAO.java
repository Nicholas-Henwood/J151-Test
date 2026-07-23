package com.vzap.trytons.dao.league;
import com.vzap.trytons.model.league.League;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueDAO {
   League createLeague(League league);
    Optional<League> findLeagueById(UUID leagueId);
    Optional<League> findLeagueByName(String leagueName);
    List<League> findAllLeagues();
    List<League> findLeaguesByLeagueManager(UUID manager_user_Id);
    List<League> findLeaguesByManagerName(String username);
    Optional<League> findLeagueByLeagueCode(String leagueCode);
    boolean existsByLeagueCode(String leagueCode);
    boolean deactivateLeague(UUID leagueId);
    boolean updateLeague(League league);
    boolean deleteLeague(UUID leagueId);
    boolean assignManager(UUID leagueId, UUID managerUserId);
}