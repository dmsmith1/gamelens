package com.gamelens.roster;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.*;
@RestController @RequestMapping("/teams")
public class RosterController {
 private final RosterService service;
 public RosterController(RosterService service) { this.service=service; }
 @GetMapping public List<Team> list() { return service.teams(); }
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Team create(@Valid @RequestBody RosterService.TeamInput input) { return service.createTeam(input); }
 @GetMapping("/{teamId}") public Team team(@PathVariable UUID teamId) { return service.team(teamId); }
 @PutMapping("/{teamId}") public Team update(@PathVariable UUID teamId,@Valid @RequestBody RosterService.TeamInput input) { return service.updateTeam(teamId,input); }
 @GetMapping("/{teamId}/players") public List<Player> roster(@PathVariable UUID teamId) { return service.roster(teamId); }
 @PostMapping("/{teamId}/players") @ResponseStatus(HttpStatus.CREATED) public Player add(@PathVariable UUID teamId,@Valid @RequestBody RosterService.PlayerInput input) { return service.savePlayer(teamId,null,input); }
 @PutMapping("/{teamId}/players/{playerId}") public Player edit(@PathVariable UUID teamId,@PathVariable UUID playerId,@Valid @RequestBody RosterService.PlayerInput input) { return service.savePlayer(teamId,playerId,input); }
 @DeleteMapping("/{teamId}/players/{playerId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void remove(@PathVariable UUID teamId,@PathVariable UUID playerId) { service.removePlayer(teamId,playerId); }
}
