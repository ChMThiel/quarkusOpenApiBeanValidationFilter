package org.acme.validation.constraints;

import io.quarkus.arc.Unremovable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.acme.validation.MessageInterpolator;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * @author christiant
 */
@Unremovable
public class MinDurationValidator implements ConstraintValidator<MinDuration, Duration> {

    public static final String MIN_DURATION_VALIDATOR_ENABLED = "MinDurationValidator.enabled";
    private Duration minQuantity;
    private String messageKey;

    @Override
    public void initialize(MinDuration aMinQuantity) {
        messageKey = aMinQuantity.message().substring(1, aMinQuantity.message().length() - 1);
        ChronoUnit unit = aMinQuantity.unit();
        minQuantity = Duration.of(aMinQuantity.value(), unit);
    }

    @Override
    public boolean isValid(Duration value, ConstraintValidatorContext context) {
        Boolean enabled = ConfigProvider.getConfig().getOptionalValue(MIN_DURATION_VALIDATOR_ENABLED, Boolean.class).orElse(true);
        if (!enabled || value == null) {
            return true;
        }
        boolean valid = value.compareTo(minQuantity) >= 0;
        if (!valid) {
            String message = 
                    MessageInterpolator
                        .getString(messageKey, () -> MinDuration.MUST_BE_GREATER_OR_EQUAL_TO_ + minQuantity)
                        .replace("{value}", minQuantity.toString());
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }
        return valid;
    }
}
