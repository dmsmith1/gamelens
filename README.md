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

Create a team with a name and season, then add players with their names, optional jersey number (0–99, including 00), position or extra-hitter role, batting hand, and throwing hand. Edit team details, edit players, or remove a player with confirmation. Rosters persist in PostgreSQL; the browser remembers your selected team.

The browser uses `/api/teams` through its proxy. Direct backend endpoints use `/teams`, with roster routes at `/teams/{teamId}/players`. Team POST and player POST return 201; deletes return 204; invalid entries return 400; missing teams or players return 404. Player operations are restricted to their parent team. These are local development APIs without authentication.

## Photo roster import

Select a team, click **Import photo**, then choose a photo or use **Take photo** on a supported phone browser. Click **Read roster photo**, correct the draft, select the rows to include, and click **Import reviewed players**. Clear printed rosters with a jersey number and full name on each line work best. Handwriting is often unreliable with the free local reader; tilted photos and complex tables may also require manual correction. You can add missing rows to the draft. Uncertain rows start unchecked. Numeric fielding positions 1–9 map to Pitcher, Catcher, First base, Second base, Third base, Shortstop, Left field, Center field, and Right field. EH is an extra hitter with no fielding position. Jersey numbers are stored and returned as strings to preserve 00.

JPEG, PNG, and WebP are supported up to 10 MB and 20 megapixels. Export HEIC photos as JPEG first. Photos are processed by local Tesseract OCR in the AI container, discarded after extraction, and never sent to an external AI service. Preview images stay in browser memory until the import closes. Positions default to Utility when unreadable; bats and throws default to Right. Review these before saving.

Bulk save is transactional. Matching name/jersey entries are skipped; retrying the same request does not insert players twice. Import metadata contains a request ID, team ID, payload hash, and counts, without storing the image or OCR text. Start a new import to intentionally change a saved draft.

For development outside Docker, install Tesseract with English language data in addition to Python requirements. OCR tests use a generated printed fixture when Tesseract and the fixture font are installed; CI installs both. `/ai/roster/extract` accepts a multipart `photo`; `/api/teams/{teamId}/players/import` accepts reviewed players and a UUID request ID.

