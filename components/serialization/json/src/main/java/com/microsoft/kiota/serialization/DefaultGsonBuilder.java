package com.microsoft.kiota.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.microsoft.kiota.PeriodAndDuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;

public class DefaultGsonBuilder {

    private static final TypeAdapter<OffsetDateTime> OFFSET_DATE_TIME =
            new TypeAdapter<OffsetDateTime>() {
                @Override
                public OffsetDateTime read(JsonReader in) throws IOException {
                    String stringValue = in.nextString();
                    try {
                        return OffsetDateTime.parse(stringValue);
                    } catch (DateTimeParseException ex) {
                        // Append UTC offset if it's missing
                        try {
                            LocalDateTime localDateTime = LocalDateTime.parse(stringValue);
                            return localDateTime.atOffset(ZoneOffset.UTC);
                        } catch (DateTimeParseException ex2) {
                            throw new JsonSyntaxException(
                                    "Failed parsing '"
                                            + stringValue
                                            + "' as OffsetDateTime; at path "
                                            + in.getPreviousPath(),
                                    ex2);
                        }
                    }
                }

                @Override
                public void write(JsonWriter out, OffsetDateTime value) throws IOException {
                    out.value(value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
                }
            };

    private static final TypeAdapter<LocalDate> LOCAL_DATE =
            new TypeAdapter<LocalDate>() {
                @Override
                public LocalDate read(JsonReader in) throws IOException {
                    String stringValue = in.nextString();
                    try {
                        return LocalDate.parse(stringValue);
                    } catch (DateTimeParseException ex) {
                        throw new JsonSyntaxException(
                                "Failed parsing '"
                                        + stringValue
                                        + "' as LocalDate; at path "
                                        + in.getPreviousPath(),
                                ex);
                    }
                }

                @Override
                public void write(JsonWriter out, LocalDate value) throws IOException {
                    out.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
                }
            };

    private static final TypeAdapter<LocalTime> LOCAL_TIME =
            new TypeAdapter<LocalTime>() {
                @Override
                public LocalTime read(JsonReader in) throws IOException {
                    String stringValue = in.nextString();
                    try {
                        return LocalTime.parse(stringValue);
                    } catch (DateTimeParseException ex) {
                        throw new JsonSyntaxException(
                                "Failed parsing '"
                                        + stringValue
                                        + "' as LocalTime; at path "
                                        + in.getPreviousPath(),
                                ex);
                    }
                }

                @Override
                public void write(JsonWriter out, LocalTime value) throws IOException {
                    out.value(value.format(DateTimeFormatter.ISO_LOCAL_TIME));
                }
            };

    private static final TypeAdapter<PeriodAndDuration> PERIOD_AND_DURATION =
            new TypeAdapter<PeriodAndDuration>() {
                @Override
                public PeriodAndDuration read(JsonReader in) throws IOException {
                    String stringValue = in.nextString();
                    try {
                        return PeriodAndDuration.parse(stringValue);
                    } catch (DateTimeParseException ex) {
                        throw new JsonSyntaxException(
                                "Failed parsing '"
                                        + stringValue
                                        + "' as PeriodAndDuration; at path "
                                        + in.getPreviousPath(),
                                ex);
                    }
                }

                @Override
                public void write(JsonWriter out, PeriodAndDuration value) throws IOException {
                    out.value(value.toString());
                }
            };

    private static final TypeAdapter<byte[]> BYTE_ARRAY =
            new TypeAdapter<byte[]>() {
                @Override
                public byte[] read(JsonReader in) throws IOException {
                    String stringValue = in.nextString();
                    try {
                        if (stringValue.isEmpty()) {
                            return null;
                        }
                        return Base64.getDecoder().decode(stringValue);
                    } catch (IllegalArgumentException ex) {
                        throw new JsonSyntaxException(
                                "Failed parsing '"
                                        + stringValue
                                        + "' as byte[]; at path "
                                        + in.getPreviousPath(),
                                ex);
                    }
                }

                @Override
                public void write(JsonWriter out, byte[] value) throws IOException {
                    out.value(Base64.getEncoder().encodeToString(value));
                }
            };

    private static final Gson defaultInstance = getDefaultBuilder().create();

    public static Gson getDefaultInstance() {
        return defaultInstance;
    }

    public static GsonBuilder getDefaultBuilder() {
        return new GsonBuilder()
                .registerTypeAdapter(OffsetDateTime.class, OFFSET_DATE_TIME.nullSafe())
                .registerTypeAdapter(LocalDate.class, LOCAL_DATE.nullSafe())
                .registerTypeAdapter(LocalTime.class, LOCAL_TIME.nullSafe())
                .registerTypeAdapter(PeriodAndDuration.class, PERIOD_AND_DURATION.nullSafe())
                .registerTypeAdapter(byte[].class, BYTE_ARRAY.nullSafe());
    }
}
