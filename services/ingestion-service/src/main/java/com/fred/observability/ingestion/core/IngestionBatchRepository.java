package com.fred.observability.ingestion.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IngestionBatchRepository extends JpaRepository<IngestionBatch, UUID> {}
