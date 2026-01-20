package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationActTimingsActvtyDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationActTimingsActvtyDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationActTimingsActvtyDtlRepository extends JpaRepository<PortCallOperationActTimingsActvtyDtl, PortCallOperationActTimingsActvtyDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);

    List<PortCallOperationActTimingsActvtyDtl> findByTransactionPoid(Long transactionPoid);

    List<PortCallOperationActTimingsActvtyDtl> findByTransactionPoidOrderByLastModifiedDateDesc(Long transactionPoid);

    List<PortCallOperationActTimingsActvtyDtl> findByTransactionPoidAndDetRowId(Long transactionPoid, Long detRowId);

    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationActTimingsActvtyDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);

    @Query("select coalesce(max(d.actualsTimingDtlPoid), 0) from PortCallOperationActTimingsActvtyDtl d where d.transactionPoid = :transactionPoid and d.detRowId = :detRowId")
    Long findMaxActualsTimingDtlPoidByTransactionPoidAndDetRowId(@Param("transactionPoid") Long transactionPoid, @Param("detRowId") Long detRowId);
}
