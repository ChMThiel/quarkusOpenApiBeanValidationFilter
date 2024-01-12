package org.acme.validation.constraints;

import io.quarkus.arc.Unremovable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.acme.validation.MessageInterpolator;

/**
 * @author christiant
 */
@Unremovable
public class MaxDurationValidator implements ConstraintValidator<MaxDuration, Duration> {

    private Duration maxDuration;
    private String messageKey;

    @Override
    public void initialize(MaxDuration aMinQuantity) {
        messageKey = aMinQuantity.message().substring(1, aMinQuantity.message().length() - 1);
        ChronoUnit unit = aMinQuantity.unit();
        maxDuration = Duration.of(aMinQuantity.value(), unit);
    }

    @Override
    public boolean isValid(Duration value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        boolean valid = value.compareTo(maxDuration) <= 0;
        if (!valid) {
            String message = MessageInterpolator
                    .getString(messageKey, () -> MaxDuration.MUST_BE_LESSER_OR_EQUAL_TO_ + maxDuration)
                    .replace("{value}", maxDuration.toString());
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }
        return valid;
    }
}
