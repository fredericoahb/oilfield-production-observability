package com.fred.observability.ingestion.api;

import com.fred.observability.ingestion.core.CsvIngestionService;
import com.fred.observability.ingestion.core.IngestionBatch;
import com.fred.observability.ingestion.core.IngestionBatchRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/ingestions")
public class IngestionController {

  private final CsvIngestionService ingestionService;
  private final IngestionBatchRepository batchRepository;

  public IngestionController(CsvIngestionService ingestionService, IngestionBatchRepository batchRepository) {
    this.ingestionService = ingestionService;
    this.batchRepository = batchRepository;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Object> ingest(@RequestPart("file") @NotNull MultipartFile file) {
    UUID batchId = ingestionService.ingest(file);
    return Map.of("batchId", batchId.toString());
  }

  @GetMapping(value = "/{batchId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Object> status(@PathVariable("batchId") String batchId) {
    Optional<IngestionBatch> batch = batchRepository.findById(UUID.fromString(batchId));
    return batch.map(b -> Map.<String, Object>of(
        "batchId", b.getBatchId().toString(),
        "status", b.getStatus().name(),
        "sourceFile", b.getSourceFile(),
        "rowsTotal", b.getRowsTotal(),
        "rowsValid", b.getRowsValid(),
        "rowsInvalid", b.getRowsInvalid(),
        "startedAt", b.getStartedAt(),
        "finishedAt", b.getFinishedAt()
    )).orElseGet(() -> Map.of("batchId", batchId, "status", "NOT_FOUND"));
  }
}
