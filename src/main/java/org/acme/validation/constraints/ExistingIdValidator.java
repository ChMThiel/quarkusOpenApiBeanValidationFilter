package org.acme.validation.constraints;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.acme.ExistingId;

@Dependent
@Unremovable
public class ExistingIdValidator implements ConstraintValidator<ExistingId, Object> {

    Class entityClass;

    @Override
    public void initialize(ExistingId aExisitingId) {
        entityClass = aExisitingId.of();
    }

    @Override
    public boolean isValid(Object aId, ConstraintValidatorContext aContext) {
        if (aId == null) {
            return true;
        }
        return true; //just a dummy
    }
}
