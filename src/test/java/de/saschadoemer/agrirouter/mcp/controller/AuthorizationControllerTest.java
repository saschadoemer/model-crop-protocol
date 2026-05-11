package de.saschadoemer.agrirouter.mcp.controller;

import de.saschadoemer.agrirouter.mcp.persistence.PersistenceService;
import de.saschadoemer.agrirouter.mcp.service.EndpointService;
import de.saschadoemer.agrirouter.mcp.service.TokenService;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@Property(name = "spec.name", value = "AuthorizationControllerTest")
@Property(name = "agrirouter.oauth.client-id", value = "test-client-id")
@Property(name = "agrirouter.oauth.client-secret", value = "test-client-secret")
@Property(name = "agrirouter.oauth.redirect-uri", value = "https://my-app.com/callback")
@Property(name = "agrirouter.env.authorize-url", value = "https://app.agrirouter.com/api/authorize")
@Property(name = "agrirouter.env.token-url", value = "https://api-oauth.agrirouter.com/token")
@Property(name = "agrirouter.env.api-url", value = "https://api.agrirouter.com")
@Property(name = "agrirouter.application-id", value = "test-app-id")
@Property(name = "agrirouter.software-version-id", value = "test-version-id")
@Property(name = "persistence.storage-path", value = "storage-auth-test.json")
public class AuthorizationControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    TokenService tokenService;

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(Paths.get("storage-auth-test.json"));
    }

    @Test
    void testGetRedirectUri() {
        String response = client.toBlocking().retrieve(
                HttpRequest.GET("/authorization/redirect").header("Authorization", "Bearer test-token")
        );
        assertNotNull(response);
        assertTrue(response.contains("https://app.agrirouter.com/api/authorize"));
        assertTrue(response.contains("client_id=test-client-id"));
        assertTrue(response.contains("redirect_uri=https%3A%2F%2Fmy-app.com%2Fcallback"));
        assertTrue(response.contains("scope=endpoints%3Amanage"));
        assertTrue(response.contains("state="));
    }

    @Test
    void testCallbackSuccess() {
        // 1. Get the redirect URI to generate and store a state
        String redirectUrl = client.toBlocking().retrieve(
                HttpRequest.GET("/authorization/redirect").header("Authorization", "Bearer test-token")
        );
        String state = extractQueryParam(redirectUrl);

        // 2. Call the callback with the state and a tenant_id
        HttpResponse<String> response = client.toBlocking().exchange(
                HttpRequest.GET("/agrirouter/auth/callback?state=" + state + "&tenant_id=test-tenant-id"),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("Authorization successful. You can close this window now.", response.body());
        assertEquals("test-tenant-id", tokenService.getTenantId());
    }

    @Test
    void testCallbackError() {
        // 1. Get the redirect URI to generate and store a state
        String redirectUrl = client.toBlocking().retrieve(
                HttpRequest.GET("/authorization/redirect").header("Authorization", "Bearer test-token")
        );
        String state = extractQueryParam(redirectUrl);

        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
                client.toBlocking().exchange(
                        HttpRequest.GET("/agrirouter/auth/callback?state=" + state + "&error=access_denied"),
                        String.class
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getResponse().getBody(String.class).orElse("").contains("Error during agrirouter authorization: access_denied"));
    }

    @Test
    void testCallbackInvalidState() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
                client.toBlocking().exchange(
                        HttpRequest.GET("/agrirouter/auth/callback?state=invalid-state&tenant_id=test-tenant-id"),
                        String.class
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertTrue(exception.getResponse().getBody(String.class).orElse("").contains("State parameter does not match the value originally sent."));
    }

    private static String extractQueryParam(String url) {
        String rawQuery = URI.create(url).getRawQuery();
        if (rawQuery == null) {
            throw new RuntimeException("URL has no query string: " + url);
        }
        return Arrays.stream(rawQuery.split("&"))
                .map(p -> p.split("=", 2))
                .filter(p -> p[0].equals("state"))
                .map(p -> p.length > 1 ? p[1] : "")
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Parameter '" + "state" + "' not found in URL"));
    }

    @Singleton
    @Replaces(TokenService.class)
    @Requires(property = "spec.name", value = "AuthorizationControllerTest")
    static class MockTokenService extends TokenService {
        @Inject
        public MockTokenService(PersistenceService persistenceService) {
            super(null, persistenceService);
        }

        @Override
        public String getAccessToken() {
            return "test-token";
        }
    }

    @Singleton
    @Replaces(EndpointService.class)
    @Requires(property = "spec.name", value = "AuthorizationControllerTest")
    static class MockEndpointService extends EndpointService {
        public MockEndpointService() {
            super(null, null, null);
        }

        @Override
        public void createEndpoint() {
            // Do nothing
        }
    }
}
