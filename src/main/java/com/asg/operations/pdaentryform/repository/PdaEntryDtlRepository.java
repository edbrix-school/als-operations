package com.asg.operations.pdaentryform.repository;

import com.asg.operations.pdaentryform.entity.PdaEntryDtl;
import com.asg.operations.pdaentryform.entity.PdaEntryDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for PDA Entry Charge Detail entity
 */
@Repository
public interface PdaEntryDtlRepository extends JpaRepository<PdaEntryDtl, PdaEntryDtlId> {

    /**
     * Find all charge details for a transaction, ordered by sequence number and row ID
     */
    List<PdaEntryDtl> findByTransactionPoidOrderBySeqnoAscDetRowIdAsc(Long transactionPoid);

    /**
     * Delete all charge details for a transaction
     */
    @Modifying
    @Query("DELETE FROM PdaEntryDtl d WHERE d.transactionPoid = :transactionPoid")
    void deleteByTransactionPoid(@Param("transactionPoid") Long transactionPoid);

    /**
     * Calculate total amount for a transaction
     */
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM PdaEntryDtl d WHERE d.transactionPoid = :transactionPoid")
    BigDecimal calculateTotalAmount(@Param("transactionPoid") Long transactionPoid);

    /**
     * Count charge details for a transaction
     */
    long countByTransactionPoid(Long transactionPoid);
}
