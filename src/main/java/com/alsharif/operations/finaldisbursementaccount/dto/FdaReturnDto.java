package com.alsharif.operations.finaldisbursementaccount.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FdaReturnDto {
    @NotBlank(message = "Correction remarks are required when returning FDA")
    private String correctionRemarks;
}
