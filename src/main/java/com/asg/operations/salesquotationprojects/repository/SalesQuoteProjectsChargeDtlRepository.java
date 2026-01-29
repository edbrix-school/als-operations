package com.asg.operations.salesquotationprojects.repository;

import com.asg.operations.salesquotationprojects.entity.SalesQuoteProjectsChargeDtl;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsChargeDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalesQuoteProjectsChargeDtlRepository extends JpaRepository<SalesQuoteProjectsChargeDtl, SalesQuoteProjectsChargeDtlId> {
    List<SalesQuoteProjectsChargeDtl> findByIdTransactionPoid(Long transactionPoid);
    
    @Query("SELECT COALESCE(MAX(c.id.detRowId), 0) FROM SalesQuoteProjectsChargeDtl c WHERE c.id.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}