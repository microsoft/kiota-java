package com.microsoft.kiota;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;

public class PeriodAndDuration implements TemporalAmount, Comparable<PeriodAndDuration>, Serializable {

    /**
     * A constant for a duration of zero.
     */
    public static final PeriodAndDuration ZERO = new PeriodAndDuration(Period.ZERO, Duration.ZERO);

    /**
     * The period.
     */
    private final Period period;

    /**
     * Gets the period section of the type.
     * @return the period section
     */
    public Period getPeriod() {
        return period;
    }

    /**
     * The duration.
     */
    private final Duration duration;
    /**
     * Gets the duration section of the type.
     * @return the duration section
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Non-public Constructor for PeriodAndDuration
     */
    protected PeriodAndDuration(Period period, Duration duration) {
        this.period = period;
        this.duration = duration;
    }

    /**
     * Creates an instance based on a period and duration.
     * @param period  the {@code Period}, not null
     * @param duration  the {@code Duration}, not null
     * @return the combined {@code PeriodAndDuration}, not null
     */
    public static PeriodAndDuration of(Period period, Duration duration) {
        Objects.requireNonNull(period, "The period must not be null");
        Objects.requireNonNull(duration, "The duration must not be null");
        return new PeriodAndDuration(period, duration);
    }

    /**
     * Creates an instance based on a period.
     * @param period  the {@code Period}, not null
     * @return the combined {@code PeriodAndDuration}, not null
     */
    public static PeriodAndDuration ofPeriod(Period period) {
        Objects.requireNonNull(period, "The period must not be null");
        return new PeriodAndDuration(period, Duration.ZERO);
    }

    /**
     * Creates an instance based on a duration.
     * @param duration  the {@code Duration}, not null
     * @return the combined {@code PeriodAndDuration}, not null
     */
    public static PeriodAndDuration ofDuration(Duration duration) {
        Objects.requireNonNull(duration, "The duration must not be null");
        return new PeriodAndDuration(Period.ZERO, duration);
    }

    /**
     * @param stringValue the {@code String} parse from.
     * @return parsed instance of {@code PeriodAndDuration}
     */
    public static PeriodAndDuration parse(String stringValue) {
        Objects.requireNonNull(stringValue, "stringValue");
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

    /**
     * Returns a string representation of the instance in the ISO-8601 format 'PnYnMnDTnHnMnS'.
     * @return the period in ISO-8601 string format
     */
    @Override
    public String toString() {
        if (period.isZero()) {
            return duration.toString();
        }
        if (duration.isZero()) {
            return period.toString();
        }
        //simply concatenate and drop the first `P` in the duration
        return period.toString() + duration.toString().substring(1);
    }
}
