package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.kiota.Compatibility;
import com.microsoft.kiota.serialization.mocks.UntypedTestEntity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

public class JsonSerializationWriterTests {

    @Test
    public void WritesSampleObjectValueWithUntypedProperties() throws IOException {
        // Arrange
        var untypedTestEntity = new UntypedTestEntity();
        untypedTestEntity.setId("1");
        var locationObject =
                new UntypedObject(
                        new HashMap<>() {
                            {
                                put("displayName", new UntypedString("Microsoft Building 92"));
                                put("floorCount", new UntypedInteger(50));
                                put("hasReception", new UntypedBoolean(true));
                                put("contact", new UntypedNull());
                            }
                        });
        untypedTestEntity.setLocation(locationObject);

        var jsonSerializer = new JsonSerializationWriter();
        jsonSerializer.writeObjectValue("", untypedTestEntity);
        var contentStream = jsonSerializer.getSerializedContent();
        var serializedJsonString = new String(Compatibility.readAllBytes(contentStream), "UTF-8");

        // Assert
        var expectedString =
                "{\"id\":\"1\",\"title\":\"Title\","
                    + "\"location\":{\"address\":{\"city\":\"Redmond\",\"postalCode\":\"98052\",\"state\":\"Washington\",\"street\":\"NE"
                    + " 36th St\"},"
                    + "\"coordinates\":{\"latitude\":47.641942,\"longitude\":-122.127222},\"displayName\":\"Microsoft"
                    + " Building 92\",\"floorCount\":50,\"hasReception\":true,\"contact\":null},"
                    + "\"keywords\":["
                    + "{\"created\":\"2023-07-26T10:41:26Z\",\"label\":\"Keyword1\",\"termGuid\":\"10e9cc83-b5a4-4c8d-8dab-4ada1252dd70\",\"wssId\":6442450941},"
                    + "{\"created\":\"2023-07-26T10:51:26Z\",\"label\":\"Keyword2\",\"termGuid\":\"2cae6c6a-9bb8-4a78-afff-81b88e735fef\",\"wssId\":6442450942}],"
                    + "\"extra\":{\"createdDateTime\":\"2024-01-15T00:00:00\\u002B00:00\"}}";
        assertEquals(expectedString, serializedJsonString);
    }
}
