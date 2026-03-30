package com.asg.operations.pdaporttariffmaster.repository;

import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffHdr;
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

    @Query("SELECT COUNT(h) > 0 FROM PdaPortTariffHdr h " +
            "WHERE h.groupPoid = :groupPoid " +
            "AND h.deleted = 'N' " +
            "AND h.transactionPoid != COALESCE(:excludeTransactionPoid, -1) " +
            "AND h.periodFrom <= :periodTo " +
            "AND h.periodTo >= :periodFrom " +
            "AND (:ports IS NULL OR h.ports LIKE CONCAT('%', :ports, '%')) " +
            "AND (:vesselTypes IS NULL OR h.vesselTypes LIKE CONCAT('%', :vesselTypes, '%'))")
    boolean existsOverlappingPeriod(
            @Param("groupPoid") Long groupPoid,
            @Param("excludeTransactionPoid") Long excludeTransactionPoid,
            @Param("periodFrom") LocalDate periodFrom,
            @Param("periodTo") LocalDate periodTo,
            @Param("ports") String ports,
            @Param("vesselTypes") String vesselTypes
    );
}