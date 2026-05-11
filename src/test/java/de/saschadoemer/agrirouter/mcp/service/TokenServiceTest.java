package de.saschadoemer.agrirouter.mcp.service;

import de.saschadoemer.agrirouter.mcp.dto.TokenResponse;
import de.saschadoemer.agrirouter.mcp.persistence.PersistenceService;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
@Property(name = "agrirouter.oauth.client-id", value = "test-client")
@Property(name = "agrirouter.oauth.client-secret", value = "test-secret")
public class TokenServiceTest {

    @Inject
    ApplicationContext applicationContext;

    @Inject
    TokenService tokenService;

    @Inject
    PersistenceService persistenceService;

    @Test
    void testTenantIdPersistence() {
        String tenantId = "test-persistence-id";
        tokenService.setTenantId(tenantId);
        
        // Verify it's in TokenService
        assertEquals(tenantId, tokenService.getTenantId());
        
        // Verify it's in PersistenceService
        assertEquals(tenantId, persistenceService.loadTenantId().orElse(null));
    }

    @Test
    void testPropertiesLoaded() {
        String tokenUrl = applicationContext.getProperty("agrirouter.env.token-url", String.class).orElse(null);
        assertNotNull(tokenUrl, "Property agrirouter.env.token-url should be loaded from application.properties");
        assertEquals("https://api-oauth.agrirouter.com/token", tokenUrl);
    }

    @Test
    void testJwtDecoding() throws Exception {
        // Create a mock JWT with an expiration claim
        long expirationEpochSeconds = java.time.Instant.now().plusSeconds(3600).getEpochSecond();
        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getUrlEncoder().encodeToString(
                ("{\"sub\":\"test-user\",\"name\":\"John Doe\",\"exp\":" + expirationEpochSeconds + "}").getBytes()
        );
        String mockJwt = header + "." + payload + ".signature";

        // Use reflection to call the private method for testing
        Method method = TokenService.class.getDeclaredMethod("decodeAndCacheToken", String.class);
        method.setAccessible(true);
        method.invoke(tokenService, mockJwt);

        java.lang.reflect.Field responseField = TokenService.class.getDeclaredField("cachedTokenResponse");
        responseField.setAccessible(true);
        TokenResponse cachedResponse = (TokenResponse) responseField.get(tokenService);

        java.lang.reflect.Field timeField = TokenService.class.getDeclaredField("tokenExpirationTime");
        timeField.setAccessible(true);
        java.time.Instant cachedExpirationTime = (java.time.Instant) timeField.get(tokenService);

        assertNotNull(cachedResponse, "cachedTokenResponse should be set after decoding");
        assertEquals(mockJwt, cachedResponse.getAccessToken(), "cached access token should match the decoded JWT");
        assertEquals(
                java.time.Instant.ofEpochSecond(expirationEpochSeconds),
                cachedExpirationTime,
                "tokenExpirationTime should match the exp claim from the JWT"
        );
    }
}
