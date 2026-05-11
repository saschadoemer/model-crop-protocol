package de.saschadoemer.agrirouter.mcp.dto.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

/**
 * DTO for the endpoint creation request.
 */
@Serdeable
@Introspected
public class EndpointRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("application_id")
    private String applicationId;

    @JsonProperty("software_version_id")
    private String softwareVersionId;

    @JsonProperty("endpoint_type")
    private String endpointType;

    @JsonProperty("capabilities")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private List<Capability> capabilities;

    @JsonProperty("subscriptions")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private List<Subscription> subscriptions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getSoftwareVersionId() {
        return softwareVersionId;
    }

    public void setSoftwareVersionId(String softwareVersionId) {
        this.softwareVersionId = softwareVersionId;
    }

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Capability> capabilities) {
        this.capabilities = capabilities;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
