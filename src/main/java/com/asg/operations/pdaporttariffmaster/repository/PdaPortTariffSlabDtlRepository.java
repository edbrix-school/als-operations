package com.asg.operations.pdaporttariffmaster.repository;

import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffSlabDtl;
import com.asg.operations.pdaporttariffmaster.key.PdaPortTariffSlabDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PdaPortTariffSlabDtlRepository extends JpaRepository<PdaPortTariffSlabDtl, PdaPortTariffSlabDtlId> {
    
    @Modifying
    @Query("DELETE FROM PdaPortTariffSlabDtl s WHERE s.id.transactionPoid = :transactionPoid")
    void deleteByTransactionPoid(@Param("transactionPoid") Long transactionPoid);

    @Query("SELECT s FROM PdaPortTariffSlabDtl s WHERE s.id.transactionPoid = :transactionPoid AND s.id.chargeDetRowId = :chargeDetRowId ORDER BY s.id.detRowId ASC")
    List<PdaPortTariffSlabDtl> findByTransactionPoidAndChargeDetRowIdOrderByDetRowIdAsc(@Param("transactionPoid") Long transactionPoid, @Param("chargeDetRowId") Long chargeDetRowId);

    @Modifying
    @Query("DELETE FROM PdaPortTariffSlabDtl s WHERE s.id.transactionPoid = :transactionPoid AND s.id.chargeDetRowId = :chargeDetRowId")
    void deleteByTransactionPoidAndChargeDetRowId(@Param("transactionPoid") Long transactionPoid, @Param("chargeDetRowId") Long chargeDetRowId);
}