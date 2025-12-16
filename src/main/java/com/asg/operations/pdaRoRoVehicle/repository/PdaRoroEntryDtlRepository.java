package com.asg.operations.pdaRoRoVehicle.repository;

import com.asg.operations.pdaRoRoVehicle.entity.PdaRoroEntryDtl;
import com.asg.operations.pdaRoRoVehicle.entity.PdaRoroEntryDtlId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PdaRoroEntryDtlRepository extends JpaRepository<PdaRoroEntryDtl, PdaRoroEntryDtlId> {

    List<PdaRoroEntryDtl> findByIdTransactionPoid(Long transactionPoid);
}
