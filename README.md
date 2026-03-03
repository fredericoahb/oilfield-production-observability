# Oilfield Production Observability

![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3-C71A36?style=flat-square&logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![OpenTelemetry](https://img.shields.io/badge/OpenTelemetry-Enabled-000000?style=flat-square&logo=opentelemetry&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-Metrics-E6522C?style=flat-square&logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-Dashboards-F46800?style=flat-square&logo=grafana&logoColor=white)
![Loki](https://img.shields.io/badge/Loki-Logs-F46800?style=flat-square&logo=grafana&logoColor=white)
![Tempo](https://img.shields.io/badge/Tempo-Traces-F46800?style=flat-square&logo=grafana&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/fredericoahb/oilfield-production-observability)

## Try it in the cloud (Codespaces)

1) Click **Open in GitHub Codespaces**
2) In the terminal, run:
```bash
make demo
```
3) Open the forwarded ports:
- KPI API (8082)
- Grafana (3000)


End-to-end **production telemetry observability** for oil & gas wells: ingest daily production data (oil/gas/water), compute KPIs (e.g., water cut), expose APIs, and ship **logs + metrics + traces** to a local observability stack (**Grafana + Prometheus + Loki + Tempo + OTel Collector**).

This repository is designed as a **senior-level portfolio project**: clean architecture boundaries, reproducible local environment, and production-grade observability patterns.

---

## What you get

- **Ingestion Service** (Spring Boot, Java 21)
  - Upload a production CSV (`multipart/form-data`)
  - Validates and persists daily production rows (idempotent upsert)
  - Emits metrics and traces for ingestion pipeline

- **KPI Service** (Spring Boot, Java 21)
  - Lists wells
  - Computes and serves KPIs
  - Returns “latest day” KPIs per well

- **Observability stack (Docker Compose)**
  - **Prometheus** for metrics
  - **Grafana** for dashboards + Explore
  - **Loki + Promtail** for logs
  - **Tempo** for traces
  - **OpenTelemetry Collector** for OTLP pipeline
  - **PostgreSQL** as data store (host port mapped to **5433** to avoid conflicts)

---

## Architecture (high-level)

```
                 ┌───────────────────────────────┐
                 │           Grafana              │
                 │ Dashboards / Explore (Loki/Tempo)
                 └───────────────┬───────────────┘
                                 │
     ┌───────────────┐    ┌──────▼──────┐    ┌───────────────┐
     │ Ingestion API  │    │ Prometheus  │    │     Tempo      │
     │ (8081)         │───▶│ (9090)      │    │ (3200)         │
     │ /v1/ingestions │    └─────────────┘    └───────────────┘
     │ /actuator/*    │           ▲                  ▲
     └───────┬────────┘           │                  │ OTLP
             │ JDBC               │ metrics           │
             ▼                    │                  │
        ┌───────────┐        ┌────┴────┐       ┌─────┴─────┐
        │ Postgres   │        │  Loki   │       │ OTel Coll. │
        │ (5433->5432)◀───────│ (3100)  │◀──────│ (4317)     │
        └─────┬─────┘  logs   └────┬────┘       └───────────┘
              │                    │
              ▼                    ▼
     ┌────────────────────────────────────┐
     │ KPI Service (8082)                 │
     │ /v1/kpis/wells                     │
     │ /v1/kpis/wells/latest?wellId=...   │
     │ /actuator/*                        │
     └────────────────────────────────────┘
```

---

## Tech stack

- **Java 21**, **Spring Boot 3**
- Maven multi-module (monorepo style)
- **PostgreSQL 16**
- OpenTelemetry Java Agent (automatic instrumentation)
- Micrometer + Actuator (`/actuator/prometheus`)
- Docker Compose: Grafana, Prometheus, Loki, Tempo, OTel Collector, Promtail

---

## Quick start (local)

### 1) Start the stack

```bash
docker compose up -d
docker compose ps
```

### 2) Check health

```bash
curl -sS http://127.0.0.1:8081/actuator/health
curl -sS http://127.0.0.1:8082/actuator/health
```

### 3) Ingest sample CSV

```bash
curl -sS -F "file=@data/sample_production.csv" http://127.0.0.1:8081/v1/ingestions
```

You’ll get a `batchId` back.

### 4) Query wells and latest KPIs

List wells:

```bash
curl -sS http://127.0.0.1:8082/v1/kpis/wells
```

Then query latest KPIs using the **query-param endpoint** (important because Volve well IDs contain `/` and spaces):

```bash
curl -sS "http://127.0.0.1:8082/v1/kpis/wells/latest?wellId=15%2F9-F-11%20H"
```

---

## One-command demo

### Using Makefile

```bash
make demo
```

This will:
1) start the stack  
2) ingest the sample CSV  
3) fetch the first wellId  
4) query latest KPIs for that well  

Other useful targets:

```bash
make up
make seed
make health
make logs-ing
make logs-kpi
make urls
```

### Using scripts (optional)
- `scripts/demo.sh` (Git Bash / Linux / macOS)
- `scripts/demo.ps1` (Windows PowerShell)

---

## API reference (minimal)

### Ingestion Service (8081)

- `POST /v1/ingestions`  
  Upload CSV as multipart form field named `file`.

Example:

```bash
curl -i -sS -F "file=@data/sample_production.csv" http://127.0.0.1:8081/v1/ingestions
```

### KPI Service (8082)

- `GET /v1/kpis/wells`  
  Returns list of well IDs discovered in the dataset.

- `GET /v1/kpis/wells/latest?wellId=...`  
  Returns latest-day KPIs for the given well.

Example:

```bash
curl -sS "http://127.0.0.1:8082/v1/kpis/wells/latest?wellId=15%2F9-F-11%20H"
```

---

## Observability

### URLs
- Grafana: `http://127.0.0.1:3000` (admin/admin)
- Prometheus: `http://127.0.0.1:9090`

### Logs (Loki)
Grafana → Explore → Loki:
- `{compose_service="ingestion-service"}`
- `{compose_service="kpi-service"}`

### Traces (Tempo)
Grafana → Explore → Tempo:
- filter by `service.name=ingestion-service`
- filter by `service.name=kpi-service`

### Metrics
Both services expose:
- `GET /actuator/prometheus`

In Prometheus, search:
- `http_server_requests_seconds_count`
- `jvm_memory_used_bytes`
- `process_uptime_seconds`

---

## Troubleshooting

### “Port 5432 already allocated”
The compose file maps Postgres to `5433:5432` by default.  
If you still have conflicts, change the left side of the mapping.

### “404 on /v1/kpis/wells/{wellId}/latest”
Volve well IDs contain `/` and spaces. Use:  
- `GET /v1/kpis/wells/latest?wellId=...` (URL encoded)

### Services not responding
Check logs:
```bash
docker compose logs --tail=200 ingestion-service
docker compose logs --tail=200 kpi-service
```

---

## Roadmap (nice upgrades)
- Add alert thresholds and a `/v1/kpis/alerts` endpoint
- Add a UI dashboard (React/Next.js) consuming the KPI API
- Add RAG/LLM “Ops Copilot” for runbooks + alert explanations (Spring AI)
- Add CI workflow (lint/test/build) + container build publish

---

## License
MIT


---

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
