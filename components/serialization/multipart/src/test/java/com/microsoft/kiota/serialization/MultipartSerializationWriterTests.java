package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.microsoft.kiota.PeriodAndDuration;
import com.microsoft.kiota.MultipartBody;
import com.microsoft.kiota.RequestAdapter;
import com.microsoft.kiota.serialization.JsonSerializationWriterFactory;
import org.junit.jupiter.api.Test;

import com.microsoft.kiota.serialization.mocks.TestEntity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MultipartSerializationWriterTests {
	@Test
	public void throwsOnParsable() throws IOException, UnsupportedEncodingException {
		final var testEntity = new TestEntity();
		testEntity.setId("48d31887-5fad-4d73-a9f5-3c356e68a038");
		testEntity.setWorkDuration(PeriodAndDuration.parse("P1M"));
		testEntity.setStartWorkTime(LocalTime.of(8, 0, 0));
		testEntity.setBirthDay(LocalDate.of(2017, 9, 4));
		testEntity.setDeviceNames(Arrays.asList("device1","device2"));
		testEntity.getAdditionalData().put("mobilePhone", null);
		testEntity.getAdditionalData().put("jobTitle", "Author");
		testEntity.getAdditionalData().put("accountEnabled", false);
		testEntity.getAdditionalData().put("createdDateTime", OffsetDateTime.MIN);
		testEntity.getAdditionalData().put("otherPhones", Arrays.asList(Arrays.asList("device1","device2")));
		try (final var serializationWriter = new MultipartSerializationWriter()) {
			assertThrows(RuntimeException.class, () -> serializationWriter.writeObjectValue(null, testEntity));
		}
	}
	private final byte[] byteForTest = new byte[] { 0x01, 0x02, 0x03 };
	@Test
	public void writesBytArrayValue() throws IOException {
		try (final var serializationWriter = new MultipartSerializationWriter()) {
			serializationWriter.writeByteArrayValue("key", byteForTest);
			try(final var result = serializationWriter.getSerializedContent()) {
				try(final var reader = new BufferedReader(new InputStreamReader(result, "UTF-8"))) {
					final String strResult = reader.lines().collect(Collectors.joining("\n"));
					assertEquals("\u0001\u0002\u0003", strResult);
				}
			}
		}
	}
	@Test
	public void writesAStructuredObject() throws IOException {
		final TestEntity testEntity = new TestEntity();
		testEntity.setId("48d31887-5fad-4d73-a9f5-3c356e68a038");
		testEntity.setWorkDuration(PeriodAndDuration.parse("P1M"));
		testEntity.setStartWorkTime(LocalTime.of(8, 0, 0));
		testEntity.setBirthDay(LocalDate.of(2017, 9, 4));
		testEntity.setDeviceNames(Arrays.asList("device1","device2"));
		testEntity.getAdditionalData().put("mobilePhone", null);
		testEntity.getAdditionalData().put("jobTitle", "Author");
		testEntity.getAdditionalData().put("accountEnabled", false);
		testEntity.getAdditionalData().put("createdDateTime", OffsetDateTime.MIN);
		testEntity.getAdditionalData().put("otherPhones", Arrays.asList(Arrays.asList("device1","device2")));
		final RequestAdapter requestAdapter = mock(RequestAdapter.class);
		when(requestAdapter.getSerializationWriterFactory()).thenReturn(new JsonSerializationWriterFactory());
		try (final var serializationWriter = new MultipartSerializationWriter()) {
			final MultipartBody multipartBody = new MultipartBody();
			multipartBody.requestAdapter = requestAdapter;
			multipartBody.addOrReplacePart("testEntity", "application/json", testEntity);
			multipartBody.addOrReplacePart("image", "application/octet-stream", byteForTest);
			serializationWriter.writeObjectValue(null, multipartBody);

			try(final var result = serializationWriter.getSerializedContent()) {
				try(final var reader = new BufferedReader(new InputStreamReader(result, "UTF-8"))) {
					final String strResult = reader.lines().collect(Collectors.joining("\r\n"));
					assertEquals("--" + multipartBody.getBoundary() + "\r\nContent-Type: application/octet-stream\r\nContent-Disposition: form-data; name=\"image\"\r\n\r\n"+new String(byteForTest, "UTF-8")+"\r\n--" + multipartBody.getBoundary() + "\r\nContent-Type: application/json\r\nContent-Disposition: form-data; name=\"testEntity\"\r\n\r\n{\"id\":\"48d31887-5fad-4d73-a9f5-3c356e68a038\",\"birthDay\":\"2017-09-04\",\"workDuration\":\"P1M\",\"startWorkTime\":\"08:00:00\",\"deviceNames\":[\"device1\",\"device2\"],\"mobilePhone\":null,\"jobTitle\":\"Author\",\"createdDateTime\":\"-999999999-01-01T00:00:00+18:00\",\"otherPhones\":[[\"device1\",\"device2\"]],\"accountEnabled\":false}\r\n--" + multipartBody.getBoundary() + "--", strResult);
				}
			}
		}
	}
}
