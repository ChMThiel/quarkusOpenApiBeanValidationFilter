package org.acme;

import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;

/**
 * Interface for marking CREATE-Context during BeanValidation
 * @see ConvertGroup
 * @see ValidUpdate
 */
public interface ValidCreate extends Default {
    
}
