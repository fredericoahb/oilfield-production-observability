package com.fred.observability.kpi.api;

import com.fred.observability.kpi.core.KpiComputeService;
import com.fred.observability.kpi.core.KpiModels;
import com.fred.observability.kpi.core.ProductionDailyRepository;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/kpis")
public class KpiController {

  private final KpiComputeService computeService;
  private final ProductionDailyRepository repo;

  public KpiController(KpiComputeService computeService, ProductionDailyRepository repo) {
    this.computeService = computeService;
    this.repo = repo;
  }

  @GetMapping("/wells")
  @WithSpan("serveApi")
  public Map<String, Object> listWells() {
    List<String> wells = repo.listWells();
    return Map.of("count", wells.size(), "wells", wells);
  }

  @GetMapping("/wells/latest")
  @WithSpan("serveApi")
  public KpiModels.WellKpiLatest latestByQuery(@RequestParam("wellId") String wellId) {
    return computeService.latestKpi(wellId)
        .orElseThrow(() -> new WellNotFoundException(wellId));
  }

  @GetMapping("/wells/{wellId}/latest")
  @WithSpan("serveApi")
  public KpiModels.WellKpiLatest latest(@PathVariable("wellId") String wellId) {
    return computeService.latestKpi(wellId)
        .orElseThrow(() -> new WellNotFoundException(wellId));
  }

  @GetMapping("/alerts/latest")
  @WithSpan("serveApi")
  public KpiModels.AlertsResponse alerts() {
    return computeService.lastAlerts();
  }

  @PostMapping("/recompute")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @WithSpan("serveApi")
  public Map<String, Object> recompute() {
    computeService.computeKpisAndAlerts();
    return Map.of("status", "ok");
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  private static class WellNotFoundException extends RuntimeException {
    WellNotFoundException(String wellId) { super("Well not found: " + wellId); }
  }
}
