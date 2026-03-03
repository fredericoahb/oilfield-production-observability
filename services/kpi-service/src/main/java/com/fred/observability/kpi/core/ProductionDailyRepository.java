package com.fred.observability.kpi.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionDailyRepository extends JpaRepository<ProductionDaily, UUID> {

  Optional<ProductionDaily> findTopByWellIdOrderByProductionDateDesc(String wellId);

  @Query("select distinct p.wellId from ProductionDaily p")
  List<String> listWells();

  List<ProductionDaily> findByWellIdAndProductionDateBetweenOrderByProductionDateAsc(String wellId, LocalDate from, LocalDate to);
}
