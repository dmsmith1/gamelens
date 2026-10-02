package com.gamelens.roster;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import java.util.*;

@Service @Transactional
public class RosterService {
 private final TeamRepository teams;
 private final PlayerRepository players;
 public RosterService(TeamRepository teams, PlayerRepository players) { this.teams=teams; this.players=players; }
 public record TeamInput(@NotBlank @Size(max=100) String name, @NotBlank @Size(max=40) String season) {}
 public record PlayerInput(@NotBlank @Size(max=80) String firstName, @NotBlank @Size(max=80) String lastName,
   @Min(0) @Max(99) Integer jerseyNumber, @NotNull Player.Position primaryPosition,
   @NotNull Player.BattingHand bats, @NotNull Player.ThrowingHand throwsHand) {}
 @Transactional(readOnly=true) public List<Team> teams() { return teams.findAllByOrderByNameAscSeasonAsc(); }
 @Transactional(readOnly=true) public Team team(UUID id) { return teams.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Team not found")); }
 public Team createTeam(TeamInput input) { return teams.save(new Team(UUID.randomUUID(),input.name().strip(),input.season().strip())); }
 public Team updateTeam(UUID id, TeamInput input) { Team t=team(id);t.name=input.name().strip();t.season=input.season().strip();return teams.save(t); }
 @Transactional(readOnly=true) public List<Player> roster(UUID teamId) { team(teamId);return players.findByTeamIdOrderByLastNameAscFirstNameAsc(teamId); }
 public Player savePlayer(UUID teamId, UUID playerId, PlayerInput input) {
   team(teamId);
   Player p=playerId==null ? new Player() : player(teamId,playerId);
   if(playerId==null) { p.id=UUID.randomUUID(); p.teamId=teamId; }
   p.firstName=input.firstName().strip();p.lastName=input.lastName().strip();p.jerseyNumber=input.jerseyNumber();
   p.primaryPosition=input.primaryPosition();p.bats=input.bats();p.throwsHand=input.throwsHand();
   return players.save(p);
 }
 private Player player(UUID teamId, UUID playerId) { return players.findByIdAndTeamId(playerId,teamId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Player not found on this team")); }
 public void removePlayer(UUID teamId, UUID playerId) { team(teamId);players.delete(player(teamId,playerId)); }
}
