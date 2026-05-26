#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

DB_NAME="${DB_NAME:-urban_service}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_USER="${DB_USER:-$(whoami)}"
RAW_DATA_URL="${RAW_DATA_URL:-https://data.sfgov.org/resource/vw6y-z8j6.csv?\$limit=5000}"
RAW_DATA_PATH="${RAW_DATA_PATH:-data-pipeline/311.csv}"

cd "$PROJECT_ROOT"

echo "Step 1/6: Downloading latest raw 311 CSV data"
curl -L "$RAW_DATA_URL" -o "$RAW_DATA_PATH"

echo
echo "Step 2/6: Cleaning raw 311 CSV data"
python3 data-pipeline/ingest.py

echo
echo "Step 3/6: Ensuring database schema exists"
psql \
  -h "$DB_HOST" \
  -p "$DB_PORT" \
  -U "$DB_USER" \
  -d "$DB_NAME" \
  -f database/schema.sql

echo
echo "Step 4/6: Clearing old service request rows"
psql \
  -h "$DB_HOST" \
  -p "$DB_PORT" \
  -U "$DB_USER" \
  -d "$DB_NAME" \
  -c "TRUNCATE TABLE service_requests;"

echo
echo "Step 5/6: Importing cleaned CSV data"
psql \
  -h "$DB_HOST" \
  -p "$DB_PORT" \
  -U "$DB_USER" \
  -d "$DB_NAME" \
  -f database/import_clean_311.sql

echo
echo "Step 6/6: Verifying imported row count"
psql \
  -h "$DB_HOST" \
  -p "$DB_PORT" \
  -U "$DB_USER" \
  -d "$DB_NAME" \
  -c "SELECT COUNT(*) AS service_request_count FROM service_requests;"

echo
echo "Data update complete"
