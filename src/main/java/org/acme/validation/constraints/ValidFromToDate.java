package org.acme.boundary;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
public @interface ValidFromToDate {

    String openApiDescription() default "fromDate has to be before toDate";

    String message() default "{ValidFromToDate}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
