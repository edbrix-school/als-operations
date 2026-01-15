package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationMailDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationMailDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationMailDtlRepository extends JpaRepository<PortCallOperationMailDtl, PortCallOperationMailDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);

    List<PortCallOperationMailDtl> findByTransactionPoid(Long transactionPoid);
    
    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationMailDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
