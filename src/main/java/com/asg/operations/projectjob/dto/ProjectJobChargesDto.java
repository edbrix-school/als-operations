package com.asg.operations.projectjob.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectJobChargesDto {

    private Long detRowId;

    private BigDecimal chargePoid;
    private BigDecimal currencyExchange;
    private BigDecimal quantity;
    private BigDecimal buyingPercharge;
    private BigDecimal billingPrecharge;
    private BigDecimal paidAtPortPoid;


    @Size(max = 10,message = "Currency code must be less than or equal to 10 characters")
    private String currencyCode;
    private String payMode;
    private String rcptNoOld;
    private String chargeCodeOld;
    private LocalDateTime rcptDaeOld;
    private String costInvOld;

    private BigDecimal equipmentPoid;
    private BigDecimal totalBuyingCharge;
    private BigDecimal totalSellingCharge;

    private LocalDateTime costInvDtOld;
    private String rcptIvPoid;
    private BigDecimal totalCostBooked;

    private String dataRowId;
    private String costCurrency;
    private BigDecimal costCurrencyRate;
    private String costBookRef;
    private String printGroup;
    private String remarks;
    private String shChargeInv;

    private String unitType;

    private BigDecimal taxPoid;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal taxInputAmount;

    private String cnRefDocId;
    private String cnRefDocPoid;
    private String cnRefDetRowId;
    private String cnIssueInvoice;

    private BigDecimal houseBlPoid;
    private BigDecimal supplierPoid;

    private String chargeBasis;
    private String enteryLocation;

}