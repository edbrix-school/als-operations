package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationEstBertDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationEstBertDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationEstBertDtlRepository extends JpaRepository<PortCallOperationEstBertDtl, PortCallOperationEstBertDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);
    List<PortCallOperationEstBertDtl> findByTransactionPoid(Long transactionPoid);
    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationEstBertDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
