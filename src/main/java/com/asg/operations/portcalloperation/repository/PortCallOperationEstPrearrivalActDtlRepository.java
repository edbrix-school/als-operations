package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationEstPrearrivalActDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationEstPrearrivalActDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationEstPrearrivalActDtlRepository extends JpaRepository<PortCallOperationEstPrearrivalActDtl, PortCallOperationEstPrearrivalActDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);

    void deleteByTransactionPoidAndDetRowId(Long transactionPoid, Long detRowId);

    List<PortCallOperationEstPrearrivalActDtl> findByTransactionPoid(Long transactionPoid);

    List<PortCallOperationEstPrearrivalActDtl> findByTransactionPoidOrderByLastModifiedDateDesc(Long transactionPoid);

    List<PortCallOperationEstPrearrivalActDtl> findByTransactionPoidAndDetRowIdOrderByPreActivityDtlPoidAsc(Long transactionPoid, Long detRowId);

    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationEstPrearrivalActDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);

    @Query("select coalesce(max(d.preActivityDtlPoid), 0) from PortCallOperationEstPrearrivalActDtl d where d.transactionPoid = :transactionPoid and d.detRowId = :detRowId")
    Long findMaxPreActivityDtlPoidByTransactionPoidAndDetRowId(@Param("transactionPoid") Long transactionPoid, @Param("detRowId") Long detRowId);
}
