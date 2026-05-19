# Urban Service Platform

Small production-style urban operations analytics system built with Python,
PostgreSQL, and Spring Boot.

Current data flow:

```text
Raw 311 CSV -> Python cleaning script -> Clean CSV -> PostgreSQL -> Spring Boot REST API
```

## Completed So Far

- Ingest and clean San Francisco 311 service request data.
- Generate a small data profiling report.
- Create a PostgreSQL schema for cleaned service requests.
- Import cleaned CSV data into PostgreSQL.
- Expose Spring Boot health and analytics REST APIs.

## Project Structure

```text
data-pipeline/
  311.csv
  ingest.py
  processed/clean_311.csv

database/
  schema.sql
  import_clean_311.sql

backend/
  pom.xml
  src/main/java/com/example/urbanservice/
```

## Data Pipeline

Run the Python cleaning script from the project root:

```bash
python3 data-pipeline/ingest.py
```

This reads:

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

## PostgreSQL Setup

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

## Backend

Start the Spring Boot backend:

```bash
cd backend
./mvnw spring-boot:run
```

Run tests:

```bash
cd backend
./mvnw test
```

The backend connects to:

```text
jdbc:postgresql://localhost:5432/urban_service
```

The local database username is configured in:

```text
backend/src/main/resources/application.properties
```

## API Endpoints

Health check:

```http
GET /api/health
```

Example response:

```json
{
  "status": "ok"
}
```

Database health check:

```http
GET /api/health/db
```

Example response:

```json
{
  "status": "ok",
  "serviceRequestCount": 4881
}
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

## Next Milestones

- Build a React dashboard.
- Add charts for service type ranking, neighborhood heat, monthly trend, and average resolution time.
- Add map visualization using the map points API.
- Add simple operational risk scoring.
- Add simulation or prediction for request growth and hotspot areas.
