package com.microsoft.kiota;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UriTemplateTest {

    Map<String, Object> params = Map.of(
            "baseurl", "http://localhost:8080",
            "id", 1,
            "name", "foo",
            "async", true,
            "page", 10,
            "date", OffsetDateTime.of(2023, 2, 13, 18, 49, 00, 00, ZoneOffset.UTC));

    // Tests inspired from: https://github.com/micronaut-projects/micronaut-core/blob/02992a905cf9a2279b7fe8e49927ff080cb937d5/http/src/test/groovy/io/micronaut/http/uri/UriTemplateSpec.groovy
    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1, delimiter = ';')
    void shouldProduceExpectedOutput(String template, String expected) {
        // Arrange
        UriTemplate uriTemplate = new UriTemplate(template);

        // Act
        String result = uriTemplate.expand(params);

        // Assert
        assertEquals(expected, result);
    }
}
