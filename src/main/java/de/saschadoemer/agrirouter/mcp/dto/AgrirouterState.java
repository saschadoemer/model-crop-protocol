package de.saschadoemer.agrirouter.mcp.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

/**
 * DTO to hold agrirouter state.
 */
@Serdeable
@Introspected
public class AgrirouterState {

    private String tenantId;
    private String externalId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
