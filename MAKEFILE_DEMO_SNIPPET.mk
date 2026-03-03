# --- Demo scenario (simulated data) ---
.PHONY: demo-baseline demo-anomaly demo-walkthrough

demo-baseline: up
	python3 data-tools/generate_demo_data.py
	curl -sS -F "file=@data/demo_baseline.csv" http://127.0.0.1:8081/v1/ingestions | cat
	@echo ""
	curl -sS http://127.0.0.1:8082/v1/kpis/wells | cat
	@echo ""
	curl -sS "http://127.0.0.1:8082/v1/kpis/wells/latest?wellId=WELL-ALPHA" | cat
	@echo ""

demo-anomaly: up
	python3 data-tools/generate_demo_data.py
	curl -sS -F "file=@data/demo_anomaly_high_watercut.csv" http://127.0.0.1:8081/v1/ingestions | cat
	@echo ""
	curl -sS "http://127.0.0.1:8082/v1/kpis/wells/latest?wellId=WELL-ALPHA" | cat
	@echo ""

demo-walkthrough: demo-baseline demo-anomaly
	@echo ""
	@echo "Next: open Grafana at http://127.0.0.1:3000 (admin/admin) and take screenshots."
