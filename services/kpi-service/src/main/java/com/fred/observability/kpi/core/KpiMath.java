package com.fred.observability.kpi.core;

public final class KpiMath {
  private KpiMath() {}

  public static double waterCutPct(double oilBopd, double waterBopd) {
    double denom = oilBopd + waterBopd;
    if (denom <= 0) return 0.0;
    return (waterBopd / denom) * 100.0;
  }
}
