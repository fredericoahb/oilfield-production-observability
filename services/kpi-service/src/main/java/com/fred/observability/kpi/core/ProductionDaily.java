package com.fred.observability.kpi.core;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
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

  protected ProductionDaily() {}

  public String getWellId() { return wellId; }
  public LocalDate getProductionDate() { return productionDate; }
  public double getOilBopd() { return oilBopd; }
  public double getGasSm3d() { return gasSm3d; }
  public double getWaterBopd() { return waterBopd; }
}
