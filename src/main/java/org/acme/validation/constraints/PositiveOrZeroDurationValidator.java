package org.acme.validation.constraints;

import io.quarkus.arc.Unremovable;
import java.time.Duration;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForNumber;

/**
 * @see PositiveOrZeroValidatorForNumber
 * @author christiant
 */
@Unremovable
public class PositiveOrZeroDurationValidator implements ConstraintValidator<PositiveOrZeroDuration, Duration> {

    @Override
    public boolean isValid(Duration value, ConstraintValidatorContext context) {
        return new PositiveOrZeroValidatorForNumber().isValid(value == null ? null : value.getSeconds(), context);
    }
}
