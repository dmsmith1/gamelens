# GameLens architecture

Angular provides the scoring interface. Spring Boot owns teams, players, games, official scoring events, and PostgreSQL persistence. Python/FastAPI will interpret video and produce observations.

AI observations must never silently become official scoring events. A scorer confirms or corrects observations; retain confidence, source footage references, and correction history. Build an append-only scoring event model with reproducible game state and explicit correction events in the scoring milestone.

No video upload, detection model, authentication, or scoring functionality is implemented yet. Before shared or public deployment, add authentication, team authorization, secure secrets, and a consent/retention policy for game footage.

Services communicate through explicit APIs. The frontend proxies /api to Spring Boot and /ai to FastAPI in Docker. Initially keep AI inference outside the official scoring transaction.
