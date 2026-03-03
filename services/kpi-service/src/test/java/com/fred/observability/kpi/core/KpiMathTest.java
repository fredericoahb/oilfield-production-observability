package com.fred.observability.kpi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KpiMathTest {

  @Test
  void waterCutPct_zeroDenom_isZero() {
    assertEquals(0.0, KpiMath.waterCutPct(0, 0), 0.0001);
    assertEquals(0.0, KpiMath.waterCutPct(-1, -1), 0.0001);
  }

  @Test
  void waterCutPct_basic() {
    assertEquals(20.0, KpiMath.waterCutPct(80, 20), 0.0001);
    assertEquals(50.0, KpiMath.waterCutPct(50, 50), 0.0001);
  }
}
