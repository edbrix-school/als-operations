package com.asg.operations.projects.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.operations.projects.key.FFProjectsChargesDtlId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECTS_CHARGES_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(FFProjectsChargesDtlId.class)
public class FFProjectsChargesDtl {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @AuditIgnore
    private Long detRowId;

    @Column(name = "QUOTATION_REFERENCE_POID")
    private Long quotationReferencePoid;

    @Column(name = "CHARGE_DETAILS_POID")
    private Long chargeDetailsPoid;

    @Column(name = "PRINTABLE_CHARGE_DESCRIPTION", length = 500)
    private String printableChargeDescription;

    @Column(name = "CHARGE_BASIS", length = 100)
    private String chargeBasis;

    @Column(name = "QUANTITY")
    private Double quantity;

    @Column(name = "UNIT", length = 50)
    private String unit;

    @Column(name = "BUYING_CURRENCY_CODE", length = 10)
    private String buyingCurrencyCode;

    @Column(name = "CURRENCY_RATE")
    private Double currencyRate;

    @Column(name = "BUYING_UNIT_RATE")
    private Double buyingUnitRate;

    @Column(name = "BUYING_TOTAL_BHD")
    private Double buyingTotalBhd;

    @Column(name = "SELLING_UNIT_RATE")
    private Double sellingUnitRate;

    @Column(name = "SELLING_TOTAL")
    private Double sellingTotal;

    @Column(name = "TAX_ID_POID")
    private Long taxIdPoid;

    @Column(name = "TAX_PERCENTAGE")
    private Double taxPercentage;

    @Column(name = "TAX_AMOUNT")
    private Double taxAmount;

    @Column(name = "SELLING_GRAND_TOTAL")
    private Double sellingGrandTotal;

    @Column(name = "SELLING_GRAND_TOTAL_BHD")
    private Double sellingGrandTotalBhd;

    @Column(name = "MARGIN_BHD")
    private Double marginBhd;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "DELETED", length = 1)
    @AuditIgnore
    private String deleted = "N";

    @Column(name = "CREATED_BY", length = 20)
    @AuditIgnore
    private String createdBy;

    @Column(name = "CREATED_DATE")
    @AuditIgnore
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    @AuditIgnore
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    @AuditIgnore
    private LocalDateTime lastModifiedDate;
}
