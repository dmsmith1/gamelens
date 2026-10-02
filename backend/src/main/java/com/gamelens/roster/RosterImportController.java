package com.gamelens.roster;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController
public class RosterImportController {
 private final RosterImportService service;
 public RosterImportController(RosterImportService service) {this.service=service;}
 @PostMapping("/teams/{teamId}/players/import")
 public RosterImportService.ImportResult save(@PathVariable UUID teamId,@Valid @RequestBody RosterImportService.ImportInput input) {return service.save(teamId,input);}
}
