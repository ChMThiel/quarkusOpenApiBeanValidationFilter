package org.acme;

import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;

/**
 * Interface for marking Delete-Context during BeanValidation
 * @see ConvertGroup
 * @see ValidCreate
 */
public interface ValidDelete extends Default {
    
}
