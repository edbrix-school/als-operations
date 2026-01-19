package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationCargoDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationCargoDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationCargoDtlRepository extends JpaRepository<PortCallOperationCargoDtl, PortCallOperationCargoDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);

    List<PortCallOperationCargoDtl> findByTransactionPoid(Long transactionPoid);

    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationCargoDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
