package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.microsoft.kiota.Compatibility;
import com.microsoft.kiota.PeriodAndDuration;
import com.microsoft.kiota.serialization.mocks.MyEnum;
import com.microsoft.kiota.serialization.mocks.TestEntity;
import com.microsoft.kiota.serialization.mocks.UntypedTestEntity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

class JsonSerializationWriterTests {

    @Test
    void writesSampleObjectValueWithPrimitivesInAdditionalData() throws IOException {
        var testEntity = new TestEntity();

        Double accountBalance = 330.7;
        testEntity
                .getAdditionalData()
                .put("accountBalance", accountBalance); // place a double in the additional data

        testEntity
                .getAdditionalData()
                .put("nickName", "Peter Pan"); // place a string in the additional data

        int reportsCount = 4;
        testEntity
                .getAdditionalData()
                .put("reportsCount", reportsCount); // place a int in the additional data

        float averageScore = 78.142F;
        testEntity
                .getAdditionalData()
                .put("averageScore", averageScore); // place a float in the additional data

        boolean hasDependents = true;
        testEntity
                .getAdditionalData()
                .put("hasDependents", hasDependents); // place a bool in the additional data

        List<String> aliases = new ArrayList<>();
        aliases.add("alias1");
        aliases.add("alias2");

        testEntity
                .getAdditionalData()
                .put("aliases", aliases); // place a collection in the additional data

        try (final var jsonSerializer =
                new JsonSerializationWriter(DefaultGsonBuilder.getDefaultInstance())) {
            jsonSerializer.writeObjectValue("", testEntity);
            var contentStream = jsonSerializer.getSerializedContent();
            var serializedJsonString =
                    new String(Compatibility.readAllBytes(contentStream), "UTF-8");
            // Assert
            var expectedString =
                    "{\"aliases\":[\"alias1\",\"alias2\"],\"nickName\":\"Peter"
                        + " Pan\",\"hasDependents\":true,\"accountBalance\":330.7,\"reportsCount\":4,\"averageScore\":78.142}";
            assertEquals(expectedString, serializedJsonString);
        }
    }

    @Test
    void useNonStandardOffsetDateTimeFormat() throws IOException {
        var testEntity = new TestEntity();
        testEntity.setCreatedDateTime(OffsetDateTime.parse("2024-02-12T19:47:39+00:00"));
        try (final var jsonSerializer =
                new JsonSerializationWriter(JsonParseNodeTests.customGson)) {
            jsonSerializer.writeObjectValue("", testEntity);
            var contentStream = jsonSerializer.getSerializedContent();
            var serializedJsonString =
                    new String(Compatibility.readAllBytes(contentStream), "UTF-8");
            // Assert
            var expectedString = "{\"createdDateTime\":\"2024-02-12T19:47:39+0000\"}";
            assertEquals(expectedString, serializedJsonString);
        }
    }

    @Test
    void writesSampleObjectValueWithParsableInAdditionalData() throws IOException {
        var testEntity = new TestEntity();
        testEntity.setId("test_id");
        var phones = new ArrayList<String>();
        phones.add("123456789");
        testEntity.setPhones(phones);
        var managerAdditionalData = new TestEntity();
        managerAdditionalData.setId("manager_id");
        managerAdditionalData.setMyEnum(MyEnum.MY_VALUE1);

        testEntity
                .getAdditionalData()
                .put("manager", managerAdditionalData); // place a parsable in the addtionaldata

        try (final var jsonSerializer =
                new JsonSerializationWriter(DefaultGsonBuilder.getDefaultInstance())) {
            jsonSerializer.writeObjectValue("", testEntity);
            var contentStream = jsonSerializer.getSerializedContent();
            var serializedJsonString =
                    new String(Compatibility.readAllBytes(contentStream), "UTF-8");
            // Assert
            var expectedString =
                    "{\"id\":\"test_id\",\"phones\":[\"123456789\"],\"manager\":{\"id\":\"manager_id\",\"myEnum\":\"VALUE1\"}}";
            assertEquals(expectedString, serializedJsonString);
        }
    }

