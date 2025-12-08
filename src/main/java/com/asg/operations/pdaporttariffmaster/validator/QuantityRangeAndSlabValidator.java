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

        // 1. quantityFrom <= quantityTo
        if (req.getQuantityFrom() != null &&
                req.getQuantityTo() != null &&
                req.getQuantityFrom().compareTo(req.getQuantityTo()) > 0) {

            ctx.buildConstraintViolationWithTemplate("Quantity From must be less than or equal to Quantity To")
                    .addPropertyNode("quantityFrom")
                    .addConstraintViolation();
            isValid = false;
        }

        // 2. At least one day/rate pair must be provided
        boolean hasAnyPair =
                (req.getDays1() != null || req.getRate1() != null) ||
                        (req.getDays2() != null || req.getRate2() != null) ||
                        (req.getDays3() != null || req.getRate3() != null) ||
                        (req.getDays4() != null || req.getRate4() != null);

        if (!hasAnyPair) {
            ctx.buildConstraintViolationWithTemplate("At least one day/rate pair must be provided")
                    .addConstraintViolation();
            isValid = false;
        }

        // Disable default messages
        ctx.disableDefaultConstraintViolation();

        return isValid;
    }
}

