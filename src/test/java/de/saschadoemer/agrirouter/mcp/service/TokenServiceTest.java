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
        assertNotNull(tokenUrl, "Property agrirouter.env.token-url should be loaded from application.yml");
        assertEquals("https://api-oauth.agrirouter.com/token", tokenUrl);
    }

    @Test
    void testJwtDecoding() throws Exception {
        // Create a mock JWT
        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getUrlEncoder().encodeToString("{\"sub\":\"test-user\",\"name\":\"John Doe\"}".getBytes());
        String mockJwt = header + "." + payload + ".signature";

        // Mock the cachedTokenResponse to avoid fetchToken() call
        TokenResponse mockResponse = new TokenResponse();
        mockResponse.setAccessToken(mockJwt);
        mockResponse.setExpiresIn(3600);
        
        java.lang.reflect.Field responseField = TokenService.class.getDeclaredField("cachedTokenResponse");
        responseField.setAccessible(true);
        responseField.set(tokenService, mockResponse);
        
        java.lang.reflect.Field timeField = TokenService.class.getDeclaredField("tokenExpirationTime");
        timeField.setAccessible(true);
        timeField.set(tokenService, java.time.Instant.now().plusSeconds(3600));

        // Use reflection to call the private method for testing
        Method method = TokenService.class.getDeclaredMethod("decodeAndCacheToken", String.class);
        method.setAccessible(true);
        method.invoke(tokenService, mockJwt);
    }
}
