package com.alsharif.operations.pdaporttariffmaster.repository;

import com.alsharif.operations.pdaporttariffmaster.entity.ShipVesselTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ShipVesselTypeMasterRepository extends JpaRepository<ShipVesselTypeMaster, BigDecimal> {
    boolean existsByVesselTypePoidAndGroupPoid(BigDecimal vesselTypePoid, BigDecimal groupPoid);

    boolean existsByVesselTypePoid(BigDecimal vesselTypePoid);

    @Query("SELECT v.vesselTypeName " +
            "FROM ShipVesselTypeMaster v " +
            "WHERE v.vesselTypePoid IN :vesselTypePoidList " +
            "AND v.groupPoid = :groupPoid " +
            "AND (v.deleted IS NULL OR UPPER(v.deleted) = 'N')")
    List<String> findVesselTypeNamesByVesselTypePoidInAndGroupPoid(
            @Param("vesselTypePoidList") List<BigDecimal> vesselTypePoidList,
            @Param("groupPoid") BigDecimal groupPoid);
}

