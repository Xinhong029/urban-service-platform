# Urban Service Platform

A small production-style urban operations analytics system for city 311 service
request data.

The project demonstrates an end-to-end data application:

```text
Raw 311 CSV -> Python cleaning -> Clean CSV -> PostgreSQL -> Spring Boot API -> React dashboard
```

## Project Goals

- Ingest raw city 311 service request data.
- Clean and validate messy CSV data.
- Store cleaned records in PostgreSQL.
- Expose backend REST APIs with Spring Boot.
- Display operational dashboards with React.
- Prepare the project for Docker-based local development.
- Build toward future risk scoring, simulation, and hotspot prediction.

## Tech Stack

- Python and pandas for data ingestion and cleaning.
- PostgreSQL for relational data storage.
- Spring Boot and Java for backend REST APIs.
- React, TypeScript, and Vite for the frontend dashboard.
- Docker Compose for reproducible full-stack local setup.

## Completed So Far

- Python cleaning script for San Francisco 311 data.
- Data profiling report for time range, service types, neighborhoods, monthly
  volume, average resolution time, and missing or invalid fields.
- PostgreSQL schema and indexes for cleaned service request records.
- CSV import scripts for local PostgreSQL and Docker PostgreSQL.
- Spring Boot health and analytics APIs.
- React dashboard connected to backend APIs.
- Reusable frontend components for bar-list analytics panels.
- Lightweight SVG map preview for latitude and longitude points.
- Simple neighborhood operational risk score API and dashboard panel.
- Targeted neighborhood risk simulation API and dashboard panel.
- Baseline monthly request forecast API and dashboard panel.
- Docker Compose configuration for PostgreSQL, Spring Boot, and React.

## Project Structure

```text
data-pipeline/
  311.csv
  ingest.py
  processed/clean_311.csv

database/
  schema.sql
  import_clean_311.sql
  import_clean_311_docker.sql

backend/
  pom.xml
  src/main/java/com/example/urbanservice/

frontend/
  package.json
  vite.config.ts
  src/
    api/
    components/
    App.tsx
    App.css

docker-compose.yml
.env.example
```

## Data Pipeline

Run the Python cleaning script from the project root:

```bash
python3 data-pipeline/ingest.py
```

The script reads:

```text
data-pipeline/311.csv
```

and writes:

```text
data-pipeline/processed/clean_311.csv
```

The script also prints a profiling report with:

- Data time range
- Top service types
- Top neighborhoods
- Monthly request counts
- Average resolution hours
- Missing or invalid field summary

## Local PostgreSQL Setup

Create the local project database:

```bash
psql -d postgres -c "CREATE DATABASE urban_service"
```

Create the table and indexes:

```bash
psql -d urban_service -f database/schema.sql
```

Import the cleaned CSV:

```bash
psql -d urban_service -f database/import_clean_311.sql
```

Verify the imported row count:

```bash
psql -d urban_service -c "SELECT COUNT(*) FROM service_requests"
```

Expected result:

```text
4881
```

## Docker Setup

Copy the environment template if you want to customize database settings:

```bash
cp .env.example .env
```

Start PostgreSQL, the Spring Boot backend, and the React frontend with Docker Compose:

```bash
docker compose up -d
```

The Docker database uses port `5433` on the host machine to avoid conflicting
with a local PostgreSQL server on port `5432`.

The Docker backend uses port `8080`:

```text
http://localhost:8080
```

The Docker frontend uses port `5173`:

```text
http://localhost:5173
```

Verify the imported data:

```bash
PGPASSWORD=urban_password psql -h localhost -p 5433 -U urban_user -d urban_service -c "SELECT COUNT(*) FROM service_requests;"
```

Verify the backend:

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/health/db
```

Open the frontend:

```text
http://localhost:5173
```

Stop the Docker services:

```bash
docker compose down
```

## Backend

Start the Spring Boot backend:

```bash
cd backend
./mvnw spring-boot:run
```

Run backend tests:

```bash
cd backend
./mvnw test
```

By default, the backend connects to:

```text
jdbc:postgresql://localhost:5432/urban_service
```

The database connection is configured in:

```text
backend/src/main/resources/application.properties
```

## Frontend

Install frontend dependencies:

```bash
cd frontend
npm install
```

Start the React development server:

```bash
cd frontend
npm run dev
```

Open the frontend in the browser:

```text
http://localhost:5173
```

The frontend uses the Vite proxy in `frontend/vite.config.ts` to send `/api`
requests to the Spring Boot backend at:

```text
http://localhost:8080
```

Run frontend checks:

```bash
cd frontend
npm run lint
npm run build
```

## API Endpoints

Health check:

```http
GET /api/health
```

Database health check:

```http
GET /api/health/db
```

Top service types:

```http
GET /api/analytics/top-service-types
```

Top neighborhoods:

```http
GET /api/analytics/top-neighborhoods
```

Monthly request counts:

```http
GET /api/analytics/monthly-request-counts
```

Monthly request forecast:

```http
GET /api/analytics/monthly-forecast
GET /api/analytics/monthly-forecast?periods=6
```

Average resolution time:

```http
GET /api/analytics/average-resolution-time
```

Map points:

```http
GET /api/analytics/map-points
GET /api/analytics/map-points?limit=100
```

The map points endpoint defaults to 500 records and caps the limit at 1000.

Neighborhood risk scores:

```http
GET /api/analytics/neighborhood-risk
```

Neighborhood risk simulation:

```http
GET /api/analytics/neighborhood-risk/simulation
GET /api/analytics/neighborhood-risk/simulation?growthPercent=20
GET /api/analytics/neighborhood-risk/simulation?neighborhood=Bayview%20Hunters%20Point&growthPercent=50
```

## Example API Tests

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/health/db
curl http://localhost:8080/api/analytics/top-service-types
curl http://localhost:8080/api/analytics/top-neighborhoods
curl http://localhost:8080/api/analytics/monthly-request-counts
curl "http://localhost:8080/api/analytics/monthly-forecast?periods=6"
curl http://localhost:8080/api/analytics/average-resolution-time
curl "http://localhost:8080/api/analytics/map-points?limit=5"
curl http://localhost:8080/api/analytics/neighborhood-risk
curl "http://localhost:8080/api/analytics/neighborhood-risk/simulation?growthPercent=20"
curl "http://localhost:8080/api/analytics/neighborhood-risk/simulation?neighborhood=Bayview%20Hunters%20Point&growthPercent=50"
```

## Debugging Notes

If port 8080 is already in use:

```bash
lsof -i :8080
kill <PID>
```

If PostgreSQL says the table does not exist, run:

```bash
psql -d urban_service -f database/schema.sql
```

If the backend cannot connect to PostgreSQL, check:

- PostgreSQL is running.
- Database `urban_service` exists.
- Table `service_requests` exists.
- `backend/src/main/resources/application.properties` has the correct username.

If the frontend loads but data is unavailable in local development, check:

- The Spring Boot backend is running on port `8080`.
- The React app is running on port `5173`.
- `frontend/vite.config.ts` contains the `/api` proxy.
- Browser DevTools Network tab shows whether API requests are failing.

If the Docker frontend loads but data is unavailable, check:

- `docker compose ps` shows the backend as healthy.
- `frontend/nginx.conf` proxies `/api/` to `http://backend:8080/api/`.
- `docker compose logs frontend` and `docker compose logs backend` for errors.

If Docker is not recognized:

```text
zsh: command not found: docker
```

Install and start Docker Desktop, then open a new terminal.

## Next Milestones

- Deploy the backend and frontend to the cloud.
