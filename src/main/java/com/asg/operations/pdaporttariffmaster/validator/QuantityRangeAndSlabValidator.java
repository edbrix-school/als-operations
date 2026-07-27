package com.asg.operations.pdaporttariffmaster.validator;

import com.asg.operations.pdaporttariffmaster.annotation.QuantityRangeAndSlab;
import com.asg.operations.pdaporttariffmaster.dto.PdaPortTariffSlabDetailRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class QuantityRangeAndSlabValidator implements ConstraintValidator<QuantityRangeAndSlab, PdaPortTariffSlabDetailRequest> {

    @Override
    public boolean isValid(PdaPortTariffSlabDetailRequest req, ConstraintValidatorContext ctx) {

        if (req == null) return true;

        boolean isValid = true;
        ctx.disableDefaultConstraintViolation();

        // Quantity From/To range is validated on the charge (TariffSlabValidator)
        // so messages can include chargePoid + slab row number.

        // At least one day/rate pair must be provided
        boolean hasAnyPair =
                (req.getDays1() != null || req.getRate1() != null) ||
                        (req.getDays2() != null || req.getRate2() != null) ||
                        (req.getDays3() != null || req.getRate3() != null) ||
                        (req.getDays4() != null || req.getRate4() != null);

        if (!hasAnyPair) {
            String rowPrefix = (req.getDetRowId() != null && req.getDetRowId() > 0)
                    ? "Slab detRowId " + req.getDetRowId() + ": "
                    : "";
            ctx.buildConstraintViolationWithTemplate(rowPrefix + "At least one day/rate pair must be provided")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
