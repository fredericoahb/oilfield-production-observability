.PHONY: help up down logs test build

help:
	@echo "Targets:"
	@echo "  up     - start full stack (services + observability)"
	@echo "  down   - stop stack"
	@echo "  logs   - follow logs"
	@echo "  build  - build jars"
	@echo "  test   - run tests"

MVN := $(shell if [ -x ./mvnw ]; then echo ./mvnw; else echo mvn; fi)

build:
	$(MVN) -q -DskipTests package

test:
	$(MVN) -q test

up:
	docker compose up -d --build

down:
	docker compose down -v

logs:
	docker compose logs -f --tail=200
# --- Codespaces / demo targets ---
COMPOSE ?= docker compose

.PHONY: up seed demo urls health

up:
	$(COMPOSE) up -d

seed:
	curl -sS -F "file=@data/sample_production.csv" http://127.0.0.1:8081/v1/ingestions | cat

health:
	@echo "Ingestion:"
	@curl -sS http://127.0.0.1:8081/actuator/health | cat
	@echo ""
	@echo "KPI:"
	@curl -sS http://127.0.0.1:8082/actuator/health | cat
	@echo ""

demo: up seed
	@echo "Wells:"
	@curl -sS http://127.0.0.1:8082/v1/kpis/wells | cat
	@echo ""
	@echo "Grafana: http://127.0.0.1:3000 (admin/admin)"
	@echo "Prometheus: http://127.0.0.1:9090"

urls:
	@echo "Grafana:    http://127.0.0.1:3000 (admin/admin)"
	@echo "Prometheus: http://127.0.0.1:9090"
	@echo "Ingestion:  http://127.0.0.1:8081/actuator/health"
	@echo "KPI:        http://127.0.0.1:8082/actuator/health"

