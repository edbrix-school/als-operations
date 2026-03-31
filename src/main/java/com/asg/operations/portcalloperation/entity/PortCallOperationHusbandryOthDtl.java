package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "OPS_PC_HUSBANDRY_OTH_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationHusbandryOthDtlId.class)
public class PortCallOperationHusbandryOthDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "ARRANGEMENT", length = 300)
    private String arrangement;

    @Column(name = "DESCRIPTION_TEXT", length = 300)
    private String descriptionText;

    @Column(name = "MEET_GREET", length = 1)
    private String meetGreet;

    @Column(name = "NO_OF_DAYS")
    private BigDecimal noOfDays;

    @Column(name = "QTY")
    private BigDecimal qty;

    @Column(name = "UNIT_POID")
    private Long unitPoid;

    @Column(name = "UNIT_PRICE")
    private BigDecimal unitPrice;

    @Column(name = "CURRENCY_CODE", length = 50)
    private String currencyCode;

    @Column(name = "TOTAL_PRICE")
    private BigDecimal totalPrice;

    @Column(name = "ADJUSTED_PRICE")
    private BigDecimal adjustedPrice;

    @Column(name = "ARRNGMNT_ATTACHMENTS", length = 4000)
    private String arrngmntAttachments;

    @Column(name = "REQUESTED_BY", length = 300)
    private String requestedBy;

    @Column(name = "PAYMENT_MODE", length = 300)
    private String paymentMode;
}
