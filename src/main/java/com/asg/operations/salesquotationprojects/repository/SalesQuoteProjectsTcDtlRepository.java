package com.asg.operations.salesquotationprojects.repository;

import com.asg.operations.salesquotationprojects.entity.SalesQuoteProjectsTcDtl;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsTcDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalesQuoteProjectsTcDtlRepository extends JpaRepository<SalesQuoteProjectsTcDtl, SalesQuoteProjectsTcDtlId> {
    List<SalesQuoteProjectsTcDtl> findByIdTransactionPoid(Long transactionPoid);
    
    @Query("SELECT COALESCE(MAX(t.id.detRowId), 0) FROM SalesQuoteProjectsTcDtl t WHERE t.id.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}