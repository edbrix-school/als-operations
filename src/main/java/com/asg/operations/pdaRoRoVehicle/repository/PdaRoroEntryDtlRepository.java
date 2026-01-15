package com.asg.operations.pdaRoRoVehicle.repository;

import com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtl;
import com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PdaRoroEntryDtlRepository extends JpaRepository<PdaRoRoEntryDtl, PdaRoRoEntryDtlId> {

    List<PdaRoRoEntryDtl> findByIdTransactionPoid(Long transactionPoid);
}
