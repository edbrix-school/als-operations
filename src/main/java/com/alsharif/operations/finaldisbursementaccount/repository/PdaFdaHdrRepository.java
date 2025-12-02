package com.alsharif.operations.finaldisbursementaccount.repository;

import com.alsharif.operations.finaldisbursementaccount.entity.PdaFdaHdr;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PdaFdaHdrRepository extends JpaRepository<PdaFdaHdr, Long> {

    @Query("""
            SELECT h
            FROM PdaFdaHdr h
            WHERE h.groupPoid = :groupPoid
              AND h.companyPoid = :companyPoid
              AND h.deleted = 'N'
              AND (:transactionPoid IS NULL OR h.transactionPoid = :transactionPoid)
              AND (:etaFrom IS NULL OR h.arrivalDate >= :etaFrom)
              AND (:etaTo IS NULL OR h.arrivalDate <= :etaTo)
            """)
    Page<PdaFdaHdr> searchFdaHeaders(
            @Param("groupPoid") Long groupPoid,
            @Param("companyPoid") Long companyPoid,
            @Param("transactionPoid") Long transactionPoid,
            @Param("etaFrom") LocalDate etaFrom,
            @Param("etaTo") LocalDate etaTo,
            Pageable pageable
    );

    @Query(value = """
                SELECT MAX(TO_NUMBER(REGEXP_SUBSTR(DOC_REF, '[0-9]+$')))
                FROM PDA_FDA_HDR
                WHERE DOC_REF LIKE :prefix || '%'
                  AND REGEXP_LIKE(DOC_REF, :prefix || '[0-9]+$')
                  AND GROUP_POID = :groupPoid
            """, nativeQuery = true)
    Integer findMaxSequence(@Param("prefix") String prefix, @Param("groupPoid") Long groupPoid);


    Optional<PdaFdaHdr> findByTransactionPoidAndGroupPoidAndCompanyPoid(Long transactionPoid, Long groupPoid, Long companyPoid);

    Optional<PdaFdaHdr> findByTransactionPoidAndGroupPoidAndCompanyPoidAndDeleted(Long transactionPoid, Long groupPoid, Long companyPoid, String deleted);

    List<PdaFdaHdr> findByPdaRefAndGroupPoidAndCompanyPoidAndDeleted(String pdaRef, Long groupPoid, Long companyPoid, String deleted);

}
