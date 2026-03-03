package com.fred.observability.kpi.core;

import java.time.LocalDate;
import java.util.List;

public class KpiModels {

  public record WellKpiLatest(
      String wellId,
      LocalDate date,
      double oilBopd,
      double gasSm3d,
      double waterBopd,
      double waterCutPct
  ) {}

  public record Alert(
      String wellId,
      String type,
      String severity,
      String message,
      LocalDate date
  ) {}

  public record AlertsResponse(
      LocalDate evaluatedDate,
      List<Alert> alerts
  ) {}
}
