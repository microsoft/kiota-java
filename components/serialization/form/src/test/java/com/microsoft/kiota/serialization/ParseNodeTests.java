package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.kiota.PeriodAndDuration;
import com.microsoft.kiota.serialization.mocks.TestEntity;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
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
    public void getsEntityValueFromForm() {
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
    public void getCollectionOfObjectValuesFromForm() {
        final FormParseNode parseNode = new FormParseNode(testUserForm);
        assertThrows(
                RuntimeException.class,
                () ->
                        parseNode.getCollectionOfObjectValues(
                                TestEntity::createFromDiscriminatorValue));
    }

    @Test
    public void returnsDefaultIfChildNodeDoesNotExist() {
        final FormParseNode parseNode = new FormParseNode(testUserForm);
        final ParseNode childNode = parseNode.getChildNode("doesNotExist");
        assertNull(childNode);
    }

    @Test
    public void getCollectionOfBooleanPrimitiveValuesFromForm() {
        final String TestFormData = "bools=true&" + "bools=false";
        final ParseNode numberNode = new FormParseNode(TestFormData).getChildNode("bools");
        final List<Boolean> numberCollection =
                numberNode.getCollectionOfPrimitiveValues(Boolean.class);
        assertNotNull(numberCollection);
        assertEquals(2, numberCollection.size());
        assertEquals(true, numberCollection.get(0));
    }

    @Test
    public void getCollectionOfGuidPrimitiveValuesFromForm() {
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
}
