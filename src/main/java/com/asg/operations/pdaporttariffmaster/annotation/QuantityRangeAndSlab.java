package com.asg.operations.pdaporttariffmaster.annotation;

import com.asg.operations.pdaporttariffmaster.validator.QuantityRangeAndSlabValidator;
import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = QuantityRangeAndSlabValidator.class)
public @interface QuantityRangeAndSlab {

    String message() default "Invalid slab detail";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};
}

