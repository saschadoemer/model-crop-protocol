package de.saschadoemer.agrirouter.mcp.controller;

import de.saschadoemer.agrirouter.mcp.service.TokenService;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@Property(name = "agrirouter.oauth.client-id", value = "test-client-id")
@Property(name = "agrirouter.oauth.client-secret", value = "test-client-secret")
@Property(name = "agrirouter.oauth.redirect-uri", value = "https://my-app.com/callback")
@Property(name = "agrirouter.env.authorize-url", value = "https://app.agrirouter.com/api/authorize")
@Property(name = "agrirouter.env.token-url", value = "https://api-oauth.agrirouter.com/token")
@Property(name = "agrirouter.env.api-url", value = "https://api.agrirouter.com")
public class AuthorizationControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testGetRedirectUri() {
        String response = client.toBlocking().retrieve(
                HttpRequest.GET("/authorization/redirect").header("Authorization", "Bearer test-token")
        );
        assertNotNull(response);
        assertTrue(response.contains("https://app.agrirouter.com/api/authorize"));
        assertTrue(response.contains("client_id=test-client-id"));
        assertTrue(response.contains("redirect_uri=https://my-app.com/callback"));
        assertTrue(response.contains("scope=endpoints:manage"));
        assertTrue(response.contains("state="));
    }

    @Singleton
    @Replaces(TokenService.class)
    static class MockTokenService extends TokenService {
        public MockTokenService() {
            super(null);
        }

        @Override
        public String getAccessToken() {
            return "test-token";
        }
    }
}
