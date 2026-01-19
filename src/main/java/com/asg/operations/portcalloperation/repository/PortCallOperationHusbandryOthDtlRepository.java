package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationHusbandryOthDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationHusbandryOthDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationHusbandryOthDtlRepository extends JpaRepository<PortCallOperationHusbandryOthDtl, PortCallOperationHusbandryOthDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);
    List<PortCallOperationHusbandryOthDtl> findByTransactionPoid(Long transactionPoid);
    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationHusbandryOthDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
