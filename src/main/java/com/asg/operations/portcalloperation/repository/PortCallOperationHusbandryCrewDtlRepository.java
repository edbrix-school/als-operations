package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationHusbandryCrewDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationHusbandryCrewDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationHusbandryCrewDtlRepository extends JpaRepository<PortCallOperationHusbandryCrewDtl, PortCallOperationHusbandryCrewDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);
    List<PortCallOperationHusbandryCrewDtl> findByTransactionPoid(Long transactionPoid);
    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationHusbandryCrewDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
