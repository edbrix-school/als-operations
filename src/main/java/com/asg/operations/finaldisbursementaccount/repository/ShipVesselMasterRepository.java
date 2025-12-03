package com.asg.operations.finaldisbursementaccount.repository;

import com.asg.operations.finaldisbursementaccount.entity.ShipVesselMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipVesselMasterRepository extends JpaRepository<ShipVesselMaster, Long> {

    boolean existsByVesselPoid(Long vesselPoid);
}
