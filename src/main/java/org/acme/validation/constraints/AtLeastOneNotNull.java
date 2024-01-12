package org.acme;

import jakarta.validation.Constraint;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import org.acme.validation.constraints.AtLeastOneNotNullValidator;

/**
 * The annotated type has to contain given fields. At least one of the field-values has be not null. In case of
 * Collection the Collection must not be empty In case of a Quantity it's value must not be zero
 *
 * @author christiant
 * @see NotNull
 */
@Target({TYPE, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Repeatable(AtLeastOneNotNull.List.class)
@Constraint(validatedBy = AtLeastOneNotNullValidator.class)
public @interface AtLeastOneNotNull {

    String openApiDescription() default "At least one of fields {ofFields} has to be not null";

    String message() default "{io.gec.smom.base.validation.AtLeastOneNotNull.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Validated fields. At least one of the field-values has to be not null
     *
     * @return
     */
    String[] ofFields();

    /**
     * Defines several {@link AtLeastOneNotNull} annotations on the same element.
     *
     * @see AtLeastOneNotNull
     */
    @Target({ElementType.TYPE, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {

        AtLeastOneNotNull[] value();
    }

}
