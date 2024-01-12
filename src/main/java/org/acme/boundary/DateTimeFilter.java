package org.acme.boundary;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.ws.rs.QueryParam;

/**
 * Filter defining a date-range to be used on queries
 *
 * @param <T>
 */
@ValidFromToDate
public class DateTimeFilter<T extends DateTimeFilter> {

    @QueryParam("from")
    @NotNull
    @PastOrPresent
    private OffsetDateTime fromDate;

    @QueryParam("to")
    @NotNull
    private OffsetDateTime toDate;

    //<editor-fold defaultstate="collapsed" desc="getter & setter">
    public OffsetDateTime getFromDate() {
        return fromDate;
    }

    public T setFromDate(OffsetDateTime aFromDate) {
        fromDate = aFromDate;
        return (T) this;
    }

    public OffsetDateTime getToDate() {
        return toDate;
    }

    public T setToDate(OffsetDateTime aToDate) {
        toDate = aToDate;
        return (T) this;
    }
    //</editor-fold>
}
