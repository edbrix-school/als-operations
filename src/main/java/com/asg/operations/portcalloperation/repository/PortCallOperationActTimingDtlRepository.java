package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationActTimingDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationActTimingDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationActTimingDtlRepository extends JpaRepository<PortCallOperationActTimingDtl, PortCallOperationActTimingDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);

    List<PortCallOperationActTimingDtl> findByTransactionPoid(Long transactionPoid);

    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationActTimingDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
