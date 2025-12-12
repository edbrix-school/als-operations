package com.asg.operations.pdaentryform.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for PDA Entry TDR Detail
 * Note: Contains all fields from Request plus audit fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEntryTdrDetailResponse {

    private Long transactionPoid;
    private Long detRowId;
    private String mlo;
    private String pol;
    private String slot;
    private String subSlot;
    private String disch20fl;
    private String disch20mt;
    private String disch40fl;
    private String disch40mt;
    private String disch45fl;
    private String disch45mt;
    private String dischTot20;
    private String dischTot40;
    private String dischTot45;
    private String load20fl;
    private String load20mt;
    private String load40fl;
    private String load40mt;
    private String load45fl;
    private String load45mt;
    private String loadTot20;
    private String loadTot40;
    private String loadTot45;
    private String loadAlm20;
    private String loadAlm40;
    private String loadAlm45;
    private String full20dc;
    private String full20tk;
    private String full20fr;
    private String full20ot;
    private String full40dc;
    private String full40ot;
    private String full40fr;
    private String full40rf;
    private String full40rh;
    private String full40hc;
    private String full45;
    private String dg20dc;
    private String dg20tk;
    private String dg40dc;
    private String dg40hc;
    private String dg20rf;
    private String dg40rf;
    private String dg40hr;
    private String oog20ot;
    private String oog20fr;
    private String oog40ot;
    private String oog40fr;
    private String mt20dc;
    private String mt20tk;
    private String mt20fr;
    private String mt20ot;
    private String mt40dc;
    private String mt40ot;
    private String mt40fr;
    private String mt40rf;
    private String mt40rh;
    private String mt40hc;
    private String mt45;
    private String remarks;
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String lastModifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;
}

