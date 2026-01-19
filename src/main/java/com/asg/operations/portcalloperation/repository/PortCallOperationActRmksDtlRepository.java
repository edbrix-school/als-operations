package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationActRmksDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationActRmksDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationActRmksDtlRepository extends JpaRepository<PortCallOperationActRmksDtl, PortCallOperationActRmksDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);

    List<PortCallOperationActRmksDtl> findByTransactionPoid(Long transactionPoid);

    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationActRmksDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
