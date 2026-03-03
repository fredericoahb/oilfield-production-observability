# Architecture

This repository contains two Spring Boot services:

- **ingestion-service**: accepts daily production files (CSV) and persists time-series rows (per well, per day).
- **kpi-service**: computes KPIs & alerts and exposes an API for dashboards.

Shared infrastructure:

- **PostgreSQL**: persistence (production rows + batch metadata).
- **Prometheus**: metrics scraping from Spring Actuator.
- **Grafana**: dashboards (SLI/SLO-ready).
- **Tempo + OpenTelemetry Collector**: distributed traces.

## Data Model (simplified)

- `production_daily(well_id, production_date, oil_bopd, gas_sm3d, water_bopd)`
- `ingestion_batch(batch_id, source_file, status, rows_total, rows_valid, rows_invalid, started_at, finished_at)`

## Trace Taxonomy (what you should see in Tempo)

- `ingestBatch` → `validateRow` → `persistTimeseries`
- `computeKpi`
- `serveApi` (KPI endpoints)

The HTTP server spans (Spring MVC) and DB spans (JDBC) are also present via the OpenTelemetry Java agent.
