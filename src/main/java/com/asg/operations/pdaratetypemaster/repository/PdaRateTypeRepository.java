package com.asg.operations.pdaratetypemaster.repository;



import com.asg.operations.pdaratetypemaster.entity.PdaRateTypeMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;


@Repository
public interface PdaRateTypeRepository extends JpaRepository<PdaRateTypeMaster, Long> {

    Optional<PdaRateTypeMaster> findByRateTypeCodeAndGroupPoid(String rateTypeCode, BigDecimal groupPoid);
    boolean existsByRateTypeCodeAndGroupPoid(String rateTypeCode, BigDecimal groupPoid);
    boolean existsByRateTypeNameAndGroupPoid(String rateTypeName, BigDecimal groupPoid);

    @Query("SELECT prtm FROM PdaRateTypeMaster prtm WHERE prtm.rateTypePoid = :rateTypeId " +
            "AND prtm.groupPoid = :groupPoid " +
            "AND (prtm.deleted IS NULL OR prtm.deleted = 'N')")
    Optional<PdaRateTypeMaster> findByRateTypePoidAndGroupPoid(
            @Param("rateTypeId") Long rateTypeId,
            @Param("groupPoid") BigDecimal groupPoid
    );

    @Query("SELECT prtm FROM PdaRateTypeMaster prtm WHERE " +
            "(:code IS NULL OR UPPER(prtm.rateTypeCode) LIKE UPPER(CONCAT('%', :code, '%'))) " +
            "AND (:name IS NULL OR UPPER(prtm.rateTypeName) LIKE UPPER(CONCAT('%', :name, '%'))) " +
            "AND (:active IS NULL OR prtm.active = :active) " +
            "AND (prtm.deleted IS NULL OR prtm.deleted = 'N') " +
            "AND prtm.groupPoid = :groupPoid")
    Page<PdaRateTypeMaster> searchRateTypes(
            @Param("code") String code,
            @Param("name") String name,
            @Param("active") String active,
            @Param("groupPoid") BigDecimal groupPoid,
            Pageable pageable
    );

    @Query("SELECT MAX(prtm.seqno) FROM PdaRateTypeMaster prtm WHERE prtm.groupPoid = :groupPoid")
    Optional<BigInteger> findMaxSeqnoByGroupPoid(@Param("groupPoid") BigDecimal groupPoid);

    boolean existsByRateTypePoidAndDeletedIgnoreCase(Long rateTypePoid, String deleted);
}