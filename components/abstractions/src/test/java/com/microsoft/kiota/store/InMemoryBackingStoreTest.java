package com.microsoft.kiota.store;

import static org.junit.jupiter.api.Assertions.*;

import com.microsoft.kiota.TestEntity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

class InMemoryBackingStoreTest {
    @Test
    void SetsAndGetsValueFromStore() {
        // Arrange
        var testBackingStore = new InMemoryBackingStore();
        // Act
        assertTrue(testBackingStore.enumerate().isEmpty());
        testBackingStore.set("name", "Peter");
        // Assert
        assertFalse(testBackingStore.enumerate().isEmpty());
        assertEquals("Peter", testBackingStore.enumerate().values().toArray()[0]);
    }

    @Test
    void PreventsDuplicatesInStore() {
        // Arrange
        var testBackingStore = new InMemoryBackingStore();
        // Act
        assertTrue(testBackingStore.enumerate().isEmpty());
        testBackingStore.set("name", "Peter");
        testBackingStore.set("name", "Peter Pan"); // modify a second time
        // Assert
        assertFalse(testBackingStore.enumerate().isEmpty());
        assertFalse(testBackingStore.enumerate().isEmpty());
        assertEquals("Peter Pan", testBackingStore.enumerate().values().toArray()[0]);
    }

    @Test
    void EnumeratesValuesChangedToNullInStore() {
        // Arrange
        var testBackingStore = new InMemoryBackingStore();
        // Act
        assertTrue(testBackingStore.enumerate().isEmpty());
        testBackingStore.set("name", "Peter Pan");
        testBackingStore.set("email", "peterpan@neverland.com");
        testBackingStore.set("phone", null); // phone changes to null
        // Assert
        assertFalse(testBackingStore.enumerate().isEmpty());
        assertEquals(
                1,
                testBackingStore
                        .enumerateKeysForValuesChangedToNull()
                        .spliterator()
                        .getExactSizeIfKnown());
        assertEquals(3, testBackingStore.enumerate().size()); // all values come back
        assertEquals(
                "phone",
                testBackingStore
                        .enumerateKeysForValuesChangedToNull()
                        .iterator()
                        .next()); // first item
    }

    @Test
    void RemovesValuePreviouslyInStore() {
        // Arrange
        var testBackingStore = new InMemoryBackingStore();
        // Act
        testBackingStore.set("name", "Peter Pan");
        assertFalse(testBackingStore.enumerate().isEmpty());
        testBackingStore.remove("name");
        // Assert
        assertNull(testBackingStore.get("name"));
        assertTrue(testBackingStore.enumerate().isEmpty());
    }

    @Test
    void TestsBackingStoreEmbeddedInModel() {
        // Arrange dummy user with initialized backingstore
        var testUser = new TestEntity();
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe");
        testUser.getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change
        testUser.setBusinessPhones(Arrays.asList(new String[] {"+1 234 567 891"}));
        // Assert by retrieving only changed values
        testUser.getBackingStore().setReturnOnlyChangedValues(true);
        var changedValues = testUser.getBackingStore().enumerate();
        assertFalse(changedValues.isEmpty());
        assertEquals(1, changedValues.size());
        assertEquals("businessPhones", changedValues.keySet().toArray()[0]);
    }

    @Test
    void TestsBackingStoreEmbeddedInModelWithAdditionDataValues() {
        // Arrange dummy user with initialized backingstore
        var testUser = new TestEntity();
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe");
        testUser.getAdditionalData().put("extensionData", null);
        testUser.getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change to a property and additionalData
        testUser.setBusinessPhones(Arrays.asList(new String[] {"+1 234 567 891"}));
        testUser.getAdditionalData().put("anotherExtension", null);
        // Assert by retrieving only changed values
        testUser.getBackingStore().setReturnOnlyChangedValues(true);
        var changedValues = testUser.getBackingStore().enumerate();
        assertFalse(changedValues.isEmpty());
        assertEquals(2, changedValues.size());
        assertTrue(changedValues.containsKey("businessPhones"));
        assertTrue(changedValues.containsKey("additionalData"));
    }

