package com.gamelens.roster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PlayerRepository extends JpaRepository<Player,UUID> {
 List<Player> findByTeamIdOrderByLastNameAscFirstNameAsc(UUID teamId);
 Optional<Player> findByIdAndTeamId(UUID id, UUID teamId);
}
