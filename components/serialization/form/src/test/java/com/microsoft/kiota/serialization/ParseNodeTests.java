package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.time.Period;

import org.junit.jupiter.api.Test;

import com.microsoft.kiota.serialization.mocks.TestEntity;

public class ParseNodeTests {
	private final String testUserForm = "displayName=Megan+Bowen&" +
                                        "numbers=one,two,thirtytwo&" +
                                        "givenName=Megan&" +
                                        "accountEnabled=true&" +
                                        "createdDateTime=2017-07-29T03:07:25Z&" +
                                        "jobTitle=Auditor&" +
                                        "mail=MeganB@M365x214355.onmicrosoft.com&" +
                                        "mobilePhone=null&" +
                                        "officeLocation=null&" +
                                        "preferredLanguage=en-US&" +
                                        "surname=Bowen&" +
                                        "workDuration=P1M&" +
                                        "startWorkTime=08:00:00&" +
                                        "endWorkTime=17:00:00&" +
                                        "userPrincipalName=MeganB@M365x214355.onmicrosoft.com&" +
                                        "birthDay=2017-09-04&" +
                                        "id=48d31887-5fad-4d73-a9f5-3c356e68a038";
	@Test
	public void getsEntityValueFromForm() {
		final FormParseNode parseNode = new FormParseNode(testUserForm);
		final TestEntity entity = parseNode.getObjectValue(TestEntity::createFromDiscriminatorValue);
		assertNotNull(entity);
		assertNull(entity.getOfficeLocation());
		assertTrue(entity.getAdditionalData().containsKey("jobTitle"));
		assertTrue(entity.getAdditionalData().containsKey("mobilePhone"));
		assertEquals("Auditor", entity.getAdditionalData().get("jobTitle"));
		assertEquals("48d31887-5fad-4d73-a9f5-3c356e68a038", entity.getId());
		assertEquals(Period.parse("P1M"), entity.getWorkDuration());
		assertEquals(LocalTime.of(8, 0, 0, 0), entity.getStartWorkTime());
		assertEquals(LocalTime.of(17, 0, 0, 0), entity.getEndWorkTime());
		assertEquals("2017-09-04", entity.getBirthDay().toString());
	}
	@Test
	public void getCollectionOfObjectValuesFromForm() {
		final FormParseNode parseNode = new FormParseNode(testUserForm);
		assertThrows(RuntimeException.class, () ->  parseNode.getCollectionOfObjectValues(TestEntity::createFromDiscriminatorValue));
	}
	@Test
	public void returnsDefaultIfChildNodeDoesNotExist() {
		final FormParseNode parseNode = new FormParseNode(testUserForm);
		final ParseNode childNode = parseNode.getChildNode("doesNotExist");
		assertNull(childNode);
	}
}
