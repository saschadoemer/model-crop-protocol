package com.agrirouter.api;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * Configuration for the agrirouter.
 */
@ConfigurationProperties("agrirouter.oauth")
public class AgrirouterConfiguration {

    private String clientId;
    private String clientSecret;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
