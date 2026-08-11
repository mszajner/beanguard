package io.beanguard.api.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = PolishNIPValidator.class)
@Documented
public @interface PolishNIP {
    String message() default "Invalid Polish NIP number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
