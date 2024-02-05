package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.*;

import com.microsoft.kiota.serialization.mocks.UntypedTestEntity;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

class JsonParseNodeTests {
    private static final JsonParseNodeFactory _parseNodeFactory = new JsonParseNodeFactory();
    private static final String contentType = "application/json";
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
                + "    \"extra\": {\r\n"
                + "        \"createdDateTime\":\"2024-01-15T00:00:00\\u002B00:00\"\r\n"
                + "    }\r\n"
                + "}";

    @Test
    void itDDoesNotFailForGetChildElementOnMissingKey() throws UnsupportedEncodingException {
        final var initialString = "{displayName\": \"Microsoft Teams Meeting\"}";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var result = parseNode.getChildNode("@odata.type");
        assertNull(result);
    }

    @Test
    public void GetEntityWithUntypedNodesFromJson() throws UnsupportedEncodingException {
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
        assertNull(((UntypedNull) locationProperties.get("contact")).getValue());
        assertNotNull(entity.getKeywords());
        assertInstanceOf(UntypedArray.class, entity.getKeywords()); // creates untyped array
        assertNull(entity.getDetail());
        var extra = entity.getAdditionalData().get("extra");
        assertNotNull(extra);
    }
}
