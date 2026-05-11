package de.saschadoemer.agrirouter.mcp.service;

import de.saschadoemer.agrirouter.mcp.dto.TokenResponse;
import de.saschadoemer.agrirouter.mcp.persistence.PersistenceService;
import io.micronaut.context.annotation.Value;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * Service to handle agrirouter tokens.
 */
@Singleton
public class TokenService {

    private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);

    private final ObjectMapper objectMapper;

    private final PersistenceService persistenceService;

    private final HttpClient httpClient;

    @Value("${agrirouter.oauth.client-id}")
    private String clientId;

    @Value("${agrirouter.oauth.client-secret}")
    private String clientSecret;

    @Value("${agrirouter.env.token-url}")
    private String tokenUrl;

    private TokenResponse cachedTokenResponse;

    private Instant tokenExpirationTime;

    private String tenantId;

    public TokenService(ObjectMapper objectMapper, PersistenceService persistenceService) {
        this.objectMapper = objectMapper;
        this.persistenceService = persistenceService;
        this.httpClient = HttpClient.newHttpClient();
        if (this.persistenceService != null) {
            this.persistenceService.loadTenantId().ifPresent(id -> this.tenantId = id);
        }
    }

    /**
     * Get the tenant ID.
     *
     * @return the tenant ID
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Set the tenant ID.
     *
     * @param tenantId the tenant ID
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
        if (this.persistenceService != null) {
            this.persistenceService.saveTenantId(tenantId);
        }
    }

    /**
     * Get the access token. Fetches a new one if it's expired or not available.
     *
     * @return the access token
     */
    @SuppressWarnings({"unused"})
    public String getAccessToken() {
        if (cachedTokenResponse == null || isTokenExpired()) {
            fetchToken();
        }
        return cachedTokenResponse.getAccessToken();
    }

    private boolean isTokenExpired() {
        if (tokenExpirationTime == null) {
            return true;
        }
        return Instant.now().isAfter(tokenExpirationTime.minusSeconds(60));
    }

    private void fetchToken() {
        LOG.info("Fetching new agrirouter token from {}...", tokenUrl);
        String body = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s",
                clientId,
                clientSecret);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch token: " + response.body());
            }
            cachedTokenResponse = objectMapper.readValue(response.body(), TokenResponse.class);
            if (cachedTokenResponse == null) {
            String accessToken = cachedTokenResponse.getAccessToken();
            if (accessToken == null || accessToken.isBlank()) {
                throw new RuntimeException("Failed to fetch token: token response is missing a valid access_token value.");
            }
                throw new RuntimeException("Failed to fetch token: token response body could not be parsed.");
            }
            Integer expiresIn = cachedTokenResponse.getExpiresIn();
            if (expiresIn == null || expiresIn <= 0) {
                throw new RuntimeException("Failed to fetch token: token response is missing a valid expires_in value.");
            }
            tokenExpirationTime = Instant.now().plusSeconds(expiresIn);
            LOG.info("Successfully fetched and decoded agrirouter token. Expires in {} seconds.", expiresIn);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching agrirouter token", e);
        }
    }

    /**
     * Decode and cache the token.
     *
     * @param jwt the token as JWT
     */
    @SuppressWarnings({"unused"})
    private void decodeAndCacheToken(String jwt) {
        this.cachedTokenResponse = new TokenResponse();
        this.cachedTokenResponse.setAccessToken(jwt);
        long expirationEpochSeconds = -1;
        String[] parts = jwt.split("\\.");
        if (parts.length >= 2) {
            try {
                byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
                String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);
                @SuppressWarnings("unchecked")
                Map<String, Object> claims = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(payloadJson, Map.class);
                Object exp = claims.get("exp");
                if (exp instanceof Number) {
                    expirationEpochSeconds = ((Number) exp).longValue();
                }
            } catch (Exception e) {
                LOG.warn("Failed to decode JWT payload, using default expiration", e);
            }
        }
        this.tokenExpirationTime = expirationEpochSeconds > 0
                ? Instant.ofEpochSecond(expirationEpochSeconds)
                : Instant.now().plusSeconds(3600);
    }

}
