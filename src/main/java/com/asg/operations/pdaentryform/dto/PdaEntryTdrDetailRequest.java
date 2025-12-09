package com.asg.operations.pdaentryform.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating PDA Entry TDR Detail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEntryTdrDetailRequest {

    private Long detRowId; // null for new, existing value for update

    @Size(max = 50)
    private String mlo;

    @Size(max = 50)
    private String pol;

    @Size(max = 50)
    private String slot;

    @Size(max = 50)
    private String subSlot;

    // Discharge fields
    @Size(max = 50)
    private String disch20fl;

    @Size(max = 50)
    private String disch20mt;

    @Size(max = 50)
    private String disch40fl;

    @Size(max = 50)
    private String disch40mt;

    @Size(max = 50)
    private String disch45fl;

    @Size(max = 50)
    private String disch45mt;

    @Size(max = 50)
    private String dischTot20;

    @Size(max = 50)
    private String dischTot40;

    @Size(max = 50)
    private String dischTot45;

    // Load fields
    @Size(max = 50)
    private String load20fl;

    @Size(max = 50)
    private String load20mt;

    @Size(max = 50)
    private String load40fl;

    @Size(max = 50)
    private String load40mt;

    @Size(max = 50)
    private String load45fl;

    @Size(max = 50)
    private String load45mt;

    @Size(max = 50)
    private String loadTot20;

    @Size(max = 50)
    private String loadTot40;

    @Size(max = 50)
    private String loadTot45;

    @Size(max = 50)
    private String loadAlm20;

    @Size(max = 50)
    private String loadAlm40;

    @Size(max = 50)
    private String loadAlm45;

    // Full container fields
    @Size(max = 50)
    private String full20dc;

    @Size(max = 50)
    private String full20tk;

    @Size(max = 50)
    private String full20fr;

    @Size(max = 50)
    private String full20ot;

    @Size(max = 50)
    private String full40dc;

    @Size(max = 50)
    private String full40ot;

    @Size(max = 50)
    private String full40fr;

    @Size(max = 50)
    private String full40rf;

    @Size(max = 50)
    private String full40rh;

    @Size(max = 50)
    private String full40hc;

    @Size(max = 50)
    private String full45;

    // Dangerous Goods (DG) fields
    @Size(max = 50)
    private String dg20dc;

    @Size(max = 50)
    private String dg20tk;

    @Size(max = 50)
    private String dg40dc;

    @Size(max = 50)
    private String dg40hc;

    @Size(max = 50)
    private String dg20rf;

    @Size(max = 50)
    private String dg40rf;

    @Size(max = 50)
    private String dg40hr;

    // Out of Gauge (OOG) fields
    @Size(max = 50)
    private String oog20ot;

    @Size(max = 50)
    private String oog20fr;

    @Size(max = 50)
    private String oog40ot;

    @Size(max = 50)
    private String oog40fr;

    // Empty (MT) fields
    @Size(max = 50)
    private String mt20dc;

    @Size(max = 50)
    private String mt20tk;

    @Size(max = 50)
    private String mt20fr;

    @Size(max = 50)
    private String mt20ot;

    @Size(max = 50)
    private String mt40dc;

    @Size(max = 50)
    private String mt40ot;

    @Size(max = 50)
    private String mt40fr;

    @Size(max = 50)
    private String mt40rf;

    @Size(max = 50)
    private String mt40rh;

    @Size(max = 50)
    private String mt40hc;

    @Size(max = 50)
    private String mt45;

    @Size(max = 300)
    private String remarks;

}

