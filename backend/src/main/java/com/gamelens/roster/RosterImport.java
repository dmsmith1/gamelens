package com.gamelens.roster;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="roster_imports")
public class RosterImport {
 @Id public UUID id;
 @Column(nullable=false) public UUID teamId;
 @Column(nullable=false,length=64) public String fingerprint;
 @Column(nullable=false) public int importedCount;
 @Column(nullable=false) public int skippedCount;
 protected RosterImport() {}
}
