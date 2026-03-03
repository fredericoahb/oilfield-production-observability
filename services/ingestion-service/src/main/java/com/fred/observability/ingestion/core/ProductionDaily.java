package com.fred.observability.ingestion.core;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "production_daily",
    uniqueConstraints = @UniqueConstraint(name = "uk_well_date", columnNames = {"well_id", "production_date"}))
public class ProductionDaily {

  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "well_id", nullable = false, length = 64)
  private String wellId;

  @Column(name = "production_date", nullable = false)
  private LocalDate productionDate;

  @Column(name = "oil_bopd", nullable = false)
  private double oilBopd;

  @Column(name = "gas_sm3d", nullable = false)
  private double gasSm3d;

  @Column(name = "water_bopd", nullable = false)
  private double waterBopd;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected ProductionDaily() {}

  public ProductionDaily(String wellId, LocalDate productionDate, double oilBopd, double gasSm3d, double waterBopd) {
    this.wellId = wellId;
    this.productionDate = productionDate;
    this.oilBopd = oilBopd;
    this.gasSm3d = gasSm3d;
    this.waterBopd = waterBopd;
    this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
  }

  public UUID getId() { return id; }
  public String getWellId() { return wellId; }
  public LocalDate getProductionDate() { return productionDate; }
  public double getOilBopd() { return oilBopd; }
  public double getGasSm3d() { return gasSm3d; }
  public double getWaterBopd() { return waterBopd; }

  public void applyValuesFrom(ProductionDaily other) {
    this.oilBopd = other.oilBopd;
    this.gasSm3d = other.gasSm3d;
    this.waterBopd = other.waterBopd;
    // keep createdAt as original record creation
  }

}
