package org.acme.boundary;

import java.util.UUID;
import org.acme.AtLeastOneNotNull;
import org.acme.ExistingId;

/**
 * @since 07.12.2023
 */
@AtLeastOneNotNull(ofFields = {"superField1", "superField2"})
public abstract class AbstractBeanValidationDummy {

    @ExistingId(of = Sample.class)
    UUID superField1;
    UUID superField2;

    @ExistingId(of = Sample.class)
    abstract UUID getSuperGetter();
}
