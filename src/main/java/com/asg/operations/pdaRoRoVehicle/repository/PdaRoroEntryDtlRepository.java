package com.asg.operations.pdaRoRoVehicle.repository;

import com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtl;
import com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtlId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PdaRoroEntryDtlRepository extends JpaRepository<PdaRoRoEntryDtl, PdaRoRoEntryDtlId> {

    List<PdaRoRoEntryDtl> findByIdTransactionPoid(Long transactionPoid);
}
