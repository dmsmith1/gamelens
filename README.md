# GameLens

Baseball scoring with a path toward AI-assisted play interpretation from one phone.

This first milestone contains an Angular 22 frontend, Spring Boot 4.1.1 backend (Java 21), Python 3.12/FastAPI service, PostgreSQL, Docker Compose, and build checks. Team and player management is available. Scoring and AI detection are future milestones.

## Run locally with Docker

Install Docker Desktop, then from this directory:

```sh
cp .env.example .env
docker compose up --build
```

Open http://localhost:4200. Backend health: http://localhost:8080/actuator/health. AI health: http://localhost:8000/health. The database persists in a Docker volume. Use `docker compose down` to stop; `docker compose down -v` deletes local database data.

Ports bind to localhost. Example credentials are for local development only. No authentication is implemented; this is not ready for public deployment.

## Develop outside Docker

Requires Node 22.22.3+, Java 21, and Python 3.12. Start PostgreSQL with `docker compose up -d database`.

```sh
cd frontend
npm ci
npm start
```

In another terminal:

```sh
cd backend
./mvnw spring-boot:run
```

In another terminal:

```sh
cd ai
python3 -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

## Verify

- Frontend: `cd frontend && npm ci && npm run build`
- Backend: `cd backend && ./mvnw verify`
- AI: activate the virtual environment, then `cd ai && python -m pytest`

See [architecture](docs/architecture.md) and [milestones](docs/backlog.md).

## Teams and rosters

Create a team with a name and season, then add players with their names, optional jersey number (0–99), position, batting hand, and throwing hand. Edit team details, edit players, or remove a player with confirmation. Rosters persist in PostgreSQL; the browser remembers your selected team.

The browser uses `/api/teams` through its proxy. Direct backend endpoints use `/teams`, with roster routes at `/teams/{teamId}/players`. Team POST and player POST return 201; deletes return 204; invalid entries return 400; missing teams or players return 404. Player operations are restricted to their parent team. These are local development APIs without authentication.

Photo roster import is planned: extract a draft from a printed roster or lineup card, review names and numbers, then save. It is not implemented yet.
