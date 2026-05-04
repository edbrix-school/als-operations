package com.asg.operations.finaldisbursementaccount.repository;

import com.asg.operations.finaldisbursementaccount.entity.ShipVesselMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipVesselMasterRepository extends JpaRepository<ShipVesselMaster, Long> {

    boolean existsByVesselPoid(Long vesselPoid);

    Optional<ShipVesselMaster> findFirstByVesselNameIgnoreCase(String vesselName);
}
