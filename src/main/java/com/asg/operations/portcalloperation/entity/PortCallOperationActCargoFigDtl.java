package com.asg.operations.portcalloperation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PC_ACT_CARGOFIG_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationActCargoFigDtlId.class)
public class PortCallOperationActCargoFigDtl {

    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "CARGO", length = 300)
    private String cargo;

    @Column(name = "CALL_TYPE", length = 500)
    private String callType;

    @Column(name = "QTY")
    private BigDecimal qty;

    @Column(name = "UNIT_POID")
    private Long unitPoid;

    @Column(name = "VESSEL_REQ", length = 500)
    private String vesselReq;

    @Column(name = "TERMINAL_NOM", length = 500)
    private String terminalNom;

    @Column(name = "SHIP_FIGURE_MT")
    private BigDecimal shipFigureMt;

    @Column(name = "SHORE_FIGURE_MT")
    private BigDecimal shoreFigureMt;

    @Column(name = "SHIP_FIGURE_BBLS")
    private BigDecimal shipFigureBbls;

    @Column(name = "BL_DATE")
    private LocalDateTime blDate;

    @Column(name = "HOSE_NO")
    private BigDecimal hoseNo;

    @Column(name = "HOSE_SIZE")
    private BigDecimal hoseSize;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
