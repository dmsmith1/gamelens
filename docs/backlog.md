# Milestones

1. Foundation: buildable apps, health checks, local PostgreSQL, CI.
2. Teams and players: Flyway schema, validated APIs, roster screens, database integration tests.
3. Games and lineups: schedule, opponent, batting order, substitutions.
4. Manual scoring: pitch counts, outs, innings, runners, score, event replay and corrections.
5. Video proof of concept: one phone, recorded footage, labeled plays, measured accuracy.
6. AI suggestions: observations, confidence, scorer review and corrections.
7. Pilot: authentication, permissions, consent, retention, and operation with a small set of teams.

Next acceptance target: create a team, add/edit/remove roster players, reload the page, and see persisted data. Invalid input must return useful errors; access must eventually be scoped to the team owner.
