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
- Monthly data update script for downloading, cleaning, and re-importing fresh
  311 data.
- Cloud deployment preparation for backend CORS and frontend API base URL
  configuration.

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
scripts/
  update_data.sh
  run_monthly_update.sh
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

## Automatic Data Update

Run the update script from the project root:

```bash
./scripts/update_data.sh
```

The script performs the local data refresh workflow:

```text
Download latest raw CSV -> run Python cleaning -> ensure PostgreSQL schema -> truncate old rows -> import clean CSV -> verify row count
```

The default raw data source is the San Francisco 311 open data CSV endpoint:

```text
https://data.sfgov.org/resource/vw6y-z8j6.csv?$limit=5000
```

To use a different source URL, pass `RAW_DATA_URL`:

```bash
RAW_DATA_URL="https://example.com/311.csv" ./scripts/update_data.sh
```

By default, it connects to local PostgreSQL:

```text
Database: urban_service
Host: localhost
Port: 5432
User: current macOS username
```

To refresh the Docker PostgreSQL database instead, pass database environment variables:

```bash
DB_HOST=localhost DB_PORT=5433 DB_USER=urban_user DB_NAME=urban_service PGPASSWORD=urban_password ./scripts/update_data.sh
```

## Monthly Scheduled Data Refresh

The project includes a cron-friendly wrapper script:

```bash
./scripts/run_monthly_update.sh
```

It runs `scripts/update_data.sh` and writes output to:

```text
logs/data_update.log
```

To schedule the update for the first day of every month at 2:00 AM, open your crontab:

```bash
crontab -e
```

Add this line:

```cron
0 2 1 * * /Users/zhengxinhong/project/urban-service-platform/scripts/run_monthly_update.sh
```

Cron format:

```text
minute hour day-of-month month day-of-week command
```

So `0 2 1 * *` means:

```text
Run at 2:00 AM on the 1st day of every month.
```

View update logs:

```bash
tail -f logs/data_update.log
```

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
docker compose up -d --build
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

## Cloud Deployment Preparation

This project is prepared for a split cloud deployment:

```text
Render PostgreSQL -> Render Spring Boot Web Service -> Render Static Site frontend
```

The backend reads database settings from environment variables, so local and
cloud environments can use the same code:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
FRONTEND_ALLOWED_ORIGINS
PORT
```

For the deployed backend, `SPRING_DATASOURCE_URL` should point to the cloud
PostgreSQL database. On Render, prefer the internal database connection details
when the backend and database are in the same Render region.

Example backend environment variables:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<database>
SPRING_DATASOURCE_USERNAME=<database-user>
SPRING_DATASOURCE_PASSWORD=<database-password>
FRONTEND_ALLOWED_ORIGINS=https://<your-frontend>.onrender.com
PORT=10000
```

`PORT` tells the cloud platform which port the Spring Boot server should listen
on. Locally, the backend still defaults to `8080`.

The frontend reads its backend URL from a Vite build-time environment variable:

```text
VITE_API_BASE_URL
```

For local development, leave `VITE_API_BASE_URL` empty. The frontend will call
relative `/api` URLs, and Vite or Nginx will proxy those requests to the backend.

For cloud deployment, set it to the deployed backend URL:

```text
VITE_API_BASE_URL=https://<your-backend>.onrender.com
```

For a Render Static Site, use:

```text
Root Directory: frontend
Build Command: npm install && npm run build
Publish Directory: dist
```

After changing frontend environment variables on a static site, rebuild and
redeploy the frontend because Vite injects those values during the build.

## Manual Cloud Data Refresh

The deployed cloud database can be refreshed manually with the same update
script used for local development:

```bash
./scripts/update_data.sh
```

When refreshing the Render PostgreSQL database, pass the cloud database
connection settings as environment variables:

```text
DB_NAME=<render-database-name>
DB_HOST=<render-database-external-host>
DB_PORT=5432
DB_USER=<render-database-user>
PGPASSWORD=<render-database-password>
RAW_DATA_URL=https://data.sfgov.org/resource/vw6y-z8j6.csv?$limit=5000
```

Example:

```bash
DB_NAME=urban_service \
DB_HOST=<render-database-external-host> \
DB_PORT=5432 \
DB_USER=<render-database-user> \
PGPASSWORD=<render-database-password> \
./scripts/update_data.sh
```

This command downloads the latest raw 311 CSV, runs the Python cleaning script,
recreates the schema if needed, truncates old rows, imports the clean CSV, and
prints the final row count.

The project intentionally uses manual cloud refresh for now to avoid recurring
cron job costs. A successful update ends with:

```text
Data update complete
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
