## Walkthrough (for non–oil & gas readers)

**Story:** “This well started producing much more water than usual.”  
In oil & gas operations, a rising **water cut** (percentage of water in produced fluids) often signals declining efficiency and higher handling cost.

### 1) Start the stack
```bash
docker compose up -d
docker compose ps
```

### 2) Ingest baseline data (normal behavior)
```bash
curl -sS -F "file=@data/demo_baseline.csv" http://127.0.0.1:8081/v1/ingestions
```

### 3) Query wells and latest KPI
```bash
curl -sS http://127.0.0.1:8082/v1/kpis/wells
curl -sS "http://127.0.0.1:8082/v1/kpis/wells/latest?wellId=WELL-ALPHA"
```

### 4) Simulate anomaly (water cut spike) and re-ingest
```bash
curl -sS -F "file=@data/demo_anomaly_high_watercut.csv" http://127.0.0.1:8081/v1/ingestions
curl -sS "http://127.0.0.1:8082/v1/kpis/wells/latest?wellId=WELL-ALPHA"
```

### Suggested screenshots (examples are in `docs/screenshots/`)
- `01-compose-ps.png` – stack up
- `02-ingestion-baseline.png` – baseline ingestion (batchId)
- `03-kpi-baseline.png` – baseline KPI (lower water cut)
- `04-ingestion-anomaly.png` – anomaly ingestion
- `05-kpi-anomaly.png` – anomaly KPI (higher water cut)
- `06-grafana-dashboard-mock.png` – simple KPI chart
- `07-grafana-logs-mock.png` – logs view
