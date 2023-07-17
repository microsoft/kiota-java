package com.microsoft.kiota;

import javax.annotation.Nonnull;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;

public class PeriodAndDuration
        implements TemporalAmount, Comparable<PeriodAndDuration>{

    /**
     * @param stringValue the {@code String} parse from.
     * @return parsed instance of {@code PeriodAndDuration}
     */
    public static PeriodAndDuration parse(String stringValue) {
        return null;
    }

    /**
     * @param periodAndDuration the {@code PeriodAndDuration} for which to compare to
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(@Nonnull PeriodAndDuration periodAndDuration) {
        return 0;
    }

    /**
     * @param unit the {@code TemporalUnit} for which to return the value
     * @return the long value of the unit
     */
    @Override
    public long get(TemporalUnit unit) {
        return 0;
    }

    /**
     * @return the List of TemporalUnits; not null
     */
    @Override
    public List<TemporalUnit> getUnits() {
        return null;
    }

    /**
     * @param temporal the temporal object to add the amount to, not null
     * @return an object of the same observable type with the addition made, not null
     */
    @Override
    public Temporal addTo(Temporal temporal) {
        return null;
    }

    /**
     * @param temporal the temporal object to subtract the amount from, not null
     * @return an object of the same observable type with the subtraction made, not null
     */
    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return null;
    }
}
