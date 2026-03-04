package com.asg.operations.pdaentryform.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "PDA_ENTRY_TDR_DETAIL")
@IdClass(PdaEntryTdrDetailId.class)
public class PdaEntryTdrDetail extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @NotNull
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @NotNull
    private Long detRowId;

    @Column(name = "MLO", length = 50)
    @Size(max = 50)
    private String mlo;

    @Column(name = "POL", length = 50)
    @Size(max = 50)
    private String pol;

    @Column(name = "SLOT", length = 50)
    @Size(max = 50)
    private String slot;

    @Column(name = "SUB_SLOT", length = 50)
    @Size(max = 50)
    private String subSlot;

    @Column(name = "DISCH_20FL", length = 50)
    @Size(max = 50)
    private String disch20fl;

    @Column(name = "DISCH_20MT", length = 50)
    @Size(max = 50)
    private String disch20mt;

    @Column(name = "DISCH_40FL", length = 50)
    @Size(max = 50)
    private String disch40fl;

    @Column(name = "DISCH_40MT", length = 50)
    @Size(max = 50)
    private String disch40mt;

    @Column(name = "DISCH_45FL", length = 50)
    @Size(max = 50)
    private String disch45fl;

    @Column(name = "DISCH_45MT", length = 50)
    @Size(max = 50)
    private String disch45mt;

    @Column(name = "DISCH_TOT_20", length = 50)
    @Size(max = 50)
    private String dischTot20;

    @Column(name = "DISCH_TOT_40", length = 50)
    @Size(max = 50)
    private String dischTot40;

    @Column(name = "DISCH_TOT_45", length = 50)
    @Size(max = 50)
    private String dischTot45;

    @Column(name = "LOAD_20FL", length = 50)
    @Size(max = 50)
    private String load20fl;

    @Column(name = "LOAD_20MT", length = 50)
    @Size(max = 50)
    private String load20mt;

    @Column(name = "LOAD_40FL", length = 50)
    @Size(max = 50)
    private String load40fl;

    @Column(name = "LOAD_40MT", length = 50)
    @Size(max = 50)
    private String load40mt;

    @Column(name = "LOAD_45FL", length = 50)
    @Size(max = 50)
    private String load45fl;

    @Column(name = "LOAD_45MT", length = 50)
    @Size(max = 50)
    private String load45mt;

    @Column(name = "LOAD_TOT_20", length = 50)
    @Size(max = 50)
    private String loadTot20;

    @Column(name = "LOAD_TOT_40", length = 50)
    @Size(max = 50)
    private String loadTot40;

    @Column(name = "LOAD_TOT_45", length = 50)
    @Size(max = 50)
    private String loadTot45;

    @Column(name = "LOAD_ALM_20", length = 50)
    @Size(max = 50)
    private String loadAlm20;

    @Column(name = "LOAD_ALM_40", length = 50)
    @Size(max = 50)
    private String loadAlm40;

    @Column(name = "LOAD_ALM_45", length = 50)
    @Size(max = 50)
    private String loadAlm45;

    @Column(name = "FULL_20DC", length = 50)
    @Size(max = 50)
    private String full20dc;

    @Column(name = "FULL_20TK", length = 50)
    @Size(max = 50)
    private String full20tk;

    @Column(name = "FULL_20FR", length = 50)
    @Size(max = 50)
    private String full20fr;

    @Column(name = "FULL_20OT", length = 50)
    @Size(max = 50)
    private String full20ot;

    @Column(name = "FULL_40DC", length = 50)
    @Size(max = 50)
    private String full40dc;

    @Column(name = "FULL_40OT", length = 50)
    @Size(max = 50)
    private String full40ot;

    @Column(name = "FULL_40FR", length = 50)
    @Size(max = 50)
    private String full40fr;

    @Column(name = "FULL_40RF", length = 50)
    @Size(max = 50)
    private String full40rf;

    @Column(name = "FULL_40RH", length = 50)
    @Size(max = 50)
    private String full40rh;

    @Column(name = "FULL_40HC", length = 50)
    @Size(max = 50)
    private String full40hc;

    @Column(name = "FULL_45", length = 50)
    @Size(max = 50)
    private String full45;

    @Column(name = "DG_20DC", length = 50)
    @Size(max = 50)
    private String dg20dc;

    @Column(name = "DG_20TK", length = 50)
    @Size(max = 50)
    private String dg20tk;

    @Column(name = "DG_40DC", length = 50)
    @Size(max = 50)
    private String dg40dc;

    @Column(name = "DG_40HC", length = 50)
    @Size(max = 50)
    private String dg40hc;

    @Column(name = "DG_20RF", length = 50)
    @Size(max = 50)
    private String dg20rf;

    @Column(name = "DG_40RF", length = 50)
    @Size(max = 50)
    private String dg40rf;

    @Column(name = "DG_40HR", length = 50)
    @Size(max = 50)
    private String dg40hr;

    @Column(name = "OOG_20OT", length = 50)
    @Size(max = 50)
    private String oog20ot;

    @Column(name = "OOG_20FR", length = 50)
    @Size(max = 50)
    private String oog20fr;

    @Column(name = "OOG_40OT", length = 50)
    @Size(max = 50)
    private String oog40ot;

    @Column(name = "OOG_40FR", length = 50)
    @Size(max = 50)
    private String oog40fr;

    @Column(name = "MT_20DC", length = 50)
    @Size(max = 50)
    private String mt20dc;

    @Column(name = "MT_20TK", length = 50)
    @Size(max = 50)
    private String mt20tk;

    @Column(name = "MT_20FR", length = 50)
    @Size(max = 50)
    private String mt20fr;

    @Column(name = "MT_20OT", length = 50)
    @Size(max = 50)
    private String mt20ot;

    @Column(name = "MT_40DC", length = 50)
    @Size(max = 50)
    private String mt40dc;

    @Column(name = "MT_40OT", length = 50)
    @Size(max = 50)
    private String mt40ot;

    @Column(name = "MT_40FR", length = 50)
    @Size(max = 50)
    private String mt40fr;

    @Column(name = "MT_40RF", length = 50)
    @Size(max = 50)
    private String mt40rf;

    @Column(name = "MT_40RH", length = 50)
    @Size(max = 50)
    private String mt40rh;

    @Column(name = "MT_40HC", length = 50)
    @Size(max = 50)
    private String mt40hc;

    @Column(name = "MT_45", length = 50)
    @Size(max = 50)
    private String mt45;

    @Column(name = "REMARKS", length = 300)
    @Size(max = 300)
    private String remarks;
}


