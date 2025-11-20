package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.microsoft.kiota.serialization.mocks.MyEnum;
import com.microsoft.kiota.serialization.mocks.TestEntity;
import com.microsoft.kiota.serialization.mocks.UntypedTestEntity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

class JsonParseNodeTests {
    private static final JsonParseNodeFactory _parseNodeFactory = new JsonParseNodeFactory();
    private static final String contentType = "application/json";

    private static final String testJsonString =
            "{\"displayName\":\"My"
                + " Group\",\"phones\":[\"+1234567890\"],\"myEnum\":\"VALUE1\",\"enumCollection\":[\"VALUE1\"],\"id\":\"11111111-1111-1111-1111-111111111111"
                + "\",\"members@delta\":[{\"@odata.type\":\"#microsoft.graph.user\",\"id\":\"22222222-2222-2222-2222-222222222222\"}]}";
    private static final String testUntypedJson =
            "{\r\n"
                + "    \"@odata.context\":"
                + " \"https://graph.microsoft.com/v1.0/$metadata#sites('contoso.sharepoint.com')/lists('fa631c4d-ac9f-4884-a7f5-13c659d177e3')/items('1')/fields/$entity\",\r\n"
                + "    \"id\": \"5\",\r\n"
                + "    \"title\": \"Project 101\",\r\n"
                + "    \"location\": {\r\n"
                + "        \"address\": {\r\n"
                + "            \"city\": \"Redmond\",\r\n"
                + "            \"postalCode\": \"98052\",\r\n"
                + "            \"state\": \"Washington\",\r\n"
                + "            \"street\": \"NE 36th St\"\r\n"
                + "        },\r\n"
                + "        \"coordinates\": {\r\n"
                + "            \"latitude\": 47.641942,\r\n"
                + "            \"longitude\": -122.127222\r\n"
                + "        },\r\n"
                + "        \"displayName\": \"Microsoft Building 92\",\r\n"
                + "        \"floorCount\": 50,\r\n"
                + "        \"hasReception\": true,\r\n"
                + "        \"contact\": null\r\n"
                + "    },\r\n"
                + "    \"keywords\": [\r\n"
                + "        {\r\n"
                + "            \"created\": \"2023-07-26T10:41:26Z\",\r\n"
                + "            \"label\": \"Keyword1\",\r\n"
                + "            \"termGuid\": \"10e9cc83-b5a4-4c8d-8dab-4ada1252dd70\",\r\n"
                + "            \"wssId\": 6442450942\r\n"
                + "        },\r\n"
                + "        {\r\n"
                + "            \"created\": \"2023-07-26T10:51:26Z\",\r\n"
                + "            \"label\": \"Keyword2\",\r\n"
                + "            \"termGuid\": \"2cae6c6a-9bb8-4a78-afff-81b88e735fef\",\r\n"
                + "            \"wssId\": 6442450943\r\n"
                + "        }\r\n"
                + "    ],\r\n"
                + "    \"detail\": null,\r\n"
                + "    \"table\": [[1,2,3],[4,5,6],[7,8,9]],\r\n"
                + "    \"extra\": {\r\n"
                + "        \"createdDateTime\":\"2024-01-15T00:00:00\\u002B00:00\"\r\n"
                + "    }\r\n"
                + "}";

    public static final DateTimeFormatter customFormatter =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .parseLenient()
                    .appendOffset("+HHmm", "+0000")
                    .parseStrict()
                    .toFormatter();

