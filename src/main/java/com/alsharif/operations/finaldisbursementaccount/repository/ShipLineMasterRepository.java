package com.alsharif.operations.finaldisbursementaccount.repository;

import com.alsharif.operations.finaldisbursementaccount.entity.ShipLineMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipLineMasterRepository extends JpaRepository<ShipLineMaster, Long> {

    boolean existsByLinePoid(Long linePoid);
}
