package com.asg.operations.finaldisbursementaccount.util;

import com.asg.operations.finaldisbursementaccount.dto.FdaChargeDto;
import com.asg.operations.finaldisbursementaccount.dto.FdaHeaderDto;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaDtl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalculationUtils {

    public static BigDecimal zero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    public static void recalculateAmounts(PdaFdaDtl entity) {
        BigDecimal qty = zero(entity.getQty());
        BigDecimal days = zero(entity.getDays());
        BigDecimal rate = zero(entity.getPdaRate());
        BigDecimal currRate = zero(entity.getCurrencyRate());

        BigDecimal base = qty.multiply(days).multiply(rate);
        BigDecimal amount = currRate.compareTo(BigDecimal.ZERO) > 0 ? base.multiply(currRate) : base;

        entity.setAmount(amount);

        if (entity.getFdaAmount() == null) {
            entity.setFdaAmount(amount);
        }
        if (entity.getCostAmount() == null) {
            entity.setCostAmount(BigDecimal.ZERO);
        }
    }


    public static void computeProfitLossRuntime(List<FdaChargeDto> charges, FdaHeaderDto headerDto) {
        BigDecimal profitTotal = BigDecimal.ZERO;
        BigDecimal lossTotal = BigDecimal.ZERO;  // negative or zero
        BigDecimal totalFdaAmount = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (FdaChargeDto d : charges) {
            BigDecimal fdaAmt = zero(d.getFdaAmount() != null ? d.getFdaAmount() : d.getAmount());
            BigDecimal costAmt = zero(d.getCostAmount());

            BigDecimal pl = fdaAmt.subtract(costAmt);
            d.setProfitLoss(pl);

            if (costAmt.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal per = pl.multiply(BigDecimal.valueOf(100))
                        .divide(costAmt, 2, RoundingMode.HALF_UP);
                d.setProfitLossPer(per);
            } else {
                d.setProfitLossPer(BigDecimal.ZERO);
            }

            if (headerDto != null) {
                if (pl.compareTo(BigDecimal.ZERO) > 0) {
                    profitTotal = profitTotal.add(pl);
                } else if (pl.compareTo(BigDecimal.ZERO) < 0) {
                    lossTotal = lossTotal.add(pl);  // keep negative
                }
                totalFdaAmount = totalFdaAmount.add(fdaAmt);
                totalCost = totalCost.add(costAmt);
            }
        }

        if (headerDto != null) {
            headerDto.setProfitTotal(profitTotal);
            headerDto.setLossTotal(lossTotal);

            BigDecimal headerPl = profitTotal.add(lossTotal);  // lossTotal already negative
            headerDto.setProfitLossAmount(headerPl);
            // Keep GET response consistent with charges: legacy total amount = sum(FdaAmount)
            headerDto.setTotalAmount(totalFdaAmount);

            // Legacy: totalProfitLossPercentage = (ProfitLossAmount / FdaAmount rounded to 2) * 100, plus '%' suffix
            // If FdaAmount == 0 => "0%"
            if (totalFdaAmount.compareTo(BigDecimal.ZERO) == 0) {
                headerDto.setProfitLossPer("0%");
            } else {
                BigDecimal ratio = headerPl.divide(totalFdaAmount, 2, RoundingMode.HALF_UP);
                BigDecimal per = ratio.multiply(BigDecimal.valueOf(100));
                headerDto.setProfitLossPer(per.toString() + "%");
            }
        }
    }
}
