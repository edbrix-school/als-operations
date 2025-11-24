package com.alsharif.operations.pdaporttariffmaster.repository;

import com.alsharif.operations.pdaporttariffmaster.entity.ShipChargeMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface ShipChargeMasterRepository extends JpaRepository<ShipChargeMaster, BigDecimal> {
    boolean existsByChargePoidAndActiveIgnoreCaseAndDeletedIgnoreCase(BigDecimal chargePoid, String active, String deleted);
}

