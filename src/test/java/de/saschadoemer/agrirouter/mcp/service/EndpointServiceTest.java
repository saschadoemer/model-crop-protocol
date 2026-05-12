package de.saschadoemer.agrirouter.mcp.service;

import de.saschadoemer.agrirouter.mcp.dto.requests.Capability;
import de.saschadoemer.agrirouter.mcp.dto.requests.EndpointRequest;
import de.saschadoemer.agrirouter.mcp.dto.requests.Subscription;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class EndpointServiceTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    void testParseCapabilities() throws IOException {
        String capabilitiesJson = "[{\"message_type\":\"vid:wmv\",\"direction\":\"SEND\"},{\"message_type\":\"vid:avi\",\"direction\":\"SEND\"}]";
        List<Capability> capabilities = objectMapper.readValue(capabilitiesJson, Argument.listOf(Capability.class));

        assertNotNull(capabilities);
        assertEquals(2, capabilities.size());
        assertEquals("vid:wmv", capabilities.get(0).getMessageType());
        assertEquals("SEND", capabilities.get(0).getDirection());
        assertEquals("vid:avi", capabilities.get(1).getMessageType());
        assertEquals("SEND", capabilities.get(1).getDirection());
    }

    @Test
    void testParseSubscriptions() throws IOException {
        String subscriptionsJson = "[{\"message_type\":\"iso:11783:-10:taskdata:zip\"}]";
        List<Subscription> subscriptions = objectMapper.readValue(subscriptionsJson, Argument.listOf(Subscription.class));

        assertNotNull(subscriptions);
        assertEquals(1, subscriptions.size());
        assertEquals("iso:11783:-10:taskdata:zip", subscriptions.getFirst().getMessageType());
    }

    @Test
    void testEndpointRequestSerializationWithEmptySubscriptions() throws IOException {
        EndpointRequest request = new EndpointRequest();
        request.setSubscriptions(Collections.emptyList());
        request.setCapabilities(Collections.emptyList());

        String json = objectMapper.writeValueAsString(request);
        assertTrue(json.contains("\"subscriptions\":[]"), "JSON should contain empty subscriptions list: " + json);
    }
}
