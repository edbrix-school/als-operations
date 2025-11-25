package com.alsharif.operations.vesseltype.repository;

import com.alsharif.operations.vesseltype.entity.VesselType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VesselTypeRepository extends JpaRepository<VesselType, Long> {
    
    @Query("SELECT v FROM VesselType v WHERE (v.active IS NULL OR v.active = 'Y') AND (v.deleted IS NULL OR v.deleted = 'N') ORDER BY v.seqno ASC")
    List<VesselType> findAllActive();
}
