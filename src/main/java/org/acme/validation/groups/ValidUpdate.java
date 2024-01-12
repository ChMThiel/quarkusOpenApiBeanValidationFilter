package org.acme;

import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;

/**
 * Interface for marking Update-Context during BeanValidation
 * @see ConvertGroup
 * @see ValidCreate
 */
public interface ValidUpdate extends Default {
    
}
