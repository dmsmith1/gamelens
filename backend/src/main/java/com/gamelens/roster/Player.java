package com.gamelens.roster;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="players")
public class Player {
 @Id public UUID id;
 @Column(nullable=false) public UUID teamId;
 @Column(nullable=false, length=80) public String firstName;
 @Column(nullable=false, length=80) public String lastName;
 @Column(length=2) public String jerseyNumber;
 @Column(length=20) @Enumerated(EnumType.STRING) public Position primaryPosition;
 @Column(nullable=false,length=20) @Enumerated(EnumType.STRING) public LineupRole lineupRole = LineupRole.FIELDING;
 @Column(nullable=false, length=10) @Enumerated(EnumType.STRING) public BattingHand bats;
 @Column(name="throws_hand",nullable=false,length=10) @Enumerated(EnumType.STRING) public ThrowingHand throwsHand;
 protected Player() {}
 public enum LineupRole { FIELDING, EXTRA_HITTER }
 public enum Position { P, C, FIRST_BASE, SECOND_BASE, THIRD_BASE, SS, LF, CF, RF, DH, UTILITY }
 public enum BattingHand { RIGHT, LEFT, SWITCH }
 public enum ThrowingHand { RIGHT, LEFT }
}
