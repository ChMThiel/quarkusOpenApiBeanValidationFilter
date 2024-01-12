package org.acme.validation.constraints;

import io.quarkus.arc.Unremovable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Unremovable
public class DummyValidator implements ConstraintValidator<DummyConstraint, Boolean> {

    @Override
    public boolean isValid(Boolean value, ConstraintValidatorContext context) {
        return true;
    }
}
