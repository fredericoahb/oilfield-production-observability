#!/usr/bin/env bash
set -euo pipefail

echo "==> Installing base tools..."
sudo apt-get update -y
sudo apt-get install -y make curl python3

echo "==> Starting stack (docker compose up -d)..."
docker compose up -d

echo ""
echo "==> Done. Useful ports:"
echo "  Grafana:    3000 (admin/admin)"
echo "  Prometheus: 9090"
echo "  Ingestion:  8081"
echo "  KPI:        8082"
echo ""
echo "Next: run 'make demo' in the terminal."

