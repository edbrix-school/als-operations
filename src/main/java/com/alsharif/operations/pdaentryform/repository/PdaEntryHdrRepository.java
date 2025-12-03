package com.alsharif.operations.pdaentryform.repository;

import com.alsharif.operations.pdaentryform.entity.PdaEntryHdr;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository interface for PDA Entry Header entity
 */
@Repository
public interface PdaEntryHdrRepository extends JpaRepository<PdaEntryHdr, Long> {

    /**
     * Find by transaction POID with group and company filter
     */
    @Query("SELECT h FROM PdaEntryHdr h " +
            "WHERE h.transactionPoid = :transactionPoid " +
            "AND (:groupPoid IS NULL OR h.groupPoid = :groupPoid) " +
            "AND (:companyPoid IS NULL OR h.companyPoid = :companyPoid) " +
            "AND h.deleted = 'N'")
    Optional<PdaEntryHdr> findByTransactionPoidAndFilters(
            @Param("transactionPoid") Long transactionPoid,
            @Param("groupPoid") BigDecimal groupPoid,
            @Param("companyPoid") BigDecimal companyPoid
    );

    /**
     * Check if document reference exists
     */
    boolean existsByDocRef(String docRef);

    /**
     * Find by document reference
     */
    Optional<PdaEntryHdr> findByDocRef(String docRef);

    /**
     * Search PDA entries with filters
     */
    @Query("SELECT h FROM PdaEntryHdr h " +
            "WHERE (:groupPoid IS NULL OR h.groupPoid = :groupPoid) " +
            "AND (:companyPoid IS NULL OR h.companyPoid = :companyPoid) " +
            "AND h.deleted = COALESCE(:deleted, 'N') " +
            "AND (:docRef IS NULL OR UPPER(h.docRef) LIKE UPPER(CONCAT('%', :docRef, '%'))) " +
            "AND (:transactionRef IS NULL OR UPPER(h.transactionRef) LIKE UPPER(CONCAT('%', :transactionRef, '%'))) " +
            "AND (:principalPoid IS NULL OR h.principalPoid = :principalPoid) " +
            "AND (:status IS NULL OR h.status = :status) " +
            "AND (:refType IS NULL OR h.refType = :refType) " +
            "AND (:vesselPoid IS NULL OR h.vesselPoid = :vesselPoid) " +
            "AND (:portPoid IS NULL OR h.portPoid = :portPoid) " +
            "AND (:transactionDateFrom IS NULL OR h.transactionDate >= :transactionDateFrom) " +
            "AND (:transactionDateTo IS NULL OR h.transactionDate <= :transactionDateTo)")
    Page<PdaEntryHdr> searchPdaEntries(
            @Param("groupPoid") BigDecimal groupPoid,
            @Param("companyPoid") BigDecimal companyPoid,
            @Param("deleted") String deleted,
            @Param("docRef") String docRef,
            @Param("transactionRef") String transactionRef,
            @Param("principalPoid") BigDecimal principalPoid,
            @Param("status") String status,
            @Param("refType") String refType,
            @Param("vesselPoid") BigDecimal vesselPoid,
            @Param("portPoid") BigDecimal portPoid,
            @Param("transactionDateFrom") LocalDate transactionDateFrom,
            @Param("transactionDateTo") LocalDate transactionDateTo,
            Pageable pageable
    );

    /**
     * Check if transaction can be edited (not confirmed/closed and not principal approved for GENERAL)
     */
    @Query("SELECT CASE WHEN (h.status IN ('CONFIRMED', 'CLOSED') OR " +
            "(h.refType = 'GENERAL' AND h.principalApproved = 'Y')) THEN false ELSE true END " +
            "FROM PdaEntryHdr h " +
            "WHERE h.transactionPoid = :transactionPoid " +
            "AND (:groupPoid IS NULL OR h.groupPoid = :groupPoid) " +
            "AND (:companyPoid IS NULL OR h.companyPoid = :companyPoid) " +
            "AND h.deleted = 'N'")
    boolean isEditable(
            @Param("transactionPoid") Long transactionPoid,
            @Param("groupPoid") BigDecimal groupPoid,
            @Param("companyPoid") BigDecimal companyPoid
    );

    /**
     * Find maximum sequence number for a given prefix and group POID
     * Used for document reference generation (format: PDA-YYYY-NNNNN)
     */
    @Query(value = "SELECT MAX(TO_NUMBER(SUBSTR(DOC_REF, -5))) " +
            "FROM PDA_ENTRY_HDR " +
            "WHERE DOC_REF LIKE :prefix || '%' " +
            "AND GROUP_POID = :groupPoid " +
            "AND DELETED = 'N'", nativeQuery = true)
    Integer findMaxSequenceByPrefixAndGroup(
            @Param("prefix") String prefix,
            @Param("groupPoid") BigDecimal groupPoid
    );
}


