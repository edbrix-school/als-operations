package com.asg.operations.finaldisbursementaccount.repository;

import com.asg.operations.finaldisbursementaccount.entity.PdaFdaDtl;
import com.asg.operations.finaldisbursementaccount.key.PdaFdaDtlId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PdaFdaDtlRepository extends JpaRepository<PdaFdaDtl, PdaFdaDtlId> {

    List<PdaFdaDtl> findByIdTransactionPoid(Long transactionPoid);

    @Query("""
            SELECT d
            FROM PdaFdaDtl d
            WHERE d.id.transactionPoid = :transactionPoid
            ORDER BY d.seqNo ASC, d.id.detRowId ASC
            """)
    Page<PdaFdaDtl> findByTransactionPoid(@Param("transactionPoid") Long transactionPoid, Pageable pageable);

    @Query("SELECT MAX(d.id.detRowId) FROM PdaFdaDtl d WHERE d.id.transactionPoid = :transactionPoid")
    Long findMaxDetRowId(@Param("transactionPoid") Long transactionPoid);
}
