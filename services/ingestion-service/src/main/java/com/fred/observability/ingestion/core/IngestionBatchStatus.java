package com.fred.observability.ingestion.core;

public enum IngestionBatchStatus {
  RUNNING,
  COMPLETED,
  COMPLETED_WITH_ERRORS,
  FAILED
}
