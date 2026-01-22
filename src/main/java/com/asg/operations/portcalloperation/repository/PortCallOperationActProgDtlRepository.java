package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationActProgDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationActProgDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationActProgDtlRepository extends JpaRepository<PortCallOperationActProgDtl, PortCallOperationActProgDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);

    List<PortCallOperationActProgDtl> findByTransactionPoid(Long transactionPoid);

    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationActProgDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
