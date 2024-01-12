
package org.acme.boundary;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.acme.AtLeastOneNotNull;
import org.acme.ExistingId;
import org.acme.ValidCreate;
import org.acme.validation.constraints.DummyConstraint;
import org.acme.validation.constraints.MaxDuration;
import org.acme.validation.constraints.MinDuration;
import org.acme.validation.constraints.PositiveDuration;
import org.acme.validation.constraints.PositiveOrZeroDuration;

/**
 * @since 06.12.2023
 */
@AtLeastOneNotNull(ofFields = {"a", "b"})
public class BeanValidationDummy 
        extends AbstractBeanValidationDummy 
        implements IBeanValidationDummy {
    
    @MinDuration(value = 1, unit = ChronoUnit.HOURS)
    @MaxDuration(value = 2)
    private Duration a;
    @MinDuration(value = 3, unit = ChronoUnit.HOURS)
    @MaxDuration(value = 4)
    private Duration b;
    @PositiveDuration
    private Duration c;
    @PositiveOrZeroDuration
    private Duration d;
    

    @ExistingId(of = Sample.class)
    private UUID sampleId;
    
    private boolean x;
    private Boolean y;
    
    private UUID superGetter;
    
    @Size(max = 20)
    private List<@Size(max = 10)String> strings;
    
    @NotNull(groups = ValidCreate.class)
    private String name;

    @MinDuration(value = 11)
    @MaxDuration(value = 21)
    public Duration getA() {
        return a;
    }

    public Duration getB() {
        return b;
    }

    public Duration getC() {
        return c;
    }

    @DummyConstraint
    public boolean isX() {
        return x;
    }

    @DummyConstraint
    public Boolean isY() {
        return y;
    }
    
    @Override
    UUID getSuperGetter() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
