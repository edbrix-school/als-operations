package com.asg.operations.finaldisbursementaccount.repository;

import com.asg.operations.finaldisbursementaccount.entity.ShipVoyageHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipVoyageHdrRepository extends JpaRepository<ShipVoyageHdr, Long> {

    boolean existsByTransactionPoid(Long voyagePoid);
}
