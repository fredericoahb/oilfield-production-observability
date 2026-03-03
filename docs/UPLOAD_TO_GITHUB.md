# How to upload this project to GitHub

## Option A) Using Git CLI (recommended)

1) Create an empty repo on GitHub:
- Name: `oilfield-production-observability`
- Public or Private (your choice)
- Do **not** add README (this repo already has one)

2) Unzip this project locally.

3) Initialize git + first push:
```bash
git init
git add .
git commit -m "feat: initial oilfield observability demo"
git branch -M main
git remote add origin https://github.com/<your-user>/oilfield-production-observability.git
git push -u origin main
```

4) Enable GitHub Actions (CI is already in `.github/workflows/ci.yml`).

## Option B) Upload via GitHub Web UI

1) Create the repo in GitHub.
2) In the repo, click **Add file → Upload files**.
3) Drag-and-drop the unzipped folder content (not the folder itself).
4) Commit directly to `main`.

## Suggested repository settings

- Add topics: `java`, `spring-boot`, `opentelemetry`, `prometheus`, `grafana`, `tempo`, `observability`, `oil-and-gas`, `telemetry`
- Add a short description: “Oilfield production ingestion + KPI API + full observability stack (Prometheus/Grafana/Tempo)”
- Pin the repo in your GitHub profile.
