package com.asg.operations.pdaporttariffmaster.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChargeDetailsResponse {

    private Long transactionPoid;
    private List<PdaPortTariffChargeDetailResponse> chargeDetails;

}
