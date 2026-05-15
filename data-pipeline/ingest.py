from pathlib import Path

import pandas as pd


RAW_DATA_PATH = Path(__file__).parent / "311.csv"
OUTPUT_DIR = Path(__file__).parent / "processed"
CLEAN_DATA_PATH = OUTPUT_DIR / "clean_311.csv"

SF_LAT_RANGE = (37.0, 38.0)
SF_LNG_RANGE = (-123.0, -122.0)

KEEP_COLUMNS = [
    "service_request_id",
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
    "long",
    "source",
]

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

    cleaned["service_request_id"] = cleaned["service_request_id"].astype(str)

    for column in ["requested_datetime", "closed_date", "updated_datetime"]:
        cleaned[column] = pd.to_datetime(cleaned[column], errors="coerce")

    for column in TEXT_COLUMNS:
        output_column = "status" if column == "status_description" else column
        cleaned[output_column] = (
            cleaned[output_column]
            .fillna("Unknown")
            .astype(str)
            .str.strip()
            .replace("", "Unknown")
        )

    cleaned["supervisor_district"] = pd.to_numeric(
        cleaned["supervisor_district"], errors="coerce"
    ).astype("Int64")
    cleaned["lat"] = pd.to_numeric(cleaned["lat"], errors="coerce")
    cleaned["lng"] = pd.to_numeric(cleaned["lng"], errors="coerce")

    cleaned = cleaned.dropna(subset=["requested_datetime", "lat", "lng"])
    cleaned = cleaned[
        cleaned["lat"].between(*SF_LAT_RANGE) & cleaned["lng"].between(*SF_LNG_RANGE)
    ]
    cleaned = cleaned.drop_duplicates(subset=["service_request_id"])

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


def main() -> None:
    raw_df = load_raw_data(RAW_DATA_PATH)
    cleaned_df = clean_311_data(raw_df)

    OUTPUT_DIR.mkdir(exist_ok=True)
    cleaned_df.to_csv(CLEAN_DATA_PATH, index=False)

    print_quality_report(raw_df, cleaned_df)
    print()
    print(f"Saved cleaned data to: {CLEAN_DATA_PATH}")


if __name__ == "__main__":
    main()
