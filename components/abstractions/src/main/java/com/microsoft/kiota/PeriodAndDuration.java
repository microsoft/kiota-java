package com.microsoft.kiota;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.YEARS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.NANOS;


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
    protected PeriodAndDuration(@Nonnull Period period, @Nonnull Duration duration) {
        Objects.requireNonNull(period, "parameter period cannot be null");
        Objects.requireNonNull(duration, "parameter duration cannot be null");
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
        Objects.requireNonNull(period, "parameter period cannot be null");
        Objects.requireNonNull(duration, "parameter duration cannot be null");
        return new PeriodAndDuration(period, duration);
    }

    /**
     * Creates an instance based on a period.
     * @param period  the {@code Period}, not null
     * @return the combined {@code PeriodAndDuration}, not null
     */
    public static PeriodAndDuration ofPeriod(Period period) {
        Objects.requireNonNull(period, "parameter period cannot be null");
        return new PeriodAndDuration(period, Duration.ZERO);
    }

    /**
     * Creates an instance based on a duration.
     * @param duration  the {@code Duration}, not null
     * @return the combined {@code PeriodAndDuration}, not null
     */
    public static PeriodAndDuration ofDuration(Duration duration) {
        Objects.requireNonNull(duration, "parameter duration cannot be null");
        return new PeriodAndDuration(Period.ZERO, duration);
    }

    /**
     * @param stringValue the {@code String} parse from.
     * @return parsed instance of {@code PeriodAndDuration}
     */
    public static PeriodAndDuration parse(String stringValue) {
        Objects.requireNonNull(stringValue, "parameter stringValue cannot be null");
        //TODO implement me
        return null;
    }

    /**
     * @param periodAndDuration the {@code PeriodAndDuration} for which to compare to
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(@Nonnull PeriodAndDuration periodAndDuration) {
        Objects.requireNonNull(periodAndDuration, "parameter periodAndDuration cannot be null");
        //TODO implement me
        return 0;
    }

    /**
     * @param unit the {@code TemporalUnit} for which to return the value
     * @return the long value of the unit
     */
    @Override
    public long get(@Nonnull TemporalUnit unit) {
        Objects.requireNonNull(unit, "parameter unit cannot be null");
        if (unit instanceof ChronoUnit) {
            switch ((ChronoUnit) unit) {
                case YEARS:
                    return period.getYears();
                case MONTHS:
                    return period.getMonths();
                case DAYS:
                    return period.getDays();
                case HOURS:
                    return duration.toHours() % 24;
                case MINUTES:
                    return duration.toMinutes() % 60;
                case SECONDS:
                    return duration.getSeconds();
                case NANOS:
                    return duration.getNano();
                default:
                    break;
            }
        }
        throw new UnsupportedTemporalTypeException("Unsupported TemporalUnit of type: " + unit);
    }

    private static final List<TemporalUnit> UNITS = Collections.unmodifiableList(Arrays.<TemporalUnit>asList(YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS, NANOS));
    /**
     * @return the List of TemporalUnits; not null
     */
    @Override
    public List<TemporalUnit> getUnits() {
        return UNITS;
    }

    /**
     * @param temporal the temporal object to add the amount to, not null
     * @return an object of the same observable type with the addition made, not null
     */
    @Override
    public Temporal addTo(@Nonnull Temporal temporal) {
        Objects.requireNonNull(temporal, "parameter temporal cannot be null");
        return temporal.plus(period).plus(duration);//just add everything up
    }

    /**
     * @param temporal the temporal object to subtract the amount from, not null
     * @return an object of the same observable type with the subtraction made, not null
     */
    @Override
    public Temporal subtractFrom(@Nonnull Temporal temporal) {
        Objects.requireNonNull(temporal, "parameter temporal cannot be null");
        return temporal.minus(period).minus(duration);//just subtract everything up
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

    /**
     /**
     * Checks if this instance is equal to the specified {@code PeriodAndDuration}.
     * @param otherPeriodAndDuration  the other Object, null returns false
     * @return true if the other otherPeriodAndDuration is equal to this one
     */
    @Override
    public boolean equals(Object otherPeriodAndDuration) {
        if (otherPeriodAndDuration instanceof PeriodAndDuration) {
            PeriodAndDuration otherInstance = (PeriodAndDuration) otherPeriodAndDuration;
            return this.period.equals(otherInstance.period) && this.duration.equals(otherInstance.duration);
        }
        return false;
    }
}
