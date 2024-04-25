package com.microsoft.kiota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class BaseRequestConfigurationTest {

    public static class TestRequestConfiguration extends BaseRequestConfiguration {
        public TestRequestConfiguration() {
            super();
        }
    }

    @Test
    void testOptions() {
        TestRequestConfiguration requestConfiguration = new TestRequestConfiguration();
        assertNotNull(requestConfiguration.options);

        RequestOption requestOption = new RequestOption() {
            @Override
            public <T extends RequestOption> Class<T> getType() {
                return null;
            }
        };
        // options should be mutable list
        requestConfiguration.options.add(requestOption);
        assertEquals(1, requestConfiguration.options.size());
        assertEquals(requestOption, requestConfiguration.options.get(0));
    }
}
