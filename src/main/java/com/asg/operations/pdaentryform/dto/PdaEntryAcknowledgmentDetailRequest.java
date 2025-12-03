package com.asg.operations.pdaentryform.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating/updating PDA Entry Acknowledgment Detail
 */
public class PdaEntryAcknowledgmentDetailRequest {

    private Long detRowId; // null for new, existing value for update

    @Size(max = 2000)
    private String particulars;

    @Size(max = 1)
    private String selected;

    @Size(max = 4000)
    private String remarks;

    // Getters and Setters

    public Long getDetRowId() {
        return detRowId;
    }

    public void setDetRowId(Long detRowId) {
        this.detRowId = detRowId;
    }

    public String getParticulars() {
        return particulars;
    }

    public void setParticulars(String particulars) {
        this.particulars = particulars;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}