    @Test
    void TestsBackingStoreEmbeddedInModelWithCollectionPropertyReplacedWithNewCollection() {
        // Arrange dummy user with initialized backingstore
        var testUser = new TestEntity();
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe");
        testUser.getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change
        testUser.setBusinessPhones(Arrays.asList(new String[] {"+1 234 567 891"}));
        testUser.getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change
        testUser.setBusinessPhones(Arrays.asList(new String[] {"+1 234 567 891"}));
        // Assert by retrieving only changed values
        testUser.getBackingStore().setReturnOnlyChangedValues(true);
        var changedValues = testUser.getBackingStore().enumerate();
        assertFalse(changedValues.isEmpty());
        assertEquals(1, changedValues.size());
        assertEquals("businessPhones", changedValues.keySet().toArray()[0]);
        var businessPhones = testUser.getBackingStore().get("businessPhones");
        assertNotNull(businessPhones);
        assertEquals(1, ((List<?>) businessPhones).size());
    }

    @Test
    void TestsBackingStoreEmbeddedInModelWithCollectionPropertyReplacedWithNull() {
        // Arrange dummy user with initialized backingstore
        var testUser = new TestEntity();
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe");
        testUser.getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change
        testUser.setBusinessPhones(Arrays.asList(new String[] {"+1 234 567 891"}));
        testUser.getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change
        testUser.setBusinessPhones(null);
        // Assert by retrieving only changed values
        testUser.getBackingStore().setReturnOnlyChangedValues(true);
        var changedValues = testUser.getBackingStore().enumerate();
        assertFalse(changedValues.isEmpty());
        assertEquals(1, changedValues.size());
        assertEquals("businessPhones", changedValues.keySet().toArray()[0]);
        var changedValuesToNull = testUser.getBackingStore().enumerateKeysForValuesChangedToNull();
        assertTrue(changedValuesToNull.iterator().hasNext());
        assertEquals("businessPhones", changedValues.keySet().toArray()[0]);
        assertNull(changedValues.values().toArray()[0]);
    }

    @Test
    void TestsBackingStoreEmbeddedInModelWithCollectionPropertyModifiedByAdd() {
        // Arrange dummy user with initialized backingstore
        var testUser = new TestEntity();
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe");
        testUser.getAdditionalData().put("extensionData", null);
        testUser.getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change to a property and additionalData
        ArrayList<String> phonesList = new ArrayList<String>();
        phonesList.add("+1 234 567 891");
        testUser.setBusinessPhones(phonesList);
        // Act on the data by making a change
        testUser.getBusinessPhones().add("+1 234 567 891");
        // Assert by retrieving only changed values
        testUser.getBackingStore().setReturnOnlyChangedValues(true);
        var changedValues = testUser.getBackingStore().enumerate();
        assertFalse(changedValues.isEmpty());
        assertEquals(1, changedValues.size());
        assertEquals("businessPhones", changedValues.keySet().toArray()[0]);
        var businessPhones = testUser.getBackingStore().get("businessPhones");
        assertNotNull(businessPhones);
        assertEquals(2, ((List<?>) businessPhones).size());
    }

    @Test
    void TestsBackingStoreEmbeddedInModelWithBySettingNestedIBackedModel() {
        // Arrange dummy user with initialized backingstore
        var testUser = new TestEntity();
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe");
        testUser.getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change
        var manager = new TestEntity();
        manager.setId("2fe22fe5-1132-42cf-90f9-1dc17e325a74");
        testUser.setManager(manager);
        // Assert by retrieving only changed values
        testUser.getBackingStore().setReturnOnlyChangedValues(true);
        var changedValues = testUser.getBackingStore().enumerate();
        assertFalse(changedValues.isEmpty());
        assertEquals(1, changedValues.size());
        assertEquals("manager", changedValues.keySet().toArray()[0]);
        var changedManager = (TestEntity) changedValues.values().toArray()[0];
        assertNotNull(changedManager);
        assertEquals("2fe22fe5-1132-42cf-90f9-1dc17e325a74", changedManager.getId());
    }

