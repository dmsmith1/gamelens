CREATE TABLE teams (
 id UUID PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 season VARCHAR(40) NOT NULL
);
CREATE TABLE players (
 id UUID PRIMARY KEY,
 team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
 first_name VARCHAR(80) NOT NULL,
 last_name VARCHAR(80) NOT NULL,
 jersey_number INTEGER CHECK (jersey_number BETWEEN 0 AND 99),
 primary_position VARCHAR(20) NOT NULL,
 bats VARCHAR(10) NOT NULL,
 throws_hand VARCHAR(10) NOT NULL
);
CREATE INDEX players_team_id_idx ON players(team_id);
