package com.alsharif.operations.pdaentryform.repository;

import com.alsharif.operations.pdaentryform.entity.PdaEntryVehicleDtl;
import com.alsharif.operations.pdaentryform.entity.PdaEntryVehicleDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PDA Entry Vehicle Detail entity
 */
@Repository
public interface PdaEntryVehicleDtlRepository extends JpaRepository<PdaEntryVehicleDtl, PdaEntryVehicleDtlId> {

    /**
     * Find all vehicle details for a transaction, ordered by row ID
     */
    List<PdaEntryVehicleDtl> findByTransactionPoidOrderByDetRowIdAsc(Long transactionPoid);

    /**
     * Delete all vehicle details for a transaction
     */
    @Modifying
    @Query("DELETE FROM PdaEntryVehicleDtl d WHERE d.transactionPoid = :transactionPoid")
    void deleteByTransactionPoid(@Param("transactionPoid") Long transactionPoid);

    /**
     * Count vehicle details for a transaction
     */
    long countByTransactionPoid(Long transactionPoid);
}