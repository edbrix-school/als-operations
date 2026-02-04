package com.asg.operations.salesquotationprojects.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TempAddressProcedureResponse {
    private boolean success;
    private String message;
    private String errorMessage;
    private Long newAddressPoid;
}