package com.asg.operations.finaldisbursementaccount.util;

import com.asg.operations.finaldisbursementaccount.dto.FdaChargeDto;
import com.asg.operations.finaldisbursementaccount.dto.FdaHeaderDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculationUtilsTest {

    @Test
    void profitLossPer_ShouldMatchLegacyRoundingAndPercentSuffix() {
        // Legacy: (ProfitLossAmount / FdaAmount rounded to 2) * 100, then append '%'
        // Example: fda=1000, cost=800 => pl=200
        // ratio = 200/1000 = 0.20 => *100 = 20.00 => "20.00%"
        FdaChargeDto c = new FdaChargeDto();
        c.setFdaAmount(BigDecimal.valueOf(1000));
        c.setCostAmount(BigDecimal.valueOf(800));

        FdaHeaderDto header = new FdaHeaderDto();
        CalculationUtils.computeProfitLossRuntime(List.of(c), header);

        assertEquals(BigDecimal.valueOf(1000), header.getTotalAmount());
        assertEquals("20.00%", header.getProfitLossPer());
    }

    @Test
    void profitLossPer_ShouldReturnZeroPercentWhenFdaAmountIsZero() {
        // Legacy: if FdaAmount == 0 => "0%"
        FdaChargeDto c = new FdaChargeDto();
        c.setFdaAmount(BigDecimal.ZERO);
        c.setCostAmount(BigDecimal.valueOf(50));

        FdaHeaderDto header = new FdaHeaderDto();
        CalculationUtils.computeProfitLossRuntime(List.of(c), header);

        assertEquals("0%", header.getProfitLossPer());
    }

    @Test
    void profitLossPer_ShouldSupportNegativeProfitLoss() {
        // fda=800, cost=1000 => pl=-200
        // ratio = -200/800 = -0.25 => *100 = -25.00 => "-25.00%"
        FdaChargeDto c = new FdaChargeDto();
        c.setFdaAmount(BigDecimal.valueOf(800));
        c.setCostAmount(BigDecimal.valueOf(1000));

        FdaHeaderDto header = new FdaHeaderDto();
        CalculationUtils.computeProfitLossRuntime(List.of(c), header);

        assertEquals("-25.00%", header.getProfitLossPer());
    }
}

