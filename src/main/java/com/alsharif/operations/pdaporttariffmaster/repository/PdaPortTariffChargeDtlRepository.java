package com.alsharif.operations.pdaporttariffmaster.repository;

import com.alsharif.operations.pdaporttariffmaster.entity.PdaPortTariffChargeDtl;
import com.alsharif.operations.pdaporttariffmaster.key.PdaPortTariffChargeDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PdaPortTariffChargeDtlRepository extends JpaRepository<PdaPortTariffChargeDtl, PdaPortTariffChargeDtlId> {
    
    @Query("SELECT c FROM PdaPortTariffChargeDtl c WHERE c.id.transactionPoid = :transactionPoid ORDER BY c.seqNo ASC, c.id.detRowId ASC")
    List<PdaPortTariffChargeDtl> findByTransactionPoidOrderBySeqNoAscDetRowIdAsc(@Param("transactionPoid") Long transactionPoid);

    @Modifying
    @Query("DELETE FROM PdaPortTariffChargeDtl c WHERE c.id.transactionPoid = :transactionPoid")
    void deleteByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
