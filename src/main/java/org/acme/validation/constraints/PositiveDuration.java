package org.acme.validation.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * The annotated element must be a positive Duration or Duration.ZERO. {@code null} elements are considered valid.
 *
 * @author christiant
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {PositiveDurationValidator.class})
public @interface PositiveDuration {
    
    String openApiDescription() default "Must be greater than zero";

    String message() default "{jakarta.validation.constraints.Positive.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
