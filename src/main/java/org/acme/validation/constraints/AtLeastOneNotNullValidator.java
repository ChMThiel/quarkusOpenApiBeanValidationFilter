package org.acme.validation.constraints;

import io.quarkus.arc.Unremovable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.acme.AtLeastOneNotNull;
import org.apache.commons.lang3.reflect.FieldUtils;

@Unremovable
public class AtLeastOneNotNullValidator implements ConstraintValidator<AtLeastOneNotNull, Object> {

    private String[] fieldNames;

    @Override
    public void initialize(AtLeastOneNotNull aAtLeastOne) {
        fieldNames = aAtLeastOne.ofFields();
    }

    @Override
    public boolean isValid(Object aObject, ConstraintValidatorContext aContext) {
        if (aObject == null) {
            return true;
        }

        List<Object> objects = Arrays.stream(fieldNames)
                .map(fieldName -> getFieldValue(aObject, fieldName))
                .collect(Collectors.toList());
        boolean oneNotNullOrNotEmptyFound = false;
        for (Object object : objects) {
            if (object instanceof Collection col) {
                if (!col.isEmpty()) {
                    oneNotNullOrNotEmptyFound = true;
                }
            } else if (object != null) {
                oneNotNullOrNotEmptyFound = true;
            }
        }
        return oneNotNullOrNotEmptyFound;
    }

    Object getFieldValue(Object aObject, String aFieldName) {
        try {
            return FieldUtils.readField(aObject, aFieldName, true);
        } catch (IllegalAccessException illegalAccessException) {
            System.out.println("AtLeastOneValidator.getFieldValue() "
                    + "unable to access field " + aFieldName + " in " + aObject.getClass().getSimpleName());
            return null;
        }
    }
}
