package com.alsharif.operations.pdaporttariffmaster.validator;

import com.alsharif.operations.pdaporttariffmaster.annotation.TariffSlabValidation;
import com.alsharif.operations.pdaporttariffmaster.dto.PdaPortTariffChargeDetailRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TariffSlabValidator implements ConstraintValidator<TariffSlabValidation, PdaPortTariffChargeDetailRequest> {

    @Override
    public boolean isValid(PdaPortTariffChargeDetailRequest req, ConstraintValidatorContext ctx) {

        if (req == null) return true;

        String slab = req.getTariffSlab();

        // If tariffSlab is null or equals NONE → no validation needed
        if (slab == null || slab.equalsIgnoreCase("NONE")) {
            return true;
        }

        // If tariffSlab is provided → slabDetails must NOT be empty
        if (req.getSlabDetails() == null || req.getSlabDetails().isEmpty()) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(
                    "Slab details cannot be empty when tariffSlab is provided"
            ).addPropertyNode("slabDetails").addConstraintViolation();
            return false;
        }

        return true;
    }
}

