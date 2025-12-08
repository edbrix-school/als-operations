package com.asg.operations.pdaporttariffmaster.annotation;

import com.asg.operations.pdaporttariffmaster.validator.TariffSlabValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TariffSlabValidator.class)
public @interface TariffSlabValidation {
    String message() default "Slab details are required when tariffSlab is provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

