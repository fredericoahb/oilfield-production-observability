#!/usr/bin/env bash
set -euo pipefail

echo "==> Checking services..."
curl -sS http://127.0.0.1:8081/actuator/health >/dev/null && echo "Ingestion: OK" || echo "Ingestion: not ready"
curl -sS http://127.0.0.1:8082/actuator/health >/dev/null && echo "KPI: OK" || echo "KPI: not ready"

