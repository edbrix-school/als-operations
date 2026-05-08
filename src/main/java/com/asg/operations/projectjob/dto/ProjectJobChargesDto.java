package com.asg.operations.projectjob.dto;

import com.asg.common.lib.dto.LovGetListDto;
import com.asg.common.lib.dto.LovGetListDto;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectJobChargesDto {

    private Long detRowId;

    private Long chargePoid;
    private LovGetListDto chargesLov;
    private BigDecimal currencyExchange;
    private BigDecimal quantity;
    private BigDecimal buyingPercharge;
    private BigDecimal billingPrecharge;
    private Long paidAtPortPoid;


    @Size(max = 10,message = "Currency code must be less than or equal to 10 characters")
    private String currencyCode;

    @Size(max=1,message = "Pay Mode must be exactly 1 character")
    private String payMode;

    @Size(max = 100, message = "Receipt number (old) must not exceed 100 characters")
    private String rcptNoOld;

    @Size(max=20,message = "Charge Code (old) must be less than or equal to 20 characters")
    private String chargeCodeOld;
    private LocalDateTime rcptDaeOld;

    @Size(max = 100, message = "Cost Invoice (old) must not exceed 100 characters")
    private String costInvOld;

    private Long equipmentPoid;
    private BigDecimal totalBuyingCharge;
    private BigDecimal totalSellingCharge;

    private LocalDateTime costInvDtOld;

    @Size(max = 100, message = "Receipt invoice POID must not exceed 100 characters")
    private String rcptIvPoid;

    private BigDecimal totalCostBooked;

    @Size(max = 10, message = "Data row ID must not exceed 10 characters")
    private String dataRowId;

    @Size(max = 10, message = "Cost currency must not exceed 10 characters")
    private String costCurrency;

    private BigDecimal costCurrencyRate;

    @Size(max = 100, message = "Cost book reference must not exceed 100 characters")
    private String costBookRef;

    @Size(max = 50, message = "Print group must not exceed 50 characters")
    private String printGroup;

    @Size(max = 200, message = "Remarks must not exceed 200 characters")
    private String remarks;

    @Size(max = 25, message = "SH charge invoice must not exceed 25 characters")
    private String shChargeInv;

    @Size(max = 25, message = "Unit type must not exceed 25 characters")
    private String unitType;

    private Long taxPoid;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal taxInputAmount;

    @Size(max = 100, message = "CN reference document ID must not exceed 100 characters")
    private String cnRefDocId;

    @Size(max = 300, message = "CN reference document POID must not exceed 300 characters")
    private String cnRefDocPoid;

    @Size(max = 300, message = "CN reference detail row ID must not exceed 300 characters")
    private String cnRefDetRowId;

    @Size(max = 100, message = "CN issue invoice must not exceed 100 characters")
    private String cnIssueInvoice;

    private Long houseBlPoid;
    private LovGetListDto houseBlPoidLov;
    private Long supplierPoid;

    @Size(max = 20, message = "Charge basis must not exceed 20 characters")
    private String chargeBasis;

    @Size(max = 25, message = "Entry location must not exceed 25 characters")
    private String enteryLocation;

}