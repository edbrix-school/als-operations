package com.alsharif.operations.pdaporttariffmaster.repository;

import com.alsharif.operations.pdaporttariffmaster.entity.PdaPortTariffHdr;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PdaPortTariffHdrRepository extends JpaRepository<PdaPortTariffHdr, Long> {
    Optional<PdaPortTariffHdr> findByTransactionPoid(Long transactionPoid);

    @Query(value = """
        SELECT MAX(TO_NUMBER(SUBSTR(DOC_REF, LENGTH(:prefix) + 1)))
        FROM PDA_PORT_TARIFF_HDR
        WHERE DOC_REF LIKE :prefix || '%'
          AND GROUP_POID = :groupPoid
    """, nativeQuery = true)
    Integer findMaxSequence(@Param("prefix") String prefix,
                            @Param("groupPoid") BigDecimal groupPoid);

    Optional<PdaPortTariffHdr> findByTransactionPoidAndGroupPoid(Long transactionPoid, BigDecimal groupPoid);

    Optional<PdaPortTariffHdr> findByTransactionPoidAndGroupPoidAndDeleted(Long transactionPoid, BigDecimal groupPoid, String deleted);

    @Query("SELECT h FROM PdaPortTariffHdr h " +
            "WHERE h.groupPoid = :groupPoid " +
            "AND h.deleted = 'N' " +
            "AND (:portPoid IS NULL OR h.ports LIKE CONCAT('%', :portPoid, '%')) " +
            "AND (:periodFrom IS NULL OR h.periodTo >= :periodFrom) " +
            "AND (:periodTo IS NULL OR h.periodFrom <= :periodTo) " +
            "AND (:vesselTypePoid IS NULL OR h.vesselTypes LIKE CONCAT('%', :vesselTypePoid, '%'))")
    Page<PdaPortTariffHdr> searchTariffs(
            @Param("groupPoid") BigDecimal groupPoid,
            @Param("portPoid") String portPoid,
            @Param("periodFrom") LocalDate periodFrom,
            @Param("periodTo") LocalDate periodTo,
            @Param("vesselTypePoid") String vesselTypePoid,
            Pageable pageable
    );

    @Query("SELECT COUNT(h) > 0 FROM PdaPortTariffHdr h " +
            "WHERE h.groupPoid = :groupPoid " +
            "AND h.deleted = 'N' " +
            "AND h.transactionPoid != COALESCE(:excludeTransactionPoid, -1) " +
            "AND h.periodFrom <= :periodTo " +
            "AND h.periodTo >= :periodFrom " +
            "AND (:ports IS NULL OR h.ports LIKE CONCAT('%', :ports, '%')) " +
            "AND (:vesselTypes IS NULL OR h.vesselTypes LIKE CONCAT('%', :vesselTypes, '%'))")
    boolean existsOverlappingPeriod(
            @Param("groupPoid") BigDecimal groupPoid,
            @Param("excludeTransactionPoid") Long excludeTransactionPoid,
            @Param("periodFrom") LocalDate periodFrom,
            @Param("periodTo") LocalDate periodTo,
            @Param("ports") String ports,
            @Param("vesselTypes") String vesselTypes
    );
}