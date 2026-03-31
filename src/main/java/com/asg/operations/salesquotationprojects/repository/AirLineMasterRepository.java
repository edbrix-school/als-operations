package com.asg.operations.salesquotationprojects.repository;

import com.asg.operations.salesquotationprojects.entity.AirLineMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirLineMasterRepository extends JpaRepository<AirLineMaster, Long> {
    boolean existsByAirlinePoid(Long airlinePoid);
}
