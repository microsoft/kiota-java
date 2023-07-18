package com.microsoft.kiota;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodAndDurationTest {
    @Test
    void Defensive() {
        // Assert
        var exception = assertThrows(NullPointerException.class, () -> PeriodAndDuration.of(null,null));
        assertTrue(exception.getMessage().contains("period cannot be null"));

        var exception2 = assertThrows(NullPointerException.class, () -> PeriodAndDuration.of(null, Duration.ZERO));
        assertTrue(exception2.getMessage().contains("period cannot be null"));

        var exception3 = assertThrows(NullPointerException.class, () -> PeriodAndDuration.of(Period.ZERO, null));
        assertTrue(exception3.getMessage().contains("duration cannot be null"));

        var exception4 = assertThrows(NullPointerException.class, () -> PeriodAndDuration.ofDuration(null));
        assertTrue(exception4.getMessage().contains("duration cannot be null"));

        var exception5 = assertThrows(NullPointerException.class, () -> PeriodAndDuration.ofPeriod(null));
        assertTrue(exception5.getMessage().contains("period cannot be null"));

        final PeriodAndDuration periodAndDuration = PeriodAndDuration.of(Period.ZERO, Duration.ZERO);
        assertNotNull(periodAndDuration);
    }

    @Test
    void EqualityForZeroValues() {
        // Assert
        final PeriodAndDuration periodAndDuration = PeriodAndDuration.of(Period.ZERO, Duration.ZERO);
        assertTrue(periodAndDuration.equals(periodAndDuration));
        assertTrue(PeriodAndDuration.ZERO.equals(periodAndDuration));
    }

    @Test
    void ParsesOnlyDurationString() {
        String [] inputs = { "PT6H", "PT1M", "PT1S", "PT6H10M10S","+PT6H10M10S","-PT6H10M10S" };
        for (var durationString :inputs) {
            var duration = Duration.parse(durationString);
            var periodAndDuration = PeriodAndDuration.parse(durationString);

            // Assert
            assertEquals(Period.ZERO, periodAndDuration.getPeriod());
            assertEquals(duration, periodAndDuration.getDuration());
        }
    }

    @Test
    void SerializesOnlyDurationString() {
        String [] inputs = { "PT6H", "PT1M", "PT1S", "PT6H10M10S","+PT6H10M10S" };
        for (var durationString :inputs) {
            var duration = Duration.parse(durationString);
            var periodAndDuration = PeriodAndDuration.parse(durationString);

            // Assert
            assertEquals(Period.ZERO, periodAndDuration.getPeriod());
            assertEquals(duration, periodAndDuration.getDuration());

            // Assert serialization
            if(durationString.startsWith("+")){// character will be removed as its not needed
                durationString = durationString.substring(1);
            }

            assertEquals(durationString, periodAndDuration.toString());
        }
    }

    @Test
    void ParsesOnlyPeriodString() {
        String [] inputs = { "P6Y", "P10M", "P20D", "P6Y10M10D","+P6Y10M10D","-P6Y10M10D" };
        for (var periodString :inputs) {
            var period = Period.parse(periodString);
            var periodAndDuration = PeriodAndDuration.parse(periodString);

            // Assert
            assertEquals(Duration.ZERO, periodAndDuration.getDuration());
            assertEquals(period, periodAndDuration.getPeriod());
        }
    }

    @Test
    void SerializesOnlyPeriodString() {
        String [] inputs = { "P6Y", "P10M", "P20D", "P6Y10M10D","+P6Y10M10D"};
        for (var periodString :inputs) {
            var period = Period.parse(periodString);
            var periodAndDuration = PeriodAndDuration.parse(periodString);

            // Assert
            assertEquals(Duration.ZERO, periodAndDuration.getDuration());
            assertEquals(period, periodAndDuration.getPeriod());

            // Assert serialization
            if(periodString.startsWith("+")){// character will be removed as its not needed
                periodString = periodString.substring(1);
            }

            assertEquals(periodString, periodAndDuration.toString());
        }
    }

    @Test
    void ParsesOnlyPeriodAndDurationString() {
        String [] inputs = { "P6YT6H", "P10MT1M", "P20DT1S", "P6Y10M10DT6H10M10S","+P6Y10M10DT6H10M10S","-P6Y10M10DT6H10M10S" };
        for (var periodString :inputs) {
            var periodAndDuration = PeriodAndDuration.parse(periodString);

            // Assert
            assertNotEquals(Duration.ZERO, periodAndDuration.getDuration());
            assertNotEquals(Period.ZERO, periodAndDuration.getPeriod());
        }
    }

    @Test
    void SerializesPeriodAndDurationString() {
        String [] inputs = { "P6YT6H", "P10MT1M", "P20DT1S", "P6Y10M10DT6H10M10S","+P6Y10M10DT6H10M10S" };
        for (var periodString :inputs) {
            var periodAndDuration = PeriodAndDuration.parse(periodString);

            // Assert
            assertNotEquals(Duration.ZERO, periodAndDuration.getDuration());
            assertNotEquals(Period.ZERO, periodAndDuration.getPeriod());

            // Assert serialization
            if(periodString.startsWith("+")){// character will be removed as its not needed
                periodString = periodString.substring(1);
            }

            assertEquals(periodString, periodAndDuration.toString());
        }
    }

    @Test
    void ParsesOnlyPeriodAndDurationStringAndValue() {
        String input = "P7Y9M15DT6H10M11S";
        var periodAndDuration = PeriodAndDuration.parse(input);
        var periodAndDurationWithSign = PeriodAndDuration.parse("+"+input);

        assertEquals(periodAndDurationWithSign, periodAndDuration);// sign results in same value

        // Assert
        assertNotEquals(Duration.ZERO, periodAndDuration.getDuration());
        assertNotEquals(Period.ZERO, periodAndDuration.getPeriod());

        assertEquals(7,periodAndDuration.get(ChronoUnit.YEARS));//7 years
        assertEquals(9,periodAndDuration.get(ChronoUnit.MONTHS)); // 9 months
        assertEquals(15,periodAndDuration.get(ChronoUnit.DAYS)); // 15 days
        assertEquals(6,periodAndDuration.get(ChronoUnit.HOURS));// 6 Hours
        assertEquals(10,periodAndDuration.get(ChronoUnit.MINUTES)); // 10 minutes
        assertEquals(11,periodAndDuration.get(ChronoUnit.SECONDS)); // 11 seconds
    }

    @Test
    void ComparesPeriodAndDurationObjects() {

        String input = "P7Y9M15DT6H10M11S";
        String inputPlusOneMinute = "P7Y9M15DT6H11M11S";
        String inputMinusOneMinute = "P7Y9M15DT6H9M11S";
        String inputMinusOneMonth = "P7Y8M15DT6H10M11S";
        String inputPlusOneMonth = "P7Y10M15DT6H10M11S";
        var periodAndDuration = PeriodAndDuration.parse(input);
        var periodAndDurationWithSign = PeriodAndDuration.parse("+"+input);

        assertEquals(periodAndDurationWithSign, periodAndDuration);// sign results in same value
        assertEquals(0, periodAndDuration.compareTo(periodAndDurationWithSign)); // same comparison

        // less than
        assertEquals(-1, periodAndDuration.compareTo(PeriodAndDuration.parse(inputPlusOneMinute)));
        assertEquals(-1, periodAndDuration.compareTo(PeriodAndDuration.parse(inputPlusOneMonth)));


        // greater than
        assertEquals(1, periodAndDuration.compareTo(PeriodAndDuration.parse(inputMinusOneMinute)));
        assertEquals(1, periodAndDuration.compareTo(PeriodAndDuration.parse(inputMinusOneMonth)));

    }


}
