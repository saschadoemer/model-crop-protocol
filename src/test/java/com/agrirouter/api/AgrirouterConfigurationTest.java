package com.agrirouter.api;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
@Property(name = "agrirouter.oauth.client-id", value = "test-client-id")
@Property(name = "agrirouter.oauth.client-secret", value = "test-client-secret")
public class AgrirouterConfigurationTest {

    @Inject
    AgrirouterConfiguration configuration;

    @Test
    void testConfiguration() {
        assertEquals("test-client-id", configuration.getClientId());
        assertEquals("test-client-secret", configuration.getClientSecret());
    }
}
