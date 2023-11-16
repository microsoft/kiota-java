package com.microsoft.kiota;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.YEARS;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The aggregate type for {@code Period} and {@code Duration }
 */
public final class PeriodAndDuration
        implements TemporalAmount, Comparable<PeriodAndDuration>, Serializable {

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
    @Nonnull
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
    @Nonnull
    public Duration getDuration() {
        return duration;
    }

    /**
     * Non-public Constructor for PeriodAndDuration
     * @param period The {@code Period} component of the aggregate type
     * @param duration The {@code Duration } component of the aggregate type
     */
    private PeriodAndDuration(@Nonnull Period period, @Nonnull Duration duration) {
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
    @Nonnull
    public static PeriodAndDuration of(@Nonnull Period period, @Nonnull Duration duration) {
        Objects.requireNonNull(period, "parameter period cannot be null");
        Objects.requireNonNull(duration, "parameter duration cannot be null");
        return new PeriodAndDuration(period, duration);
    }

    /**
     * Creates an instance based on a period.
     * @param period  the {@code Period}, not null
     * @return the combined {@code PeriodAndDuration}, not null
     */
    @Nonnull
    public static PeriodAndDuration ofPeriod(@Nonnull Period period) {
        Objects.requireNonNull(period, "parameter period cannot be null");
        return new PeriodAndDuration(period, Duration.ZERO);
    }

    /**
     * Creates an instance based on a duration.
     * @param duration  the {@code Duration}, not null
     * @return the combined {@code PeriodAndDuration}, not null
     */
    @Nonnull
    public static PeriodAndDuration ofDuration(@Nonnull Duration duration) {
        Objects.requireNonNull(duration, "parameter duration cannot be null");
        return new PeriodAndDuration(Period.ZERO, duration);
    }

    /**
     * Creates an instance based on a PeriodAndDuration.
     * @param periodAndDuration the {@code PeriodAndDuration}, not null
     * @return the combined {@code PeriodAndDuration}, not null
     */
    @Nonnull
    public static PeriodAndDuration ofPeriodAndDuration(
            @Nonnull PeriodAndDuration periodAndDuration) {
        Objects.requireNonNull(periodAndDuration, "parameter periodAndDuration cannot be null");
        return new PeriodAndDuration(
                periodAndDuration.getPeriod(), periodAndDuration.getDuration());
    }

    /**
     * Parses a string to produce a {@code PeriodAndDuration}.
     * @param stringValue the {@code String} parse from.
     * @return parsed instance of {@code PeriodAndDuration}
     */
    @Nonnull
    public static PeriodAndDuration parse(@Nonnull String stringValue) {
        Objects.requireNonNull(stringValue, "parameter stringValue cannot be null");

        if (stringValue
                .substring(0, 3)
                .contains("PT")) { // it is only a duration value as it starts with 'PT', '+PT' or,
            // '-PT'
            return PeriodAndDuration.ofDuration(Duration.parse(stringValue));
        }
        int timePosition = stringValue.indexOf("T");
        if (timePosition < 0) { // only a period value as there is no time component
            return PeriodAndDuration.ofPeriod(Period.parse(stringValue));
        }

        String sign = "";
        if (stringValue.charAt(0) == '-') {
            sign = "-";
        } // no need for checking the `+` sign as this is the default

        Period period =
                Period.parse(
                        stringValue.substring(0, timePosition)); // sign will be passed and parsed
        Duration duration =
                Duration.parse(
                        sign
                                + "P"
                                + stringValue.substring(
                                        timePosition)); // pass the negative if need be
        return PeriodAndDuration.of(period, duration);
    }

    /**
     * @param periodAndDuration the {@code PeriodAndDuration} for which to compare to
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(@Nonnull PeriodAndDuration periodAndDuration) {
        Objects.requireNonNull(periodAndDuration, "parameter periodAndDuration cannot be null");

        if (this.equals(periodAndDuration)) {
            return 0; // they are the same/equal
        }

        if (this.period.equals(
                periodAndDuration.getPeriod())) { // same period so just compare the durations
            return this.duration.compareTo(periodAndDuration.getDuration());
        }

        // just check if the difference in the period is negative as this makes the duration moot
        if (this.period.minus(periodAndDuration.getPeriod()).isNegative()) {
            return -1; // this period is smaller. So duration won't count
        } else {
            return 1;
        }
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
                    return duration.getSeconds() % 60;
                case NANOS:
                    return duration.getNano();
                default:
                    break;
            }
        }
        throw new UnsupportedTemporalTypeException("Unsupported TemporalUnit of type: " + unit);
    }

    private static final List<TemporalUnit> UNITS =
            Collections.unmodifiableList(
                    Arrays.<TemporalUnit>asList(
                            YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS, NANOS));

    /**
     * @return the List of TemporalUnits; not null
     */
    @Override
    public List<TemporalUnit> getUnits() {
        return new ArrayList<>(UNITS);
    }

    /**
     * @param temporal the temporal object to add the amount to, not null
     * @return an object of the same observable type with the addition made, not null
     */
    @Override
    public Temporal addTo(@Nonnull Temporal temporal) {
        Objects.requireNonNull(temporal, "parameter temporal cannot be null");
        return temporal.plus(period).plus(duration); // just add everything up
    }

    /**
     * @param temporal the temporal object to subtract the amount from, not null
     * @return an object of the same observable type with the subtraction made, not null
     */
    @Override
    public Temporal subtractFrom(@Nonnull Temporal temporal) {
        Objects.requireNonNull(temporal, "parameter temporal cannot be null");
        return temporal.minus(period).minus(duration); // just subtract everything up
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
        // simply concatenate and drop the first `P` in the duration
        return period + duration.toString().substring(1);
    }

    /**
     * Gets the hashcode for the object.
     * @return The hashCode of the object
     */
    @Override
    public int hashCode() {
        return period.hashCode() + duration.hashCode();
    }

    /**
     * Checks if this instance is equal to the specified {@code PeriodAndDuration}.
     * @param otherPeriodAndDuration  the other Object, null returns false
     * @return true if the other otherPeriodAndDuration is equal to this one
     */
    @Override
    public boolean equals(Object otherPeriodAndDuration) {
        if (this == otherPeriodAndDuration) {
            return true; // same instance
        }

        if (otherPeriodAndDuration instanceof PeriodAndDuration) {
            PeriodAndDuration otherInstance = (PeriodAndDuration) otherPeriodAndDuration;
            return this.period.equals(otherInstance.period)
                    && this.duration.equals(otherInstance.duration);
        }
        return false;
    }
}
