package com.asg.operations.salesquotationprojects.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesQuoteProjectsTcDetailRequest {
    private Long detRowId;
    
    @Size(max = 50, message = "Clause reference cannot exceed 50 characters")
    private String clauseRef;
    
    @Size(max = 500, message = "Terms description cannot exceed 500 characters")
    private String termsDescription;
    
    private ActionType actionType;
}