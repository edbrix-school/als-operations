package com.alsharif.operations.finaldisbursementaccount.util;

import com.alsharif.operations.finaldisbursementaccount.dto.FdaChargeDto;
import com.alsharif.operations.finaldisbursementaccount.dto.FdaHeaderDto;
import com.alsharif.operations.finaldisbursementaccount.entity.PdaFdaDtl;

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
        BigDecimal amount = currRate.compareTo(BigDecimal.ZERO) > 0
                ? base.multiply(currRate)
                : base;

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
        BigDecimal lossTotal = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (FdaChargeDto d : charges) {
            BigDecimal fdaAmt = zero(d.getFdaAmount() != null ? d.getFdaAmount() : d.getAmount());
            BigDecimal costAmt = zero(d.getCostAmount());
            BigDecimal dnAmt = zero(d.getDnAmount());
            BigDecimal cnAmt = zero(d.getCnAmount());

            BigDecimal pl = fdaAmt
                    .subtract(costAmt)
                    .subtract(dnAmt)
                    .add(cnAmt);

            d.setProfitLoss(pl);

            if (costAmt.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal per = pl
                        .multiply(BigDecimal.valueOf(100))
                        .divide(costAmt, 2, RoundingMode.HALF_UP);
                d.setProfitLossPer(per);
            } else {
                d.setProfitLossPer(BigDecimal.ZERO);
            }

            if (headerDto != null) {
                if (pl.compareTo(BigDecimal.ZERO) > 0) {
                    profitTotal = profitTotal.add(pl);
                } else if (pl.compareTo(BigDecimal.ZERO) < 0) {
                    lossTotal = lossTotal.add(pl.abs());
                }
                totalCost = totalCost.add(costAmt);
            }
        }

        if (headerDto != null) {
            headerDto.setProfitTotal(profitTotal);
            headerDto.setLossTotal(lossTotal);

            BigDecimal headerPl = profitTotal.subtract(lossTotal);
            headerDto.setProfitLossAmount(headerPl);

            if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal per = headerPl
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalCost, 2, RoundingMode.HALF_UP);
                headerDto.setProfitLossPer(per.toPlainString());
            } else {
                headerDto.setProfitLossPer("0");
            }
        }
    }

    public static String getReportFileName(String reportType) {
        return switch (reportType.toLowerCase()) {
            case "usd" -> "PDA/FDAreportUSD.jrxml";
            case "default", "standard" -> "PDA/FDAreport1.jrxml";
            default -> "PDA/FDAreport1.jrxml";
        };
    }

}
