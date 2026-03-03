package com.fred.observability.kpi.core;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.fred.observability.kpi.core.KpiModels.*;

@Service
public class KpiComputeService {

  private static final Logger log = LoggerFactory.getLogger(KpiComputeService.class);

  private final ProductionDailyRepository repo;
  private final MeterRegistry registry;
  private final Timer computeTimer;

  private final Map<String, AtomicReference<Double>> oilGauge = new ConcurrentHashMap<>();
  private final Map<String, AtomicReference<Double>> gasGauge = new ConcurrentHashMap<>();
  private final Map<String, AtomicReference<Double>> waterCutGauge = new ConcurrentHashMap<>();

  private volatile AlertsResponse lastAlerts = new AlertsResponse(LocalDate.now(ZoneOffset.UTC), List.of());

  public KpiComputeService(ProductionDailyRepository repo, MeterRegistry registry) {
    this.repo = repo;
    this.registry = registry;
    this.computeTimer = Timer.builder("kpi_compute_duration")
        .description("Duration of KPI computation")
        .publishPercentileHistogram()
        .register(registry);
  }

  @Scheduled(fixedDelayString = "300000", initialDelayString = "15000")
  public void scheduledCompute() {
    computeKpisAndAlerts();
  }

  @WithSpan("computeKpi")
  public void computeKpisAndAlerts() {
    computeTimer.record(() -> {
      LocalDate now = LocalDate.now(ZoneOffset.UTC);
      List<String> wells = repo.listWells();
      List<Alert> alerts = new ArrayList<>();

      for (String well : wells) {
        Optional<ProductionDaily> latestOpt = repo.findTopByWellIdOrderByProductionDateDesc(well);
        if (latestOpt.isEmpty()) continue;

        ProductionDaily latest = latestOpt.get();
        registerWellGaugesIfNeeded(well);

        oilGauge.get(well).set(latest.getOilBopd());
        gasGauge.get(well).set(latest.getGasSm3d());
        waterCutGauge.get(well).set(KpiMath.waterCutPct(latest.getOilBopd(), latest.getWaterBopd()));

        // basic alerting based on 7-day baseline
        LocalDate from = latest.getProductionDate().minusDays(7);
        List<ProductionDaily> window = repo.findByWellIdAndProductionDateBetweenOrderByProductionDateAsc(
            well, from, latest.getProductionDate());

        double avgOil = window.stream().mapToDouble(ProductionDaily::getOilBopd).average().orElse(latest.getOilBopd());
        double avgWc = window.stream().mapToDouble(p -> KpiMath.waterCutPct(p.getOilBopd(), p.getWaterBopd())).average().orElse(0.0);

        if (latest.getOilBopd() < 0.7 * avgOil && avgOil > 0) {
          alerts.add(new Alert(well, "production_drop", "high",
              String.format("Oil dropped: latest=%.1f bopd vs 7d_avg=%.1f bopd", latest.getOilBopd(), avgOil),
              latest.getProductionDate()));
        }

        double latestWc = KpiMath.waterCutPct(latest.getOilBopd(), latest.getWaterBopd());
        if (latestWc > 60 && (latestWc - avgWc) >= 10) {
          alerts.add(new Alert(well, "water_cut_increase", "medium",
              String.format("Water cut increased: latest=%.1f%% vs 7d_avg=%.1f%%", latestWc, avgWc),
              latest.getProductionDate()));
        }
      }

      lastAlerts = new AlertsResponse(now, alerts);

      log.info("kpi_compute_done wells={} alerts={} traceId={}", wells.size(), alerts.size(), traceId());
    });
  }

  public AlertsResponse lastAlerts() {
    return lastAlerts;
  }

  public Optional<WellKpiLatest> latestKpi(String wellId) {
    return repo.findTopByWellIdOrderByProductionDateDesc(wellId)
        .map(p -> new WellKpiLatest(p.getWellId(), p.getProductionDate(), p.getOilBopd(), p.getGasSm3d(), p.getWaterBopd(),
            KpiMath.waterCutPct(p.getOilBopd(), p.getWaterBopd())));
  }

  private void registerWellGaugesIfNeeded(String wellId) {
    oilGauge.computeIfAbsent(wellId, k -> {
      AtomicReference<Double> v = new AtomicReference<>(0.0);
      Gauge.builder("production_oil_bopd", v, AtomicReference::get)
          .description("Latest oil production (bopd)")
          .tag("well_id", wellId)
          .register(registry);
      return v;
    });
    gasGauge.computeIfAbsent(wellId, k -> {
      AtomicReference<Double> v = new AtomicReference<>(0.0);
      Gauge.builder("production_gas_sm3d", v, AtomicReference::get)
          .description("Latest gas production (sm3d)")
          .tag("well_id", wellId)
          .register(registry);
      return v;
    });
    waterCutGauge.computeIfAbsent(wellId, k -> {
      AtomicReference<Double> v = new AtomicReference<>(0.0);
      Gauge.builder("water_cut_pct", v, AtomicReference::get)
          .description("Latest water cut percentage")
          .tag("well_id", wellId)
          .register(registry);
      return v;
    });
  }

  private static String traceId() {
    return Span.current().getSpanContext().getTraceId();
  }
}
