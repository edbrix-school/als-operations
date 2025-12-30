package com.asg.operations.pdaentryform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelPdaRequest {
    @NotBlank(message = "Cancel remark is mandatory")
    @Size(max = 500)
    private String cancelRemark;
}