package com.asg.operations.pdaRoRoVehicle.repository;

import com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdaRoRoEntryHdrRepository extends JpaRepository<PdaRoRoEntryHdr, Long> {
}
