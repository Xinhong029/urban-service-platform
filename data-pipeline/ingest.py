from pathlib import Path

import pandas as pd


RAW_DATA_PATH = Path(__file__).parent / "311.csv" 
OUTPUT_DIR = Path(__file__).parent / "processed" 
CLEAN_DATA_PATH = OUTPUT_DIR / "clean_311.csv"

# Define reasonable lat/lng bounds for San Francisco
SF_LAT_RANGE = (37.0, 38.0) # latitude
SF_LNG_RANGE = (-123.0, -122.0) # longitude

KEEP_COLUMNS = [
    "service_request_id", # unique identifier for each service request
    "requested_datetime",
    "closed_date",
    "updated_datetime",
    "status_description",
    "agency_responsible",
    "service_name",
    "service_subtype",
    "service_details",
    "address",
    "street",
    "supervisor_district",
    "analysis_neighborhood",
    "police_district",
    "lat",
    "long", # will be renamed to "lng" for consistency
    "source",
]

# For text columns, we will fill missing values with "Unknown", strip whitespace, and convert to string type
TEXT_COLUMNS = [
    "status_description",
    "agency_responsible",
    "service_name",
    "service_subtype",
    "service_details",
    "address",
    "street",
    "analysis_neighborhood",
    "police_district",
    "source",
]


def load_raw_data(path: Path) -> pd.DataFrame:
    """Load raw 311 data from CSV."""
    return pd.read_csv(path, low_memory=False)


def clean_311_data(df: pd.DataFrame) -> pd.DataFrame:
    """Clean raw 311 service request data for analytics and database loading."""
    cleaned = df[KEEP_COLUMNS].copy()

    cleaned = cleaned.rename(
        columns={
            "long": "lng",
            "status_description": "status",
        }
    )
 
    # Ensure service_request_id is a string to prevent issues with leading zeros or scientific notation
    cleaned["service_request_id"] = cleaned["service_request_id"].astype(str)

    for column in ["requested_datetime", "closed_date", "updated_datetime"]:
        cleaned[column] = pd.to_datetime(cleaned[column], errors="coerce")

    for column in TEXT_COLUMNS:
        output_column = "status" if column == "status_description" else column
        cleaned[output_column] = (
            cleaned[output_column]
            .fillna("Unknown") # Fill missing values with "Unknown"
            .astype(str)
            .str.strip()
            .replace("", "Unknown")
        )

    cleaned["supervisor_district"] = pd.to_numeric(
        cleaned["supervisor_district"], errors="coerce"
    ).astype("Int64")
    cleaned["lat"] = pd.to_numeric(cleaned["lat"], errors="coerce")
    cleaned["lng"] = pd.to_numeric(cleaned["lng"], errors="coerce")

    # Drop rows with missing critical values
    cleaned = cleaned.dropna(subset=["requested_datetime", "lat", "lng"])
    # Filter out records with lat/lng outside of SF bounds
    cleaned = cleaned[
        cleaned["lat"].between(*SF_LAT_RANGE) & cleaned["lng"].between(*SF_LNG_RANGE)
    ]
    # Drop duplicates
    cleaned = cleaned.drop_duplicates(subset=["service_request_id"])
    # Calculate resolution time in hours, and set negative values to NA (indicating data issues)
    cleaned["resolution_hours"] = (
        cleaned["closed_date"] - cleaned["requested_datetime"]
    ).dt.total_seconds() / 3600
    cleaned.loc[cleaned["resolution_hours"] < 0, "resolution_hours"] = pd.NA

    cleaned["request_year"] = cleaned["requested_datetime"].dt.year
    cleaned["request_month"] = cleaned["requested_datetime"].dt.month
    cleaned["request_day_of_week"] = cleaned["requested_datetime"].dt.day_name()

    return cleaned.sort_values("requested_datetime").reset_index(drop=True)


def print_quality_report(raw_df: pd.DataFrame, cleaned_df: pd.DataFrame) -> None:
    """Print a small data quality report for quick local debugging."""
    print("311 data cleaning complete")
    print(f"Raw rows: {len(raw_df):,}")
    print(f"Clean rows: {len(cleaned_df):,}")
    print(f"Removed rows: {len(raw_df) - len(cleaned_df):,}")
    print()
    print("Top service types:")
    print(cleaned_df["service_name"].value_counts().head(10))
    print()
    print("Missing values after cleaning:")
    print(cleaned_df.isna().sum()[lambda values: values > 0])


def print_profiling_report(cleaned_df: pd.DataFrame) -> None:
    """Print metrics that validate whether the data supports dashboard features."""
    print()
    print("Data profiling report")
    print("---------------------")

    print("Data time range:")
    print(f"Start: {cleaned_df['requested_datetime'].min()}")
    print(f"End:   {cleaned_df['requested_datetime'].max()}")

    print()
    print("Top service types:")
    print(cleaned_df["service_name"].value_counts().head(10))

    print()
    print("Top neighborhoods:")
    print(cleaned_df["analysis_neighborhood"].value_counts().head(10))

    print()
    print("Monthly request counts:")
    monthly_counts = (
        cleaned_df.set_index("requested_datetime")
        .resample("ME")
        .size()
        .rename("request_count")
    )
    print(monthly_counts.tail(12))

    print()
    print("Average resolution hours:")
    print(round(cleaned_df["resolution_hours"].mean(), 2))

    print()
    print("Invalid/missing field summary:")
    print(f"Missing requested_datetime: {cleaned_df['requested_datetime'].isna().sum():,}")
    print(f"Missing lat: {cleaned_df['lat'].isna().sum():,}")
    print(f"Missing lng: {cleaned_df['lng'].isna().sum():,}")
    print(f"Missing resolution_hours: {cleaned_df['resolution_hours'].isna().sum():,}")
    print(f"Negative resolution_hours: {(cleaned_df['resolution_hours'] < 0).sum():,}")
    print(f"Duplicate service_request_id: {cleaned_df['service_request_id'].duplicated().sum():,}")


def main() -> None:
    raw_df = load_raw_data(RAW_DATA_PATH)
    cleaned_df = clean_311_data(raw_df)

    OUTPUT_DIR.mkdir(exist_ok=True)
    cleaned_df.to_csv(CLEAN_DATA_PATH, index=False)

    print_quality_report(raw_df, cleaned_df)
    print_profiling_report(cleaned_df)
    print()
    print(f"Saved cleaned data to: {CLEAN_DATA_PATH}")


if __name__ == "__main__":
    main()
