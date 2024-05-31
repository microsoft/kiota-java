package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.kiota.Compatibility;
import com.microsoft.kiota.serialization.mocks.MyEnum;
import com.microsoft.kiota.serialization.mocks.TestEntity;
import com.microsoft.kiota.serialization.mocks.UntypedTestEntity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

class JsonSerializationWriterTests {

    @Test
    void writesSampleObjectValueWithParsableInAddtionalData() throws IOException {
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

        var jsonSerializer = new JsonSerializationWriter();
        jsonSerializer.writeObjectValue("", testEntity);
        var contentStream = jsonSerializer.getSerializedContent();
        var serializedJsonString = new String(Compatibility.readAllBytes(contentStream), "UTF-8");
        // Assert
        var expectedString =
                "{\"id\":\"test_id\",\"phones\":[\"123456789\"],\"manager\":{\"id\":\"manager_id\",\"myEnum\":\"VALUE1\"}}";
        assertEquals(expectedString, serializedJsonString);
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

        var jsonSerializer = new JsonSerializationWriter();
        jsonSerializer.writeObjectValue("", untypedTestEntity);
        var contentStream = jsonSerializer.getSerializedContent();
        var serializedJsonString = new String(Compatibility.readAllBytes(contentStream), "UTF-8");

        // Assert
        var expectedString =
                "{\"id\":\"1\","
                    + "\"location\":{\"hasReception\":true,\"address\":{\"state\":\"Washington\",\"city\":\"Redmond\",\"street\":\"NE"
                    + " 36th St\",\"postalCode\":\"98052\"},\"displayName\":\"Microsoft Building"
                    + " 92\",\"floorCount\":50,\"contact\":null,\"coordinates\":{\"latitude\":47.641942,\"longitude\":-122.127222}},"
                    + "\"keywords\":["
                    + "{\"wssId\":345345345,\"label\":\"Keyword1\",\"termGuid\":\"10e9cc83-b5a4-4c8d-8dab-4ada1252dd70\",\"created\":\"2023-07-26T10:41:26Z\"},"
                    + "{\"wssId\":345345345,\"label\":\"Keyword2\",\"termGuid\":\"2cae6c6a-9bb8-4a78-afff-81b88e735fef\",\"created\":\"2023-07-26T10:51:26Z\"}],"
                    + "\"extra\":{\"createdDateTime\":\"2024-01-15T00:00:00+00:00\"}}";
        assertEquals(expectedString, serializedJsonString);
    }
}
