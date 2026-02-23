package com.asg.operations.salesquotationprojects.annotation;

import com.asg.operations.salesquotationprojects.validator.TransportationModeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TransportationModeValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTransportationMode {

    String message() default "Other mode is required when transportation mode is OTHER";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
