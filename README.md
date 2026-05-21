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
- Docker Compose for reproducible database setup.

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
- Docker Compose configuration for PostgreSQL.

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

## Docker PostgreSQL Setup

Copy the environment template if you want to customize database settings:

```bash
cp .env.example .env
```

Start PostgreSQL with Docker Compose:

```bash
docker compose up -d
```

The Docker database uses port `5433` on the host machine to avoid conflicting
with a local PostgreSQL server on port `5432`.

Verify the imported data:

```bash
PGPASSWORD=urban_password psql -h localhost -p 5433 -U urban_user -d urban_service -c "SELECT COUNT(*) FROM service_requests;"
```

Stop the Docker database:

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

## Example API Tests

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/health/db
curl http://localhost:8080/api/analytics/top-service-types
curl http://localhost:8080/api/analytics/top-neighborhoods
curl http://localhost:8080/api/analytics/monthly-request-counts
curl http://localhost:8080/api/analytics/average-resolution-time
curl "http://localhost:8080/api/analytics/map-points?limit=5"
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

If the frontend loads but data is unavailable, check:

- The Spring Boot backend is running on port `8080`.
- The React app is running on port `5173`.
- `frontend/vite.config.ts` contains the `/api` proxy.
- Browser DevTools Network tab shows whether API requests are failing.

If Docker is not recognized:

```text
zsh: command not found: docker
```

Install and start Docker Desktop, then open a new terminal.

## Next Milestones

- Dockerize the Spring Boot backend.
- Connect the backend to Docker PostgreSQL through environment variables.
- Add stronger logging and error handling.
- Add operational risk scoring.
- Add simulation or prediction for request growth and hotspot areas.
- Deploy the backend and frontend to the cloud.
