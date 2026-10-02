ALTER TABLE players ADD COLUMN jersey_label VARCHAR(2);
UPDATE players SET jersey_label = CAST(jersey_number AS VARCHAR(2));
ALTER TABLE players DROP COLUMN jersey_number;
ALTER TABLE players RENAME COLUMN jersey_label TO jersey_number;
ALTER TABLE players ALTER COLUMN primary_position DROP NOT NULL;
ALTER TABLE players ADD COLUMN lineup_role VARCHAR(20) NOT NULL DEFAULT 'FIELDING';
ALTER TABLE players ADD CONSTRAINT lineup_role_valid CHECK ((lineup_role = 'EXTRA_HITTER' AND primary_position IS NULL) OR (lineup_role = 'FIELDING' AND primary_position IS NOT NULL));
