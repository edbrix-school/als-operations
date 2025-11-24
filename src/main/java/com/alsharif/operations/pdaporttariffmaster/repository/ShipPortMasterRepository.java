package com.alsharif.operations.pdaporttariffmaster.repository;

import com.alsharif.operations.pdaporttariffmaster.entity.ShipPortMaster;
import com.alsharif.operations.pdaporttariffmaster.key.ShipPortMasterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ShipPortMasterRepository extends JpaRepository<ShipPortMaster, ShipPortMasterId> {
    boolean existsByIdPortPoidAndIdGroupPoid(BigDecimal portPoid, BigDecimal groupPoid);

    @Query("SELECT s.portName FROM ShipPortMaster s WHERE s.id.portPoid IN :portPoidList AND s.id.groupPoid = :groupPoid AND (s.deleted IS NULL OR UPPER(s.deleted) = 'N')")
    List<String> findPortNamesByPortPoidInAndGroupPoid(
            @Param("portPoidList") List<BigDecimal> portPoidList,
            @Param("groupPoid") BigDecimal groupPoid);
}

