package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobChargesDTO {
    private List<ChargeDTO> charges;
    private BigDecimal totalBuyingCharge;
    private BigDecimal totalSellingCharge;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;
    private BigDecimal margin;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargeSummaryDTO {
        private Long chargePoid;
        private String chargeCode;
        private String chargeDescription;
        private BigDecimal amount;
        private String currency;
        private String type;
    }
}