    @Test
    void TestsBackingStoreEmbeddedInModelWithByUpdatingNestedIBackedModel() {
        // Arrange dummy user with initialized backingstore
        var testUser = new TestEntity();
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe");
        var manager = new TestEntity();
        manager.setId("2fe22fe5-1132-42cf-90f9-1dc17e325a74");
        testUser.setManager(manager);
        testUser.getBackingStore().setIsInitializationCompleted(true);
        testUser.getManager().getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change in the nested Ibackedmodel
        testUser.getManager().setBusinessPhones(Arrays.asList(new String[] {"+1 234 567 891"}));
        // Assert by retrieving only changed values
        testUser.getBackingStore().setReturnOnlyChangedValues(true);
        var changedValues = testUser.getBackingStore().enumerate();
        assertFalse(changedValues.isEmpty());
        assertEquals(1, changedValues.size());
        assertEquals(
                "manager",
                changedValues.keySet()
                        .toArray()[0]); // Backingstore should detect manager property changed
        var changedManager = (TestEntity) changedValues.values().toArray()[0];
        assertNotNull(changedManager);
        assertEquals("2fe22fe5-1132-42cf-90f9-1dc17e325a74", changedManager.getId());
    }

    @Test
    void
            TestsBackingStoreEmbeddedInModelWithByUpdatingNestedIBackedModelReturnsAllNestedProperties() {
        // Arrange dummy user with initialized backingstore
        var testUser = new TestEntity();
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe");
        var manager = new TestEntity();
        manager.setId("2fe22fe5-1132-42cf-90f9-1dc17e325a74");
        testUser.setManager(manager);
        testUser.getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change in the nested Ibackedmodel
        testUser.getManager().setBusinessPhones(Arrays.asList(new String[] {"+1 234 567 891"}));
        // Assert by retrieving only changed values
        testUser.getBackingStore().setReturnOnlyChangedValues(true);
        testUser.getManager().getBackingStore().setReturnOnlyChangedValues(true);
        var changedValues = testUser.getBackingStore().enumerate();
        assertFalse(changedValues.isEmpty());
        assertEquals(1, changedValues.size());
        assertEquals(
                "manager",
                changedValues.keySet()
                        .toArray()[0]); // Backingstore should detect manager property changed
        var changedNestedValues = testUser.getManager().getBackingStore().enumerate();
        assertEquals(4, changedNestedValues.size());
        assertTrue(changedNestedValues.containsKey("id"));
        assertTrue(changedNestedValues.containsKey("businessPhones"));
        var changedManager = (TestEntity) changedValues.values().toArray()[0];
        assertNotNull(changedManager);
        assertEquals("2fe22fe5-1132-42cf-90f9-1dc17e325a74", changedManager.getId());
    }

    @Test
    void TestsBackingStoreEmbeddedInModelWithByUpdatingNestedIBackedModelCollectionProperty() {
        // Arrange dummy user with initialized backingstore
        var testUser = new TestEntity();
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe");
        var firstColleague = new TestEntity();
        firstColleague.setId("2fe22fe5-1132-42cf-90f9-1dc17e325a74");
        var colleagues = Arrays.asList(new TestEntity[] {firstColleague});
        testUser.setColleagues(colleagues);
        testUser.getBackingStore().setIsInitializationCompleted(true);
        testUser.getColleagues().get(0).getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change in the nested Ibackedmodel collection item
        testUser.getColleagues()
                .get(0)
                .setBusinessPhones(Arrays.asList(new String[] {"+1 234 567 891"}));
        // Assert by retrieving only changed values
        testUser.getBackingStore().setReturnOnlyChangedValues(true);
        var changedValues = testUser.getBackingStore().enumerate();
        assertFalse(changedValues.isEmpty());
        assertEquals(1, changedValues.size());
        assertEquals(
                "colleagues",
                changedValues.keySet()
                        .toArray()[0]); // Backingstore should detect colleagues property changed
        var changedColleagues = (List<TestEntity>) (testUser.getBackingStore().get("colleagues"));
        assertNotNull(changedColleagues);
        assertEquals("2fe22fe5-1132-42cf-90f9-1dc17e325a74", changedColleagues.get(0).getId());
    }

