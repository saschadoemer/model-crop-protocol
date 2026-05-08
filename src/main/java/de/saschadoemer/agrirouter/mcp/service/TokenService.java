package de.saschadoemer.agrirouter.mcp.service;

import de.saschadoemer.agrirouter.mcp.dto.TokenResponse;
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
import java.time.Instant;

/**
 * Service to handle agrirouter tokens.
 */
@Singleton
public class TokenService {

    private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient;

    @Value("${agrirouter.oauth.client-id}")
    private String clientId;

    @Value("${agrirouter.oauth.client-secret}")
    private String clientSecret;

    @Value("${agrirouter.env.token-url}")
    private String tokenUrl;

    private TokenResponse cachedTokenResponse;

    private Instant tokenExpirationTime;

    public TokenService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Get the access token. Fetches a new one if it's expired or not available.
     *
     * @return the access token
     */
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
            tokenExpirationTime = Instant.now().plusSeconds(cachedTokenResponse.getExpiresIn());
            LOG.info("Successfully fetched and decoded agrirouter token. Expires in {} seconds.", cachedTokenResponse.getExpiresIn());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching agrirouter token", e);
        }
    }

    /**
     * Decode and cache the token.
     *
     * @param jwt the token as JWT
     */
    private void decodeAndCacheToken(String jwt) {
        this.cachedTokenResponse = new TokenResponse();
        this.cachedTokenResponse.setAccessToken(jwt);
        this.cachedTokenResponse.setExpiresIn(3600);
        this.tokenExpirationTime = Instant.now().plusSeconds(3600);
    }

}
