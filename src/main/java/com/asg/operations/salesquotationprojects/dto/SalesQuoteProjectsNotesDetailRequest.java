package com.asg.operations.salesquotationprojects.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesQuoteProjectsNotesDetailRequest {
    private Long detRowId;
    
    @Size(max = 300, message = "Notes cannot exceed 300 characters")
    private String notes;
    
    private ActionType actionType;
}