package com.asg.operations.salesquotationprojects.entity;

import com.asg.common.lib.entity.BaseEntity;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsChargeDtlId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "SALES_QUOTE_PROJECTS_CHARGE_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuoteProjectsChargeDtl extends BaseEntity {

    @EmbeddedId
    private SalesQuoteProjectsChargeDtlId id;

    @Column(name = "CHARGE_POID")
    private Long chargePoid;

    @Column(name = "PRINTABLE_CHARGE_DESC", length = 500)
    private String printableChargeDesc;

    @Column(name = "QUANTITY")
    private BigDecimal quantity;

    @Column(name = "UNIT_POID")
    private Long unitPoid;

    @Column(name = "BUY_CURRENCY_CODE", length = 20)
    private String buyCurrencyCode;

    @Column(name = "BUY_CURRENCY_RATE")
    private BigDecimal buyCurrencyRate;

    @Column(name = "BUY_UNIT_RATE")
    private BigDecimal buyUnitRate;

    @Column(name = "BUY_TOTAL_LC")
    private BigDecimal buyTotalLc;

    @Column(name = "SELL_UNIT_RATE_FC")
    private BigDecimal sellUnitRateFc;

    @Column(name = "SELL_TOTAL_FC")
    private BigDecimal sellTotalFc;

    @Column(name = "TAX_POID")
    private Long taxPoid;

    @Column(name = "TAX_PERCENTAGE")
    private BigDecimal taxPercentage;

    @Column(name = "TAX_AMOUNT_FC")
    private BigDecimal taxAmountFc;

    @Column(name = "SELL_GRAND_TOTAL_FC")
    private BigDecimal sellGrandTotalFc;

    @Column(name = "TAX_AMOUNT_LC")
    private BigDecimal taxAmountLc;

    @Column(name = "SELL_GRAND_TOTAL_LC")
    private BigDecimal sellGrandTotalLc;

    @Column(name = "REMARKS", length = 100)
    private String remarks;
}
