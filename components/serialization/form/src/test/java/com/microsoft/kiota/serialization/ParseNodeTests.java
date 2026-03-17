package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.kiota.PeriodAndDuration;
import com.microsoft.kiota.serialization.mocks.TestEntity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

public class ParseNodeTests {
    private static final String testUserForm =
            "displayName=Megan+Bowen&"
                    + "numbers=one,two,thirtytwo&"
                    + "givenName=Megan&"
                    + "accountEnabled=true&"
                    + "createdDateTime=2017-07-29T03:07:25Z&"
                    + "jobTitle=Auditor&"
                    + "mail=MeganB@M365x214355.onmicrosoft.com&"
                    + "mobilePhone=null&"
                    + "officeLocation=null&"
                    + "preferredLanguage=en-US&"
                    + "surname=Bowen&"
                    + "workDuration=P1M&"
                    + "startWorkTime=08:00:00&"
                    + "endWorkTime=17:00:00&"
                    + "userPrincipalName=MeganB@M365x214355.onmicrosoft.com&"
                    + "birthDay=2017-09-04&"
                    + "deviceNames=device1&deviceNames=device2&"
                    + // collection property
                    "otherPhones=123456789&otherPhones=987654321&"
                    + // collection property for additionalData
                    "id=48d31887-5fad-4d73-a9f5-3c356e68a038";

    @Test
    void getsEntityValueFromForm() {
        final FormParseNode parseNode = new FormParseNode(testUserForm);
        final TestEntity entity =
                parseNode.getObjectValue(TestEntity::createFromDiscriminatorValue);
        assertNotNull(entity);
        assertNull(entity.getOfficeLocation());
        assertTrue(entity.getAdditionalData().containsKey("jobTitle"));
        assertTrue(entity.getAdditionalData().containsKey("mobilePhone"));
        assertTrue(entity.getAdditionalData().containsKey("mobilePhone"));
        assertEquals(2, entity.getDeviceNames().size());
        assertEquals("true", entity.getAdditionalData().get("accountEnabled"));
        assertEquals("Auditor", entity.getAdditionalData().get("jobTitle"));
        assertEquals("48d31887-5fad-4d73-a9f5-3c356e68a038", entity.getId());
        assertEquals(PeriodAndDuration.parse("P1M"), entity.getWorkDuration());
        assertEquals(LocalTime.of(8, 0, 0, 0), entity.getStartWorkTime());
        assertEquals(LocalTime.of(17, 0, 0, 0), entity.getEndWorkTime());
        assertEquals("2017-09-04", entity.getBirthDay().toString());
    }

    @Test
    void getCollectionOfObjectValuesFromForm() {
        final FormParseNode parseNode = new FormParseNode(testUserForm);
        assertThrows(
                RuntimeException.class,
                () ->
                        parseNode.getCollectionOfObjectValues(
                                TestEntity::createFromDiscriminatorValue));
    }

    @Test
    void returnsDefaultIfChildNodeDoesNotExist() {
        final FormParseNode parseNode = new FormParseNode(testUserForm);
        final ParseNode childNode = parseNode.getChildNode("doesNotExist");
        assertNull(childNode);
    }

    @Test
    void getCollectionOfBooleanPrimitiveValuesFromForm() {
        final String TestFormData = "bools=true&" + "bools=false";
        final ParseNode numberNode = new FormParseNode(TestFormData).getChildNode("bools");
        final List<Boolean> numberCollection =
                numberNode.getCollectionOfPrimitiveValues(Boolean.class);
        assertNotNull(numberCollection);
        assertEquals(2, numberCollection.size());
        assertEquals(true, numberCollection.get(0));
    }

    @Test
    void getCollectionOfGuidPrimitiveValuesFromForm() {
        final String TestFormData =
                "ids=48d31887-5fad-4d73-a9f5-3c356e68a038&"
                        + "ids=48d31887-5fad-4d73-a9f5-3c356e68a038";
        final ParseNode numberNode = new FormParseNode(TestFormData).getChildNode("ids");
        var numberCollection = numberNode.getCollectionOfPrimitiveValues(UUID.class);
        assertNotNull(numberCollection);
        assertEquals(2, numberCollection.size());
        assertEquals(
                UUID.fromString("48d31887-5fad-4d73-a9f5-3c356e68a038"), numberCollection.get(0));
    }

    @Test
    void testParsesDateTimeOffset() {
        final var dateTimeOffsetString = "2024-02-12T19:47:39+02:00";
        final var result =
                new FormParseNode(URLEncoder.encode(dateTimeOffsetString, StandardCharsets.UTF_8))
                        .getOffsetDateTimeValue();
        assertEquals(dateTimeOffsetString, result.toString());
    }

