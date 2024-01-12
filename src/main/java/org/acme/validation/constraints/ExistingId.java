package org.acme;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.acme.validation.constraints.ExistingIdValidator;

/**
 * asserts that a object existis for annotated PrimaryKey and given class-type
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ExistingIdValidator.class)
public @interface ExistingId {
    
    String openApiDescription() default "{of} with given id has to exist";

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Entity-class. The annotated Id-field has to contain the primaryKey of this entity
     *
     * @return
     */
    Class of();    
}