    @Test
    void
            TestsBackingStoreEmbeddedInModelWithByUpdatingNestedIBackedModelCollectionPropertyReturnsAllNestedProperties() {
        // Arrange dummy user with initialized backingstore
        var testUser = new TestEntity();
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe");
        var firstColleague = new TestEntity();
        firstColleague.setId("2fe22fe5-1132-42cf-90f9-1dc17e325a74");
        var colleagues = Arrays.asList(new TestEntity[] {firstColleague});
        testUser.setColleagues(colleagues);
        testUser.getBackingStore().setIsInitializationCompleted(true);
        testUser.getColleagues().get(0).getBackingStore().setIsInitializationCompleted(true);

        testUser.getColleagues()
                .get(0)
                .setBusinessPhones(Arrays.asList(new String[] {"+1 234 567 891"}));
        testUser.getBackingStore().setReturnOnlyChangedValues(true);
        testUser.getColleagues()
                .get(0)
                .getBackingStore()
                .setReturnOnlyChangedValues(true); // serializer will do this.
        var changedValues = testUser.getBackingStore().enumerate();
        assertFalse(changedValues.isEmpty());
        assertEquals(1, changedValues.size());
        assertTrue(
                changedValues.containsKey(
                        "colleagues")); // BackingStore should detect colleagues property changed
        var changedNestedValues = testUser.getColleagues().get(0).getBackingStore().enumerate();
        assertEquals(4, changedNestedValues.size());
        assertTrue(changedNestedValues.containsKey("id"));
        assertTrue(changedNestedValues.containsKey("businessPhones"));
    }

    @Test
    void
            TestsBackingStoreEmbeddedInModelWithByUpdatingNestedIBackedModelCollectionPropertyWithExtraValueReturnsAllNestedProperties() {
        // Arrange dummy user with initialized backing store
        var testUser = new TestEntity();
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe");
        var firstColleague = new TestEntity();
        firstColleague.setId("2fe22fe5-1132-42cf-90f9-1dc17e325a74");
        List<String> phonesList = new ArrayList<>();
        phonesList.add("+1 234 567 891");
        firstColleague.setBusinessPhones(phonesList);
        var colleagues = Arrays.asList(new TestEntity[] {firstColleague});
        testUser.setColleagues(colleagues);

        testUser.getBackingStore().setIsInitializationCompleted(true);
        testUser.getColleagues().get(0).getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change in the nested Ibackedmodel collection item
        testUser.getColleagues().get(0).getBusinessPhones().add("+9 876 543 219");
        // Assert by retrieving only changed values

        testUser.getColleagues()
                .get(0)
                .getBackingStore()
                .setReturnOnlyChangedValues(true); // serializer will do this.
        testUser.getBackingStore().setReturnOnlyChangedValues(true);
        var changedValues = testUser.getBackingStore().enumerate();
        assertFalse(changedValues.isEmpty());
        assertEquals(1, changedValues.size());
        assertEquals(
                "colleagues",
                changedValues.keySet()
                        .toArray()[0]); // Backingstore should detect manager property changed
        var changedNestedValues = testUser.getColleagues().get(0).getBackingStore().enumerate();
        assertEquals(4, changedNestedValues.size());
        assertTrue(changedNestedValues.containsKey("id"));
        assertTrue(changedNestedValues.containsKey("businessPhones"));
        var businessPhones =
                (List<?>) testUser.getColleagues().get(0).getBackingStore().get("businessPhones");
        assertEquals(2, businessPhones.size());
    }

