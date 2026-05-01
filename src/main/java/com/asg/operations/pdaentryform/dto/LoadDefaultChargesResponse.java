package com.asg.operations.pdaentryform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoadDefaultChargesResponse {
    private String message;
    private List<PdaEntryChargeDetailResponse> chargeDetails;
}
