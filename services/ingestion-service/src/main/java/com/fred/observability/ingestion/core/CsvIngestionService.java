package com.fred.observability.ingestion.core;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CsvIngestionService {

  private static final Logger log = LoggerFactory.getLogger(CsvIngestionService.class);

  private final ProductionDailyRepository productionRepo;
  private final IngestionBatchRepository batchRepo;
  private final MeterRegistry registry;

  private final Counter validRows;
  private final Counter invalidRows;

  private final Map<String, AtomicReference<Double>> oilGauge = new ConcurrentHashMap<>();
  private final Map<String, AtomicReference<Double>> gasGauge = new ConcurrentHashMap<>();
  private final Map<String, AtomicReference<Double>> waterCutGauge = new ConcurrentHashMap<>();
  private final Map<String, AtomicReference<Double>> lagGauge = new ConcurrentHashMap<>();

  public CsvIngestionService(ProductionDailyRepository productionRepo, IngestionBatchRepository batchRepo, MeterRegistry registry) {
    this.productionRepo = productionRepo;
    this.batchRepo = batchRepo;
    this.registry = registry;
    this.validRows = Counter.builder("ingestion_rows_total").tag("status", "valid").register(registry);
    this.invalidRows = Counter.builder("ingestion_rows_total").tag("status", "invalid").register(registry);
  }

  @WithSpan("ingestBatch")
  public UUID ingest(MultipartFile file) {
    UUID batchId = UUID.randomUUID();
    IngestionBatch batch = new IngestionBatch(batchId, file.getOriginalFilename() == null ? "upload.csv" : file.getOriginalFilename());
    batchRepo.save(batch);

    boolean hadErrors = false;

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      // Expect header: production_date,well_id,oil_bopd,gas_sm3d,water_bopd
      String header = reader.readLine();
      if (header == null || !header.toLowerCase().contains("production_date")) {
        throw new IllegalArgumentException("CSV must start with header: production_date,well_id,oil_bopd,gas_sm3d,water_bopd");
      }

      String line;
      long rowNum = 1;
      while ((line = reader.readLine()) != null) {
        rowNum++;
        batch.incTotal();
        RowParseResult parsed = validateRow(line, rowNum);
        if (!parsed.ok()) {
          hadErrors = true;
          batch.incInvalid();
          invalidRows.increment();
          log.warn("row_invalid wellId={} batchId={} sourceFile={} validationErrors={} traceId={}",
              parsed.wellId(), batchId, batch.getSourceFile(), parsed.error(), traceId());
          continue;
        }

        persistTimeseries(parsed.production(), batchId, batch.getSourceFile());
        batch.incValid();
        validRows.increment();

        // update per-well gauges
        registerWellGaugesIfNeeded(parsed.production().getWellId());
        oilGauge.get(parsed.production().getWellId()).set(parsed.production().getOilBopd());
        gasGauge.get(parsed.production().getWellId()).set(parsed.production().getGasSm3d());

        double wc = waterCutPct(parsed.production().getOilBopd(), parsed.production().getWaterBopd());
        waterCutGauge.get(parsed.production().getWellId()).set(wc);

        long lagSec = computeLagSeconds(parsed.production().getProductionDate());
        lagGauge.get(parsed.production().getWellId()).set((double) lagSec);
      }

      batch.markCompleted(hadErrors);
      batchRepo.save(batch);

      log.info("ingest_done batchId={} sourceFile={} rowsTotal={} rowsValid={} rowsInvalid={} traceId={}",
          batchId, batch.getSourceFile(), batch.getRowsTotal(), batch.getRowsValid(), batch.getRowsInvalid(), traceId());

      return batchId;
    } catch (Exception e) {
      batch.markFailed();
      batchRepo.save(batch);
      log.error("ingest_failed batchId={} sourceFile={} err={} traceId={}", batchId, batch.getSourceFile(), e.toString(), traceId(), e);
      throw new RuntimeException(e);
    }
  }

  @WithSpan("validateRow")
  RowParseResult validateRow(String csvLine, long rowNum) {
    try {
      String[] parts = csvLine.split(",", -1);
      if (parts.length < 5) return RowParseResult.error(null, "Expected 5 columns, got " + parts.length);

      LocalDate date = LocalDate.parse(parts[0].trim());
      String wellId = parts[1].trim();
      if (wellId.isBlank()) return RowParseResult.error(null, "well_id is blank");

      double oil = Double.parseDouble(parts[2].trim());
      double gas = Double.parseDouble(parts[3].trim());
      double water = Double.parseDouble(parts[4].trim());

      if (oil < 0 || gas < 0 || water < 0) return RowParseResult.error(wellId, "negative values not allowed");

      return RowParseResult.ok(new ProductionDaily(wellId, date, oil, gas, water));
    } catch (Exception e) {
      return RowParseResult.error(null, "Row " + rowNum + ": " + e.getMessage());
    }
  }

  @WithSpan("persistTimeseries")
void persistTimeseries(ProductionDaily p, UUID batchId, String sourceFile) {
  try {
    productionRepo.save(p);
  } catch (DataIntegrityViolationException dup) {
    // Idempotency: if (well_id, production_date) already exists, update values instead of failing
    productionRepo.findByWellIdAndProductionDate(p.getWellId(), p.getProductionDate()).ifPresent(existing -> {
      existing.applyValuesFrom(p);
      productionRepo.save(existing);
    });
  }
  log.debug("row_persisted wellId={} date={} batchId={} sourceFile={} traceId={}",
      p.getWellId(), p.getProductionDate(), batchId, sourceFile, traceId());
}


  private void registerWellGaugesIfNeeded(String wellId) {
    oilGauge.computeIfAbsent(wellId, k -> {
      AtomicReference<Double> v = new AtomicReference<>(0.0);
      Gauge.builder("production_oil_bopd", v, AtomicReference::get).tag("well_id", wellId).register(registry);
      return v;
    });

    gasGauge.computeIfAbsent(wellId, k -> {
      AtomicReference<Double> v = new AtomicReference<>(0.0);
      Gauge.builder("production_gas_sm3d", v, AtomicReference::get).tag("well_id", wellId).register(registry);
      return v;
    });

    waterCutGauge.computeIfAbsent(wellId, k -> {
      AtomicReference<Double> v = new AtomicReference<>(0.0);
      Gauge.builder("water_cut_pct", v, AtomicReference::get).tag("well_id", wellId).register(registry);
      return v;
    });

    lagGauge.computeIfAbsent(wellId, k -> {
      AtomicReference<Double> v = new AtomicReference<>(0.0);
      Gauge.builder("ingestion_lag_seconds", v, AtomicReference::get).tag("well_id", wellId).register(registry);
      return v;
    });
  }

  private static double waterCutPct(double oil, double water) {
    double denom = oil + water;
    if (denom <= 0) return 0.0;
    return (water / denom) * 100.0;
  }

  private static long computeLagSeconds(LocalDate productionDate) {
    LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
    long days = java.time.temporal.ChronoUnit.DAYS.between(productionDate, todayUtc);
    return Math.max(0, days) * 86400;
  }

  private static String traceId() {
    return Span.current().getSpanContext().getTraceId();
  }

  record RowParseResult(boolean ok, ProductionDaily production, String wellId, String error) {
    static RowParseResult ok(ProductionDaily p) { return new RowParseResult(true, p, p.getWellId(), null); }
    static RowParseResult error(String wellId, String error) { return new RowParseResult(false, null, wellId, error); }
  }
}
