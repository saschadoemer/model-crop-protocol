package de.saschadoemer.agrirouter.mcp.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

/**
 * DTO for the capabilities of an endpoint.
 */
@Serdeable
@Introspected
public class Capability {

    @JsonProperty("message_type")
    private String messageType;

    @JsonProperty("direction")
    private String direction;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
