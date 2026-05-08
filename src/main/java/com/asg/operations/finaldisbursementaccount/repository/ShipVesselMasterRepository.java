package com.asg.operations.finaldisbursementaccount.repository;

import com.asg.operations.finaldisbursementaccount.entity.ShipVesselMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipVesselMasterRepository extends JpaRepository<ShipVesselMaster, Long> {

    boolean existsByVesselPoid(Long vesselPoid);

    Optional<ShipVesselMaster> findFirstByVesselNameIgnoreCase(String vesselName);

    @Query(value = "SELECT * FROM SHIP_VESSEL_MASTER WHERE TRIM(LOWER(VESSEL_NAME)) = LOWER(TRIM(:vesselName)) AND ROWNUM = 1", nativeQuery = true)
    Optional<ShipVesselMaster> findFirstByVesselNameTrimmedIgnoreCase(@Param("vesselName") String vesselName);
}
