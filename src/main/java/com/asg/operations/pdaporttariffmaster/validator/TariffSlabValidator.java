package com.asg.operations.pdaporttariffmaster.validator;

import com.asg.operations.pdaporttariffmaster.annotation.TariffSlabValidation;
import com.asg.operations.pdaporttariffmaster.dto.PdaPortTariffChargeDetailRequest;
import com.asg.operations.pdaporttariffmaster.dto.PdaPortTariffSlabDetailRequest;
import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class TariffSlabValidator implements ConstraintValidator<TariffSlabValidation, PdaPortTariffChargeDetailRequest> {

    @Override
    public boolean isValid(PdaPortTariffChargeDetailRequest req, ConstraintValidatorContext ctx) {

        if (req == null) return true;

        String slab = req.getTariffSlab();

        // If tariffSlab is null or equals NONE → no validation needed
        if (slab == null || slab.equalsIgnoreCase("NONE")) {
            return true;
        }

        List<PdaPortTariffSlabDetailRequest> slabDetails = req.getSlabDetails();

        // If tariffSlab is provided → slabDetails must NOT be empty (unless it's a new charge row)
        if (slabDetails == null || slabDetails.isEmpty()) {
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

        boolean isValid = true;
        ctx.disableDefaultConstraintViolation();
        String chargeLabel = req.getChargePoid() != null
                ? "Charge " + req.getChargePoid().toPlainString()
                : "Charge";

        for (int i = 0; i < slabDetails.size(); i++) {
            PdaPortTariffSlabDetailRequest slabReq = slabDetails.get(i);
            if (slabReq == null) {
                continue;
            }

            int slabRow = i + 1; // 1-based for UI

            if (slabReq.getQuantityFrom() != null
                    && slabReq.getQuantityTo() != null
                    && slabReq.getQuantityFrom().compareTo(slabReq.getQuantityTo()) > 0) {

                ctx.buildConstraintViolationWithTemplate(
                                chargeLabel + ", Slab row " + slabRow
                                        + ": Quantity From must be less than or equal to Quantity To")
                        .addPropertyNode("slabDetails")
                        .inIterable().atIndex(i)
                        .addPropertyNode("quantityFrom")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}
