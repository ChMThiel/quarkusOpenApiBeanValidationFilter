package org.acme.validation.constraints;

import io.quarkus.arc.Unremovable;
import java.time.Duration;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForNumber;

/**
 * @see PositiveValidatorForNumber
 * @author christiant
 */
@Unremovable
public class PositiveDurationValidator implements ConstraintValidator<PositiveDuration, Duration> {

    @Override
    public boolean isValid(Duration value, ConstraintValidatorContext context) {
        return new PositiveValidatorForNumber().isValid(value == null ? null : value.getSeconds(), context);
    }
}
