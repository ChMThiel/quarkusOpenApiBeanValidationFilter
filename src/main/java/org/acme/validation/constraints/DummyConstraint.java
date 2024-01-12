package org.acme.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @since 06.12.2023
 */
@Target(value = {ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = DummyValidator.class)
public @interface DummyConstraint {

    String openApiDescription() default "DummyConstraint desc";

    String message() default "{xxx}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
