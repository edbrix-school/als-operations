package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationDocsCopyDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationDocsCopyDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationDocsCopyDtlRepository extends JpaRepository<PortCallOperationDocsCopyDtl, PortCallOperationDocsCopyDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);

    List<PortCallOperationDocsCopyDtl> findByTransactionPoid(Long transactionPoid);

    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationDocsCopyDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
