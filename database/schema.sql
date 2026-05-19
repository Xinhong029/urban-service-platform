CREATE TABLE IF NOT EXISTS service_requests (
    service_request_id TEXT PRIMARY KEY,
    requested_datetime TIMESTAMP NOT NULL,
    closed_date TIMESTAMP,
    updated_datetime TIMESTAMP,
    status TEXT,
    agency_responsible TEXT,
    service_name TEXT,
    service_subtype TEXT,
    service_details TEXT,
    address TEXT,
    street TEXT,
    supervisor_district INTEGER,
    analysis_neighborhood TEXT,
    police_district TEXT,
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    source TEXT,
    resolution_hours DOUBLE PRECISION,
    request_year INTEGER,
    request_month INTEGER,
    request_day_of_week TEXT
);

CREATE INDEX IF NOT EXISTS idx_service_requests_requested_datetime
    ON service_requests (requested_datetime);

CREATE INDEX IF NOT EXISTS idx_service_requests_service_name
    ON service_requests (service_name);

CREATE INDEX IF NOT EXISTS idx_service_requests_analysis_neighborhood
    ON service_requests (analysis_neighborhood);

CREATE INDEX IF NOT EXISTS idx_service_requests_status
    ON service_requests (status);
