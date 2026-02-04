package com.asg.operations.pdaporttariffmaster.validator;

import com.asg.operations.pdaporttariffmaster.annotation.TariffSlabValidation;
import com.asg.operations.pdaporttariffmaster.dto.PdaPortTariffChargeDetailRequest;
import com.asg.operations.portcallreport.enums.ActionType;
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

        // If tariffSlab is provided → slabDetails must NOT be empty (unless it's a new charge row)
        if (req.getSlabDetails() == null || req.getSlabDetails().isEmpty()) {
            // Allow empty slab details for newly created charges; slabs can be added in a follow-up request
            if (req.getActionType() == ActionType.isCreated) {
                return true;
            }
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(
                    "Slab details cannot be empty when tariffSlab is provided"
            ).addPropertyNode("slabDetails").addConstraintViolation();
            return false;
        }

        return true;
    }
}