    public static final Gson customGson =
            DefaultGsonBuilder.getDefaultBuilder()
                    .registerTypeAdapter(
                            OffsetDateTime.class,
                            new TypeAdapter<OffsetDateTime>() {
                                @Override
                                public OffsetDateTime read(JsonReader in) throws IOException {
                                    String stringValue = in.nextString();
                                    try {
                                        return customFormatter.parse(
                                                stringValue, OffsetDateTime::from);
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
                                public void write(JsonWriter out, OffsetDateTime value)
                                        throws IOException {
                                    out.value(customFormatter.format(value));
                                }
                            }.nullSafe())
                    .create();

    @Test
    void itDDoesNotFailForGetChildElementOnMissingKey() throws UnsupportedEncodingException {
        final var initialString = "{displayName\": \"Microsoft Teams Meeting\"}";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var result = parseNode.getChildNode("@odata.type");
        assertNull(result);
    }

    @Test
    void testParsesDateTimeOffset() {
        final var dateTimeOffsetString = "2024-02-12T19:47:39+02:00";
        final var jsonElement = JsonParser.parseString("\"" + dateTimeOffsetString + "\"");
        final var result =
                new JsonParseNode(jsonElement, DefaultGsonBuilder.getDefaultInstance())
                        .getOffsetDateTimeValue();
        assertEquals(dateTimeOffsetString, result.toString());
    }

    @Test
    void testParsesDateTimeStringWithoutOffsetToDateTimeOffset() {
        final var dateTimeString = "2024-02-12T19:47:39";
        final var jsonElement = JsonParser.parseString("\"" + dateTimeString + "\"");
        final var result =
                new JsonParseNode(jsonElement, DefaultGsonBuilder.getDefaultInstance())
                        .getOffsetDateTimeValue();
        assertEquals(dateTimeString + "Z", result.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-02-12T19:47:39 Europe/Paris", "19:47:39"})
    void testInvalidOffsetDateTimeStringThrowsException(final String dateTimeString) {
        final var jsonElement = JsonParser.parseString("\"" + dateTimeString + "\"");
        try {
            new JsonParseNode(jsonElement, DefaultGsonBuilder.getDefaultInstance())
                    .getOffsetDateTimeValue();
        } catch (final Exception ex) {
            assertInstanceOf(JsonSyntaxException.class, ex);
            assertTrue(ex.getMessage().contains(dateTimeString));
        }
    }

    @Test
    void testNonStandardOffsetDateTimeParsing() {
        final var dateTimeString = "2024-02-12T19:47:39+0000";
        final var jsonElement = JsonParser.parseString("\"" + dateTimeString + "\"");
        final var parsedOffsetDateTime =
                new JsonParseNode(jsonElement, customGson).getOffsetDateTimeValue();
        assertEquals(OffsetDateTime.parse("2024-02-12T19:47:39+00:00"), parsedOffsetDateTime);
    }

    @Test
    void getEntityWithArrayInAdditionalData() throws UnsupportedEncodingException {
        final var rawResponse = new ByteArrayInputStream(testJsonString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        // Act
        var entity = parseNode.getObjectValue(TestEntity::createFromDiscriminatorValue);
        assertEquals("11111111-1111-1111-1111-111111111111", entity.getId());
        assertEquals(1, entity.getPhones().size());
        assertEquals(MyEnum.MY_VALUE1, entity.getMyEnum());
        assertEquals(1, entity.getEnumCollection().size());
        final var arrayValue = (UntypedArray) entity.getAdditionalData().get("members@delta");
        assertEquals(1, arrayValue.getValue().spliterator().estimateSize());
    }

    @Test
    void GetEntityWithUntypedNodesFromJson() throws UnsupportedEncodingException {
        final var rawResponse = new ByteArrayInputStream(testUntypedJson.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        // Act
        var entity = parseNode.getObjectValue(UntypedTestEntity::createFromDiscriminatorValue);
        // Assert
        assertNotNull(entity);
        assertEquals("5", entity.getId());
        assertNotNull(entity.getLocation());
        assertInstanceOf(UntypedObject.class, entity.getLocation()); // creates untyped object
        var location = (UntypedObject) entity.getLocation();
        var locationProperties = location.getValue();
        assertInstanceOf(UntypedObject.class, locationProperties.get("address"));
        assertInstanceOf(
                UntypedString.class,
                (locationProperties.get("displayName"))); // creates untyped string
        assertInstanceOf(
                UntypedDouble.class,
                (locationProperties.get("floorCount"))); // creates untyped number
        assertInstanceOf(
                UntypedBoolean.class,
                locationProperties.get("hasReception")); // creates untyped boolean
        assertInstanceOf(
                UntypedNull.class, locationProperties.get("contact")); // creates untyped null
        assertInstanceOf(
                UntypedObject.class, locationProperties.get("coordinates")); // creates untyped null
        var coordinates = (UntypedObject) locationProperties.get("coordinates");
        var coordinatesProperties = coordinates.getValue();
        assertInstanceOf(
                UntypedDouble.class,
                coordinatesProperties.get("latitude")); // creates untyped decimal
        assertInstanceOf(UntypedDouble.class, coordinatesProperties.get("longitude"));
        assertEquals(
                "Microsoft Building 92",
                ((UntypedString) locationProperties.get("displayName")).getValue());
        assertEquals(50, ((UntypedDouble) locationProperties.get("floorCount")).getValue());
        assertTrue(((UntypedBoolean) locationProperties.get("hasReception")).getValue());
        assertNull((locationProperties.get("contact")).getValue());
        assertNotNull(entity.getKeywords());
        assertInstanceOf(UntypedArray.class, entity.getKeywords()); // creates untyped array
        assertNull(entity.getDetail());
        var extra = entity.getAdditionalData().get("extra");
        assertNotNull(extra);
        assertNotNull(entity.getTable());
        var table = (UntypedArray) entity.getTable(); // the table is a collection
        for (var value : table.getValue()) {
            var row = (UntypedArray) value;
            assertNotNull(row); // The values are a nested collection
            for (var item : row.getValue()) {
                var rowItem = (UntypedDouble) item;
                assertNotNull(rowItem); // The values are UntypedInteger
            }
        }
    }

    @Test
    void getCollectionOfPrimitiveDoubleValues() throws UnsupportedEncodingException {
        final var initialString = "{\"values\":[1.1,2.2,3.3]}";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var valuesNode = parseNode.getChildNode("values");
        assertNotNull(valuesNode);
        final var doubles = valuesNode.getCollectionOfPrimitiveValues(Double.class);
        assertNotNull(doubles);
        assertEquals(3, doubles.size());
        assertEquals(1.1, doubles.get(0), 0.000001);
        assertEquals(2.2, doubles.get(1), 0.000001);
        assertEquals(3.3, doubles.get(2), 0.000001);
    }
}
