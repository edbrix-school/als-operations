package com.asg.operations.pdaentryform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEditValidationRequest {
    private Long pdaPoid;
}
