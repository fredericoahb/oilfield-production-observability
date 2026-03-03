# Observability

## URLs

- Grafana: http://localhost:3000  (admin / admin)
- Prometheus: http://localhost:9090
- Tempo: http://localhost:3200
- Loki: http://localhost:3100
- Promtail: http://localhost:9080

## Where to see traces

1) Open Grafana → Explore
2) Select data source: **Tempo**
3) Query by `service.name`:
   - `ingestion-service`
   - `kpi-service`

## Correlation: logs ↔ traces

Both services log JSON to stdout. Each log line includes `traceId` (from OpenTelemetry context).
In this repo, Loki + Promtail are included; you can jump from logs to traces by the traceId.

## SLIs that dashboards cover

- KPI API latency (p95)
- Ingestion validity (valid vs invalid rows)
- Data freshness lag per well
