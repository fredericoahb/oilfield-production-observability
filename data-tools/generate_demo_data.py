#!/usr/bin/env python3
"""
Generate simple demo CSVs for Oilfield Production Observability.

Creates:
- data/demo_baseline.csv
- data/demo_anomaly_high_watercut.csv

Columns:
production_date, well_id, oil_bopd, water_bopd, gas_sm3d
"""
from __future__ import annotations
import argparse
import datetime as dt
import random
from pathlib import Path

def generate(start: dt.date, days: int, seed: int):
    random.seed(seed)
    wells = ["WELL-ALPHA", "WELL-BRAVO"]
    rows = []
    for w in wells:
        oil0 = 1800 if w == "WELL-ALPHA" else 1400
        water0 = 150 if w == "WELL-ALPHA" else 220
        gas0 = 220000 if w == "WELL-ALPHA" else 180000
        for i in range(days):
            d = start + dt.timedelta(days=i)
            oil = max(50, oil0 * (1 - 0.01*i) + random.uniform(-40, 40))
            water = max(0, water0 * (1 + 0.005*i) + random.uniform(-20, 20))
            gas = max(1000, gas0 * (1 - 0.006*i) + random.uniform(-2500, 2500))
            rows.append((d.isoformat(), w, round(oil, 2), round(water, 2), round(gas, 2)))
    return rows

def write_csv(path: Path, rows):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as f:
        f.write("production_date,well_id,oil_bopd,water_bopd,gas_sm3d\n")
        for d,w,oil,wat,gas in rows:
            f.write(f"{d},{w},{oil},{wat},{gas}\n")

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--start", default="2026-02-01")
    ap.add_argument("--days", type=int, default=14)
    ap.add_argument("--seed", type=int, default=7)
    args = ap.parse_args()

    start = dt.date.fromisoformat(args.start)
    rows = generate(start, args.days, args.seed)

    baseline = Path("data/demo_baseline.csv")
    write_csv(baseline, rows)

    # anomaly: last day water spikes for WELL-ALPHA; oil drops
    last_day = (start + dt.timedelta(days=args.days-1)).isoformat()
    rows2 = []
    for d,w,oil,wat,gas in rows:
        if d == last_day and w == "WELL-ALPHA":
            oil = round(oil * 0.55, 2)
            wat = round(wat * 6.0, 2)
            gas = round(gas * 0.95, 2)
        rows2.append((d,w,oil,wat,gas))

    anomaly = Path("data/demo_anomaly_high_watercut.csv")
    write_csv(anomaly, rows2)

    print("Wrote:", baseline)
    print("Wrote:", anomaly)

if __name__ == "__main__":
    main()
