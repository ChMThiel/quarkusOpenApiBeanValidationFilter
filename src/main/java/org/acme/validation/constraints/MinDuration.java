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
import java.time.temporal.ChronoUnit;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * The annotated element must be greater or equal to given value. {@code null} elements are considered valid.
 *
 * @author christiant
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {MinDurationValidator.class})
public @interface MinDuration {

    public static final String MUST_BE_GREATER_OR_EQUAL_TO_ = "must be greater than or equal to ";
    
    String openApiDescription() default "Must be greater than or equal to {value} {unit}";

    String message() default "{jakarta.validation.constraints.Min.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @return value the element must be higher or equal to
     */
    long value();

    ChronoUnit unit() default ChronoUnit.SECONDS;
}
