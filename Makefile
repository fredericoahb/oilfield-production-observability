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
