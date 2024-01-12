package org.acme.validation.constraints;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.acme.ExistingName;

@Dependent
@Unremovable
public class ExistingNameValidator implements ConstraintValidator<ExistingName, String> {

    Class entityClass;

    @Override
    public void initialize(ExistingName aExisitingName) {
        entityClass = aExisitingName.of();
    }

    @Override
    public boolean isValid(String aName, ConstraintValidatorContext aContext) {
        return true;//just a dummy
    }
}
