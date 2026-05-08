package de.saschadoemer.agrirouter.mcp.controller;

import de.saschadoemer.agrirouter.mcp.service.TokenService;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Controller for agrirouter authorization.
 */
@Controller("/authorization")
public class AuthorizationController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationController.class);

    @Value("${agrirouter.oauth.client-id}")
    private String clientId;

    @Value("${agrirouter.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${agrirouter.env.authorize-url}")
    private String authorizeUrl;

    /**
     * Get the agrirouter authorization URL.
     *
     * @return the agrirouter authorization URL
     */
    @Get("/redirect")
    @Produces(MediaType.TEXT_PLAIN)
    public HttpResponse<String> getRedirectUri() {
        String state = generateRandomState();
        String url = String.format("%s?client_id=%s&redirect_uri=%s&scope=endpoints:manage&state=%s",
                authorizeUrl, clientId, redirectUri, state);
        LOG.info("Returning agrirouter authorization URL: {}", url);
        return HttpResponse.ok(url);
    }

    private String generateRandomState() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
