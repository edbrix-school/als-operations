package com.alsharif.operations.pdaentryform.repository;

import com.alsharif.operations.pdaentryform.entity.PdaEntryTdrDetail;
import com.alsharif.operations.pdaentryform.entity.PdaEntryTdrDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PDA Entry TDR Detail entity
 */
@Repository
public interface PdaEntryTdrDetailRepository extends JpaRepository<PdaEntryTdrDetail, PdaEntryTdrDetailId> {

    /**
     * Find all TDR details for a transaction, ordered by row ID
     */
    List<PdaEntryTdrDetail> findByTransactionPoidOrderByDetRowIdAsc(Long transactionPoid);

    /**
     * Delete all TDR details for a transaction
     */
    @Modifying
    @Query("DELETE FROM PdaEntryTdrDetail d WHERE d.transactionPoid = :transactionPoid")
    void deleteByTransactionPoid(@Param("transactionPoid") Long transactionPoid);

    /**
     * Count TDR details for a transaction
     */
    long countByTransactionPoid(Long transactionPoid);
}
