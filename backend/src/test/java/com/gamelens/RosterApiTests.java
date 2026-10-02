package com.gamelens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class RosterApiTests {
 @Autowired Environment env;
 RestClient client() { return RestClient.create("http://localhost:"+env.getProperty("local.server.port")); }
 Map<?,?> team(String name) { return client().post().uri("/teams").body(Map.of("name",name,"season","Spring 2027")).retrieve().body(Map.class); }
 Map<String,Object> player(String name) { return new HashMap<>(Map.of("firstName",name,"lastName","Smith","jerseyNumber",12,"primaryPosition","SS","bats","SWITCH","throwsHand","RIGHT")); }
 @Test void createsUpdatesPersistsAndRemovesPlayerWithTeamIsolation() {
  var c=client();var a=team("  Eagles  ");var b=team("Bears");
  assertEquals("Eagles",a.get("name"));
  var updated=c.put().uri("/teams/"+a.get("id")).body(Map.of("name","Eagles Updated","season","Summer 2027")).retrieve().body(Map.class);
  assertEquals("Summer 2027",updated.get("season"));
  assertEquals("Eagles Updated",c.get().uri("/teams/"+a.get("id")).retrieve().body(Map.class).get("name"));
  String path="/teams/"+a.get("id")+"/players";
  var created=c.post().uri(path).body(player("Dan")).retrieve().body(Map.class);
  String id=created.get("id").toString();
  assertEquals(1,c.get().uri(path).retrieve().body(List.class).size());
  var edited=c.put().uri(path+"/"+id).body(player("Daniel")).retrieve().body(Map.class);
  assertEquals("Daniel",edited.get("firstName"));
  assertEquals("Daniel",((Map<?,?>)c.get().uri(path).retrieve().body(List.class).get(0)).get("firstName"));
  var cross=assertThrows(HttpClientErrorException.class,() -> c.delete().uri("/teams/"+b.get("id")+"/players/"+id).retrieve().toBodilessEntity());
  assertEquals(404,cross.getStatusCode().value());
  c.delete().uri(path+"/"+id).retrieve().toBodilessEntity();
  assertTrue(c.get().uri(path).retrieve().body(List.class).isEmpty());
 }
 @Test void validatesRequestsAndHandlesMissingTeam() {
  var c=client();
  var blank=assertThrows(HttpClientErrorException.class,() -> c.post().uri("/teams").body(Map.of("name","  ","season","Spring")).retrieve().toBodilessEntity());
  assertEquals(400,blank.getStatusCode().value());assertTrue(blank.getResponseBodyAsString().contains("name"));
  var t=team("Validation");var input=player("Dan");input.put("jerseyNumber",100);
  var invalid=assertThrows(HttpClientErrorException.class,() -> c.post().uri("/teams/"+t.get("id")+"/players").body(input).retrieve().toBodilessEntity());
  assertEquals(400,invalid.getStatusCode().value());
  input.put("jerseyNumber",2.5);
  var fraction=assertThrows(HttpClientErrorException.class,() -> c.post().uri("/teams/"+t.get("id")+"/players").body(input).retrieve().toBodilessEntity());
  assertEquals(400,fraction.getStatusCode().value());
  input.put("jerseyNumber",null);
  assertNotNull(c.post().uri("/teams/"+t.get("id")+"/players").body(input).retrieve().body(Map.class));
  var missing=assertThrows(HttpClientErrorException.class,() -> c.get().uri("/teams/"+UUID.randomUUID()+"/players").retrieve().toBodilessEntity());
  assertEquals(404,missing.getStatusCode().value());
 }
}
