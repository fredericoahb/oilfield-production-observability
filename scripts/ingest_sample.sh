#!/usr/bin/env bash
set -euo pipefail

FILE=${1:-data/sample_production.csv}

echo "Ingesting: $FILE"
curl -sS -F "file=@${FILE}" http://localhost:8081/v1/ingestions | jq .
