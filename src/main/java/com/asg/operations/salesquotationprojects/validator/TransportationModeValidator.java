package com.asg.operations.salesquotationprojects.validator;

import com.asg.operations.salesquotationprojects.annotation.ValidTransportationMode;
import com.asg.operations.salesquotationprojects.dto.SalesQuoteProjectsRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class TransportationModeValidator implements ConstraintValidator<ValidTransportationMode, SalesQuoteProjectsRequest> {

    @Override
    public boolean isValid(SalesQuoteProjectsRequest request, ConstraintValidatorContext context) {

        if (request == null) {
            return true;
        }

        String transportationMode = request.getTransportationMode();

        if ("OTHER".equalsIgnoreCase(transportationMode) && StringUtils.isBlank(request.getOtherMode())) {

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Other mode is required when transportation mode is OTHER")
                    .addPropertyNode("otherMode")
                    .addConstraintViolation();

            return false;
        }
        return true;
    }
}
