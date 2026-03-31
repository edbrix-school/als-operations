package com.asg.operations.salesquotationprojects.repository;

import com.asg.operations.salesquotationprojects.entity.SalesQuoteProjectsHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalesQuoteProjectsHdrRepository extends JpaRepository<SalesQuoteProjectsHdr, Long> {
    @Query(value = "SELECT " +
            "qtn.TRANSACTION_POID, qtn.DOC_REF, qtn.TRANSACTION_DATE, " +
            "qtn.COMPANY_POID, qtn.CUSTOMER_POID, qtn.ADDRESS_POID, " +
            "qtn.CURRENCY_CODE, qtn.CURRENCY_RATE, " +
            "qtn.QUOTATION_STATUS, qtn.SALESMAN_POID, " +
            "qtn.VALIDITY_FROM_DATE, qtn.VALIDITY_TO_DATE, " +
            "qtn.PAYMENT_MODE, qtn.DELIVERY_TERMS, " +
            "qtn.LINE_POID, qtn.VESSEL_POID, qtn.VESSEL_NAME, " +
            "qtn.VOYAGE_REF, qtn.PORT_POID, qtn.PORT_DESCRIPTION, " +
            "qtn.REMARKS, qtn.TOTAL_DISCOUNT, qtn.TOTAL_AMOUNT, " +
            "qtn.ACTION_STATUS, qtn.ACTION_DUE_DATE, " +
            "qtn.ENQUIRY_REF_NUMBER, qtn.LOST_REASON, " +
            "qtn.BUSINESS_PROMOTION_VALUE, qtn.PERCENTAGE, " +
            "qtn.DETAILS, qtn.EXPEACTED_DELIVERY_DATE, " +
            "qtn.VESSEL_AGENT, qtn.QUOTED_RATE, qtn.RFQ_REF_NO, " +
            "qtn.PERCENTAGE_DISC, qtn.TOTAL_GP_AMT, " +
            "qtn.TOTAL_GP_PERCENTAGE, qtn.CUSTOMER_REF, " +
            "qtn.DELIVERY_TO_ADDRESS, qtn.SELECT_ALL_DTL, " +
            "qtn.TOT_AMT_PRINT_YN, qtn.DESCRIPTION_PRINT_YN, " +
            "qtn.ADVANCE_DETAIL, qtn.MTA_INV_POID, " +
            "qtn.SALES_INV_POID, qtn.SALES_INV_DOC_REF, " +
            "qtn.TOTAL_TAX, qtn.PARTY_ADDRESS_DETAILS, " +
            "qtn.DELETED, qtn.CREATED_BY, qtn.CREATED_DATE, " +
            "qtn.LASTMODIFIED_BY, qtn.LASTMODIFIED_DATE " +
            "FROM SALES_QUOTATION_HDR qtn " +
            "WHERE qtn.TRANSACTION_POID = :transactionPoid AND qtn.COMPANY_POID = :companyPoid", nativeQuery = true)
    List<Object[]> findSalesQuotationHeader(@Param("transactionPoid") Long transactionPoid,
                                               @Param("companyPoid") Long companyPoid);

}
