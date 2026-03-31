package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationActCondDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationActCondDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationActCondDtlRepository extends JpaRepository<PortCallOperationActCondDtl, PortCallOperationActCondDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);

    List<PortCallOperationActCondDtl> findByTransactionPoid(Long transactionPoid);

    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationActCondDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
