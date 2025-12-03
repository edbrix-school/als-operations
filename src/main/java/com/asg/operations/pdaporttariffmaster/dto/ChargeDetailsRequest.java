package com.asg.operations.pdaporttariffmaster.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChargeDetailsRequest {
    @NotNull(message = "Charge details list cannot be null")
    @NotEmpty(message = "At least one charge detail must be provided")
    @Valid
    private List<PdaPortTariffChargeDetailRequest> chargeDetails;

}
