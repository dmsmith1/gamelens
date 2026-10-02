package com.gamelens.roster;
import jakarta.validation.Valid;
import com.gamelens.roster.RosterService.PlayerInput;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.CONFLICT;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class RosterImportService {
 private final RosterService roster;
 private final RosterImportRepository imports;
 public RosterImportService(RosterService roster,RosterImportRepository imports) {this.roster=roster;this.imports=imports;}
 public record ImportInput(@NotNull UUID requestId,@NotEmpty @Size(max=100) List<@NotNull @Valid PlayerInput> players) {}
 public record ImportResult(int importedCount,int skippedCount,boolean repeated) {}
 @Transactional public ImportResult save(UUID teamId,ImportInput input) {
  // Lock the parent team so concurrent imports for the same team cannot add duplicates.
  roster.lockTeam(teamId);
  String fingerprint=fingerprint(input.players());
  var previous=imports.findById(input.requestId());
  if(previous.isPresent()) {
   var p=previous.get();
   if(!p.teamId.equals(teamId)||!p.fingerprint.equals(fingerprint)) throw new ResponseStatusException(CONFLICT,"This import changed after it was saved. Start a new photo import.");
   return new ImportResult(p.importedCount,p.skippedCount,true);
  }
  var keys=new HashSet<String>();
  roster.roster(teamId).forEach(p->keys.add(key(p.firstName,p.lastName,p.jerseyNumber)));
  int added=0, skipped=0;
  for(var p:input.players()) {
   if(!keys.add(key(p.firstName(),p.lastName(),p.jerseyNumber()))) {skipped++;continue;}
   roster.savePlayer(teamId,null,p);added++;
  }
  var record=new RosterImport();record.id=input.requestId();record.teamId=teamId;record.fingerprint=fingerprint;record.importedCount=added;record.skippedCount=skipped;imports.save(record);
  return new ImportResult(added,skipped,false);
 }
 private String key(String first,String last,String jersey) {return first.strip().toLowerCase(Locale.ROOT)+"\u0000"+last.strip().toLowerCase(Locale.ROOT)+"\u0000"+jersey;}
 private String fingerprint(List<RosterService.PlayerInput> players) {
  try {var hash=MessageDigest.getInstance("SHA-256");for(var p:players) {for(Object field:List.of(p.firstName(),p.lastName(),String.valueOf(p.jerseyNumber()),String.valueOf(p.primaryPosition()),p.lineupRole(),p.bats(),p.throwsHand())) {var bytes=field.toString().getBytes(StandardCharsets.UTF_8);hash.update(java.nio.ByteBuffer.allocate(4).putInt(bytes.length).array());hash.update(bytes);}}return HexFormat.of().formatHex(hash.digest());}
  catch(java.security.NoSuchAlgorithmException ex) {throw new IllegalStateException(ex);}
 }
}
