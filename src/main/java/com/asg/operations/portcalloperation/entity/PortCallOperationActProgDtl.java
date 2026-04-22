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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PC_ACT_PROG_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationActProgDtlId.class)
public class PortCallOperationActProgDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "EMAIL_POID")
    private Long emailPoid;

    @Column(name = "CARGO", length = 300)
    private String cargo;

    @Column(name = "PROGRESS_DATE_TIME")
    private LocalDateTime progressDateTime;

    @Column(name = "PROGRESS_QTY")
    private BigDecimal progressQty;

    @Column(name = "PROGRESS_STATUS", length = 100)
    private String progressStatus;

    @Column(name = "BALANCE_QTY")
    private BigDecimal balanceQty;

    @Column(name = "UNIT_POID")
    private Long unitPoid;

    @Column(name = "RATE_PER_HR")
    private BigDecimal ratePerHr;

    @Column(name = "ETC")
    private LocalDateTime etc;

    @Column(name = "EST_BL_DATE")
    private LocalDateTime estBlDate;
}