    @Test
    void writesSampleObjectValueWithUntypedProperties() throws IOException {
        // Arrange
        var untypedTestEntity = new UntypedTestEntity();
        untypedTestEntity.setId("1");
        var locationObject =
                new UntypedObject(
                        new HashMap<>() {
                            {
                                put(
                                        "address",
                                        new UntypedObject(
                                                new HashMap<>() {
                                                    {
                                                        put("city", new UntypedString("Redmond"));
                                                        put(
                                                                "postalCode",
                                                                new UntypedString("98052"));
                                                        put(
                                                                "state",
                                                                new UntypedString("Washington"));
                                                        put(
                                                                "street",
                                                                new UntypedString("NE 36th St"));
                                                    }
                                                }));
                                put(
                                        "coordinates",
                                        new UntypedObject(
                                                new HashMap<>() {
                                                    {
                                                        put(
                                                                "latitude",
                                                                new UntypedDouble(47.641942d));
                                                        put(
                                                                "longitude",
                                                                new UntypedDouble(-122.127222d));
                                                    }
                                                }));
                                put("displayName", new UntypedString("Microsoft Building 92"));
                                put("floorCount", new UntypedInteger(50));
                                put("hasReception", new UntypedBoolean(true));
                                put("contact", new UntypedNull());
                            }
                        });
        untypedTestEntity.setLocation(locationObject);

        var keyWordsCollection =
                new UntypedArray(
                        Arrays.asList(
                                new UntypedObject(
                                        new HashMap<>() {
                                            {
                                                put(
                                                        "created",
                                                        new UntypedString("2023-07-26T10:41:26Z"));
                                                put("label", new UntypedString("Keyword1"));
                                                put(
                                                        "termGuid",
                                                        new UntypedString(
                                                                "10e9cc83-b5a4-4c8d-8dab-4ada1252dd70"));
                                                put("wssId", new UntypedLong(345345345L));
                                            }
                                        }),
                                new UntypedObject(
                                        new HashMap<>() {
                                            {
                                                put(
                                                        "created",
                                                        new UntypedString("2023-07-26T10:51:26Z"));
                                                put("label", new UntypedString("Keyword2"));
                                                put(
                                                        "termGuid",
                                                        new UntypedString(
                                                                "2cae6c6a-9bb8-4a78-afff-81b88e735fef"));
                                                put("wssId", new UntypedLong(345345345L));
                                            }
                                        })));

        untypedTestEntity.setKeywords(keyWordsCollection);
        untypedTestEntity
                .getAdditionalData()
                .put(
                        "extra",
                        new UntypedObject(
                                new HashMap<>() {
                                    {
                                        put(
                                                "createdDateTime",
                                                new UntypedString("2024-01-15T00:00:00+00:00"));
                                    }
                                }));

        try (final var jsonSerializer =
                new JsonSerializationWriter(DefaultGsonBuilder.getDefaultInstance())) {
            jsonSerializer.writeObjectValue("", untypedTestEntity);
            var contentStream = jsonSerializer.getSerializedContent();
            var serializedJsonString =
                    new String(Compatibility.readAllBytes(contentStream), "UTF-8");

            // Assert
            var expectedString =
                    "{\"id\":\"1\","
                        + "\"location\":{\"hasReception\":true,\"coordinates\":{\"latitude\":47.641942,\"longitude\":-122.127222},"
                        + "\"address\":{\"state\":\"Washington\",\"city\":\"Redmond\",\"street\":\"NE"
                        + " 36th St\",\"postalCode\":\"98052\"},\"displayName\":\"Microsoft"
                        + " Building 92\",\"floorCount\":50,\"contact\":null},\"keywords\":["
                        + "{\"wssId\":345345345,\"label\":\"Keyword1\",\"termGuid\":\"10e9cc83-b5a4-4c8d-8dab-4ada1252dd70\",\"created\":\"2023-07-26T10:41:26Z\"},"
                        + "{\"wssId\":345345345,\"label\":\"Keyword2\",\"termGuid\":\"2cae6c6a-9bb8-4a78-afff-81b88e735fef\",\"created\":\"2023-07-26T10:51:26Z\"}],"
                        + "\"extra\":{\"createdDateTime\":\"2024-01-15T00:00:00+00:00\"}}";
            assertEquals(expectedString, serializedJsonString);
        }
    }

    @Test
    void parseWrittenValues() throws IOException {
        writeAndParse(
                ParseNode::getStringValue, SerializationWriter::writeStringValue, "just a string");
        writeAndParse(ParseNode::getBooleanValue, SerializationWriter::writeBooleanValue, true);
        writeAndParse(ParseNode::getByteValue, SerializationWriter::writeByteValue, (byte) 3);
        writeAndParse(ParseNode::getShortValue, SerializationWriter::writeShortValue, (short) 42);
        writeAndParse(
                ParseNode::getBigDecimalValue,
                SerializationWriter::writeBigDecimalValue,
                new BigDecimal(123456789L));
        writeAndParse(ParseNode::getIntegerValue, SerializationWriter::writeIntegerValue, 54321);
        writeAndParse(
                ParseNode::getFloatValue, SerializationWriter::writeFloatValue, (float) 67.89);
        writeAndParse(ParseNode::getDoubleValue, SerializationWriter::writeDoubleValue, 3245.12356);
        writeAndParse(ParseNode::getLongValue, SerializationWriter::writeLongValue, -543219876L);
        writeAndParse(
                ParseNode::getUUIDValue, SerializationWriter::writeUUIDValue, UUID.randomUUID());
        writeAndParse(
                ParseNode::getOffsetDateTimeValue,
                SerializationWriter::writeOffsetDateTimeValue,
                OffsetDateTime.now());
        writeAndParse(
                ParseNode::getLocalDateValue,
                SerializationWriter::writeLocalDateValue,
                LocalDate.now());
        writeAndParse(
                ParseNode::getLocalTimeValue,
                SerializationWriter::writeLocalTimeValue,
                LocalTime.now());
        writeAndParse(
                ParseNode::getPeriodAndDurationValue,
                SerializationWriter::writePeriodAndDurationValue,
                PeriodAndDuration.of(Period.ofYears(3), Duration.ofHours(6)));
    }

    private <T> void writeAndParse(
            TestParsable.ParseMethod<T> parseMethod,
            TestParsable.WriteMethod<T> writeMethod,
            T value)
            throws IOException {
        var testParsable = new TestParsable<>(parseMethod, writeMethod, value);
        var writer = new JsonSerializationWriter(DefaultGsonBuilder.getDefaultInstance());
        writer.writeObjectValue(null, testParsable);

        var parseNodeFactory = new JsonParseNodeFactory(DefaultGsonBuilder.getDefaultInstance());
        var parseNode =
                parseNodeFactory.getParseNode("application/json", writer.getSerializedContent());
        var result = parseNode.getObjectValue(TestParsable.factory(parseMethod, writeMethod));
        assertEquals(value, result.getRealValue());
        assertNull(result.getNullValue());
        writer.close();
    }
}