    @Test
    void testParsesDateTimeStringWithoutOffsetToDateTimeOffset() {
        final var dateTimeString = "2024-02-12T19:47:39";
        final var result =
                new FormParseNode(URLEncoder.encode(dateTimeString, StandardCharsets.UTF_8))
                        .getOffsetDateTimeValue();
        assertEquals(dateTimeString + "Z", result.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-02-12T19:47:39 Europe/Paris", "19:47:39"})
    void testInvalidOffsetDateTimeStringThrowsException(final String dateTimeString) {
        try {
            new FormParseNode(URLEncoder.encode(dateTimeString, StandardCharsets.UTF_8))
                    .getOffsetDateTimeValue();
        } catch (final Exception ex) {
            assertInstanceOf(DateTimeParseException.class, ex);
            assertTrue(ex.getMessage().contains(dateTimeString));
        }
    }

    @Test
    void getCollectionOfStringPrimitiveValuesFromForm() {
        final String testFormData = "names=Alice&names=Bob";
        final ParseNode node = new FormParseNode(testFormData).getChildNode("names");
        final List<String> result = node.getCollectionOfPrimitiveValues(String.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0));
        assertEquals("Bob", result.get(1));
    }

    @Test
    void getCollectionOfIntegerPrimitiveValuesFromForm() {
        final String testFormData = "nums=1&nums=2&nums=3";
        final ParseNode node = new FormParseNode(testFormData).getChildNode("nums");
        final List<Integer> result = node.getCollectionOfPrimitiveValues(Integer.class);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
        assertEquals(3, result.get(2));
    }

    @Test
    void getCollectionOfLongPrimitiveValuesFromForm() {
        final String testFormData = "vals=100&vals=200";
        final ParseNode node = new FormParseNode(testFormData).getChildNode("vals");
        final List<Long> result = node.getCollectionOfPrimitiveValues(Long.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100L, result.get(0));
        assertEquals(200L, result.get(1));
    }

    @Test
    void getCollectionOfDoublePrimitiveValuesFromForm() {
        final String testFormData = "vals=1.5&vals=2.5";
        final ParseNode node = new FormParseNode(testFormData).getChildNode("vals");
        final List<Double> result = node.getCollectionOfPrimitiveValues(Double.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1.5, result.get(0));
        assertEquals(2.5, result.get(1));
    }

    @Test
    void getCollectionOfFloatPrimitiveValuesFromForm() {
        final String testFormData = "vals=1.5&vals=2.5";
        final ParseNode node = new FormParseNode(testFormData).getChildNode("vals");
        final List<Float> result = node.getCollectionOfPrimitiveValues(Float.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1.5f, result.get(0));
        assertEquals(2.5f, result.get(1));
    }

    @Test
    void getCollectionOfShortPrimitiveValuesFromForm() {
        final String testFormData = "vals=10&vals=20";
        final ParseNode node = new FormParseNode(testFormData).getChildNode("vals");
        final List<Short> result = node.getCollectionOfPrimitiveValues(Short.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals((short) 10, result.get(0));
        assertEquals((short) 20, result.get(1));
    }

    @Test
    void getCollectionOfBytePrimitiveValuesFromForm() {
        final String testFormData = "vals=1&vals=2";
        final ParseNode node = new FormParseNode(testFormData).getChildNode("vals");
        final List<Byte> result = node.getCollectionOfPrimitiveValues(Byte.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals((byte) 1, result.get(0));
        assertEquals((byte) 2, result.get(1));
    }

    @Test
    void getCollectionOfBigDecimalPrimitiveValuesFromForm() {
        final String testFormData = "vals=123.45&vals=678.90";
        final ParseNode node = new FormParseNode(testFormData).getChildNode("vals");
        final List<BigDecimal> result = node.getCollectionOfPrimitiveValues(BigDecimal.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("123.45"), result.get(0));
        assertEquals(new BigDecimal("678.90"), result.get(1));
    }

    @Test
    void getCollectionOfOffsetDateTimePrimitiveValuesFromForm() {
        final String dt1 = "2024-01-01T00:00:00Z";
        final String dt2 = "2024-06-15T12:30:00Z";
        final String testFormData =
                "vals="
                        + URLEncoder.encode(dt1, StandardCharsets.UTF_8)
                        + "&vals="
                        + URLEncoder.encode(dt2, StandardCharsets.UTF_8);
        final ParseNode node = new FormParseNode(testFormData).getChildNode("vals");
        final List<OffsetDateTime> result =
                node.getCollectionOfPrimitiveValues(OffsetDateTime.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(OffsetDateTime.parse(dt1), result.get(0));
        assertEquals(OffsetDateTime.parse(dt2), result.get(1));
    }

    @Test
    void getCollectionOfLocalDatePrimitiveValuesFromForm() {
        final String testFormData = "vals=2024-01-01&vals=2024-06-15";
        final ParseNode node = new FormParseNode(testFormData).getChildNode("vals");
        final List<LocalDate> result = node.getCollectionOfPrimitiveValues(LocalDate.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(LocalDate.parse("2024-01-01"), result.get(0));
        assertEquals(LocalDate.parse("2024-06-15"), result.get(1));
    }

    @Test
    void getCollectionOfLocalTimePrimitiveValuesFromForm() {
        final String testFormData = "vals=08:00:00&vals=17:30:00";
        final ParseNode node = new FormParseNode(testFormData).getChildNode("vals");
        final List<LocalTime> result = node.getCollectionOfPrimitiveValues(LocalTime.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(LocalTime.of(8, 0, 0), result.get(0));
        assertEquals(LocalTime.of(17, 30, 0), result.get(1));
    }

    @Test
    void getCollectionOfPeriodAndDurationPrimitiveValuesFromForm() {
        final String testFormData = "vals=P1M&vals=PT2H";
        final ParseNode node = new FormParseNode(testFormData).getChildNode("vals");
        final List<PeriodAndDuration> result =
                node.getCollectionOfPrimitiveValues(PeriodAndDuration.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(PeriodAndDuration.parse("P1M"), result.get(0));
        assertEquals(PeriodAndDuration.parse("PT2H"), result.get(1));
    }

    @Test
    void getCollectionOfPrimitiveValuesThrowsForUnknownType() {
        final String testFormData = "vals=foo";
        final ParseNode node = new FormParseNode(testFormData).getChildNode("vals");
        assertThrows(
                RuntimeException.class, () -> node.getCollectionOfPrimitiveValues(Object.class));
    }
}
