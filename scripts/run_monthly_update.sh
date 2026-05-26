#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$PROJECT_ROOT/logs"
LOG_FILE="$LOG_DIR/data_update.log"

mkdir -p "$LOG_DIR"

{
  echo "============================================================"
  echo "Monthly data update started at $(date)"
  echo "Project root: $PROJECT_ROOT"
  echo

  "$PROJECT_ROOT/scripts/update_data.sh"

  echo
  echo "Monthly data update finished at $(date)"
} >> "$LOG_FILE" 2>&1
