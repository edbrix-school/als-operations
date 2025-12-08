package com.asg.operations.pdaentryform.repository;

import com.asg.operations.pdaentryform.entity.PdaEntryAcknowledgmentDtl;
import com.asg.operations.pdaentryform.entity.PdaEntryAcknowledgmentDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PDA Entry Acknowledgment Detail entity
 */
@Repository
public interface PdaEntryAcknowledgmentDtlRepository extends JpaRepository<PdaEntryAcknowledgmentDtl, PdaEntryAcknowledgmentDtlId> {

    /**
     * Find all acknowledgment details for a transaction, ordered by row ID
     */
    List<PdaEntryAcknowledgmentDtl> findByTransactionPoidOrderByDetRowIdAsc(Long transactionPoid);

    /**
     * Delete all acknowledgment details for a transaction
     */
    @Modifying
    @Query("DELETE FROM PdaEntryAcknowledgmentDtl d WHERE d.transactionPoid = :transactionPoid")
    void deleteByTransactionPoid(@Param("transactionPoid") Long transactionPoid);

    /**
     * Count acknowledgment details for a transaction
     */
    long countByTransactionPoid(Long transactionPoid);

}

