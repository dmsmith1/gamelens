package com.gamelens.roster;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="teams")
public class Team {
 @Id public UUID id;
 @Column(nullable=false, length=100) public String name;
 @Column(nullable=false, length=40) public String season;
 protected Team() {}
 Team(UUID id, String name, String season) { this.id=id; this.name=name; this.season=season; }
}
