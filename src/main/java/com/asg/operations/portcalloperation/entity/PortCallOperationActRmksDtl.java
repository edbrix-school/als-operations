package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PC_ACT_RMKS_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationActRmksDtlId.class)
public class PortCallOperationActRmksDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "REMARKS_TYPE", length = 100)
    private String remarksType;

    @Column(name = "REMARKS_FROM")
    private LocalDateTime remarksFrom;

    @Column(name = "REMARKS_TO")
    private LocalDate remarksTo;

    @Column(name = "CARGO_DETAILS", length = 100)
    private String cargoDetails;

    @Column(name = "REASON", length = 500)
    private String reason;

    @Column(name = "PC_REPORT_POID")
    private Long pcReportPoid;
}