    @Test
    void
            TestsBackingStoreEmbeddedInModelWithByUpdatingNestedIBackedModelCollectionPropertyWithExtraIBackedModelValueReturnsAllNestedProperties() {
        // Arrange dummy user with initialized backing store
        var testUser = new TestEntity();
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe");
        var firstColleague = new TestEntity();
        firstColleague.setId("2fe22fe5-1132-42cf-90f9-1dc17e325a74");
        firstColleague.setBusinessPhones(Arrays.asList(new String[] {"+1 234 567 891"}));
        var colleagues = new ArrayList<TestEntity>();
        colleagues.add(firstColleague);
        testUser.setColleagues(colleagues);
        testUser.getBackingStore().setIsInitializationCompleted(true);
        testUser.getColleagues().get(0).getBackingStore().setIsInitializationCompleted(true);
        // Act on the data by making a change in the nested Ibackedmodel collection item
        var secondColleague = new TestEntity();
        secondColleague.setId("2fe22fe5-1132-42cf-90f9-1dc17e325a74");
        testUser.getColleagues().add(secondColleague);
        // Assert by retrieving only changed values

        testUser.getColleagues()
                .get(0)
                .getBackingStore()
                .setReturnOnlyChangedValues(true); // serializer will do this.
        testUser.getBackingStore().setReturnOnlyChangedValues(true);
        var changedValues = testUser.getBackingStore().enumerate();
        assertFalse(changedValues.isEmpty());
        assertEquals(1, changedValues.size());
        assertEquals(
                "colleagues",
                changedValues.keySet()
                        .toArray()[0]); // Backingstore should detect manager property changed
        var colleaguesList = (List<TestEntity>) testUser.getBackingStore().get("colleagues");
        assertEquals(2, colleaguesList.size());
        assertEquals("2fe22fe5-1132-42cf-90f9-1dc17e325a74", colleaguesList.get(0).getId());
        assertEquals("2fe22fe5-1132-42cf-90f9-1dc17e325a74", colleagues.get(0).getId());
        var changedNestedValues = colleaguesList.get(0).getBackingStore().enumerate();
        assertEquals(4, changedNestedValues.size());
        assertTrue(changedNestedValues.containsKey("id"));
        assertTrue(changedNestedValues.containsKey("businessPhones"));
        var businessPhones =
                (List<String>) colleaguesList.get(0).getBackingStore().get("businessPhones");
        assertEquals(1, businessPhones.size());
    }

    @Test
    void TestsBackingStoreEmbeddedWithMultipleNestedModelsCollectionsAndAdditionalData() {
        // Arrange dummy user with initialized backing store
        AtomicInteger invocationCount = new AtomicInteger();
        var testUser = new TestEntity();
        testUser.getBackingStore()
                .subscribe(
                        "testSubscription",
                        (keyString, oldObject, newObject) -> {
                            invocationCount.getAndIncrement();
                        });
        testUser.setId("84c747c1-d2c0-410d-ba50-fc23e0b4abbe"); // invocation 1

        var colleagues = new ArrayList<TestEntity>();
        for (int i = 0; i < 10; i++) {
            var colleague = new TestEntity();
            colleague.setId(UUID.randomUUID().toString());
            colleague.setBusinessPhones(Arrays.asList(new String[] {"+1 234 567 891"}));
            colleague.getAdditionalData().put("count", i);
            colleagues.add(colleague);
        }
        testUser.setColleagues(colleagues); // invocation 2

        testUser.getBackingStore().setIsInitializationCompleted(true); // initialize

        assertEquals(2, invocationCount.get()); // only for setting the id and the colleagues
    }
}
