package com.asg.operations.salesquotationprojects.repository;

import com.asg.operations.salesquotationprojects.entity.SalesQuoteProjectsNotesDtl;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsNotesDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalesQuoteProjectsNotesDtlRepository extends JpaRepository<SalesQuoteProjectsNotesDtl, SalesQuoteProjectsNotesDtlId> {
    List<SalesQuoteProjectsNotesDtl> findByIdTransactionPoid(Long transactionPoid);
    
    @Query("SELECT COALESCE(MAX(n.id.detRowId), 0) FROM SalesQuoteProjectsNotesDtl n WHERE n.id.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}