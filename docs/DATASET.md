# Dataset

This repo ships with a **small sample CSV** under `data/sample_production.csv` so you can run the demo immediately.

For a realistic demo with real production history, use the **Volve open dataset** from Equinor.

Options:

1) **Equinor Volve data sharing (official)**
- Access via Equinor's open data page and follow the user guide to pull the dataset from Databricks Marketplace.
- You'll find production tables inside the dataset (including daily production history).

2) **Kaggle (simplified Volve production tables)**
- Search for "Volve production data" datasets on Kaggle.
- Use the daily production table as input.

## CSV schema expected by ingestion-service

```
production_date,well_id,oil_bopd,gas_sm3d,water_bopd
2015-01-01,15/9-F-1 C,1234,185000,210
```

Notes:
- `oil_bopd` and `water_bopd` are barrels of liquid per day (BOPD / BWPD)
- `gas_sm3d` is standard m³ per day

If you ingest a different schema, add an adapter in `CsvIngestionService`.
