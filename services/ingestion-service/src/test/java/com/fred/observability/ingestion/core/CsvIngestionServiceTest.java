package com.fred.observability.ingestion.core;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvIngestionServiceTest {

  @Test
  void validateRow_ok() {
    CsvIngestionService svc = new CsvIngestionService(null, null, new SimpleMeterRegistry());
    CsvIngestionService.RowParseResult r = svc.validateRow("2015-01-01,15/9-F-1 C,1200,180000,200", 2);
    assertTrue(r.ok());
    assertEquals("15/9-F-1 C", r.production().getWellId());
  }

  @Test
  void validateRow_bad() {
    CsvIngestionService svc = new CsvIngestionService(null, null, new SimpleMeterRegistry());
    CsvIngestionService.RowParseResult r = svc.validateRow("bad,well,1,2,3", 2);
    assertFalse(r.ok());
  }
}
