package org.acme;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.acme.validation.constraints.ExistingNameValidator;

/**
 * asserts that a object existis for annotated unique name and given class-type
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ExistingNameValidator.class)
public @interface ExistingName {

    String openApiDescription() default "{of} with given name has to exist";

    String message() default "{io.gec.smom.base.validation.ExistingName.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Entity-class. The annotated Id-field has to contain the primaryKey of this entity
     *
     * @return
     */
    Class of();
}
