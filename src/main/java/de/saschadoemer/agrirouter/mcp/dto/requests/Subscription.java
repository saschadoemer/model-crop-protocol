package de.saschadoemer.agrirouter.mcp.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

/**
 * DTO for the subscriptions of an endpoint.
 */
@Serdeable
@Introspected
public class Subscription {

    @JsonProperty("message_type")
    private String messageType;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
