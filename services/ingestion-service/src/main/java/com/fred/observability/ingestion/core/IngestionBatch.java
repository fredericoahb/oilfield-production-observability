package com.fred.observability.ingestion.core;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ingestion_batch")
public class IngestionBatch {

  @Id
  @Column(name = "batch_id", nullable = false)
  private UUID batchId;

  @Column(name = "source_file", nullable = false)
  private String sourceFile;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private IngestionBatchStatus status;

  @Column(name = "rows_total", nullable = false)
  private long rowsTotal;

  @Column(name = "rows_valid", nullable = false)
  private long rowsValid;

  @Column(name = "rows_invalid", nullable = false)
  private long rowsInvalid;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  protected IngestionBatch() {}

  public IngestionBatch(UUID batchId, String sourceFile) {
    this.batchId = batchId;
    this.sourceFile = sourceFile;
    this.status = IngestionBatchStatus.RUNNING;
    this.rowsTotal = 0;
    this.rowsValid = 0;
    this.rowsInvalid = 0;
    this.startedAt = Instant.now();
  }

  public UUID getBatchId() { return batchId; }
  public String getSourceFile() { return sourceFile; }
  public IngestionBatchStatus getStatus() { return status; }
  public long getRowsTotal() { return rowsTotal; }
  public long getRowsValid() { return rowsValid; }
  public long getRowsInvalid() { return rowsInvalid; }
  public Instant getStartedAt() { return startedAt; }
  public Instant getFinishedAt() { return finishedAt; }

  public void incTotal() { this.rowsTotal++; }
  public void incValid() { this.rowsValid++; }
  public void incInvalid() { this.rowsInvalid++; }

  public void markCompleted(boolean withErrors) {
    this.status = withErrors ? IngestionBatchStatus.COMPLETED_WITH_ERRORS : IngestionBatchStatus.COMPLETED;
    this.finishedAt = Instant.now();
  }

  public void markFailed() {
    this.status = IngestionBatchStatus.FAILED;
    this.finishedAt = Instant.now();
  }
}
