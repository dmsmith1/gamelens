CREATE TABLE roster_imports (
 id UUID PRIMARY KEY,
 team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
 fingerprint VARCHAR(64) NOT NULL,
 imported_count INTEGER NOT NULL,
 skipped_count INTEGER NOT NULL
);
