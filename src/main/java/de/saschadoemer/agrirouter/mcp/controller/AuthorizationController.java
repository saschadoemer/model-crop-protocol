package de.saschadoemer.agrirouter.mcp.controller;

import de.saschadoemer.agrirouter.mcp.service.EndpointService;
import de.saschadoemer.agrirouter.mcp.service.TokenService;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for agrirouter authorization.
 */
@Controller
public class AuthorizationController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationController.class);

    private final TokenService tokenService;

    private final EndpointService endpointService;

    private final Set<String> states = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Value("${agrirouter.oauth.client-id}")
    private String clientId;

    @Value("${agrirouter.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${agrirouter.env.authorize-url}")
    private String authorizeUrl;

    @Inject
    public AuthorizationController(TokenService tokenService, EndpointService endpointService) {
        this.tokenService = tokenService;
        this.endpointService = endpointService;
    }

    /**
     * Get the agrirouter authorization URL.
     *
     * @return the agrirouter authorization URL
     */
    @Get("/authorization/redirect")
    @Produces(MediaType.TEXT_PLAIN)
    public HttpResponse<String> getRedirectUri() {
        String state = generateRandomState();
        states.add(state);
        String url = String.format("%s?client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                authorizeUrl,
                URLEncoder.encode(clientId, StandardCharsets.UTF_8),
                URLEncoder.encode(redirectUri, StandardCharsets.UTF_8),
                URLEncoder.encode("endpoints:manage", StandardCharsets.UTF_8),
                URLEncoder.encode(state, StandardCharsets.UTF_8));
        LOG.info("Returning agrirouter authorization URL: {}", url);
        return HttpResponse.ok(url);
    }

    /**
     * Callback for the agrirouter authorization.
     *
     * @param state    the state parameter
     * @param error    the error parameter
     * @param tenantId the tenant ID parameter
     * @return the response
     */
    @Get("/agrirouter/auth/callback")
    public HttpResponse<String> callback(@QueryValue("state") String state,
                                         @Nullable @QueryValue("error") String error,
                                         @Nullable @QueryValue("tenant_id") String tenantId) {
        LOG.info("Received callback from agrirouter with state: {}, error: {}, tenant_id: {}", state, error, tenantId);
        if (!states.remove(state)) {
            LOG.error("State parameter does not match the value originally sent.");
            return HttpResponse.status(HttpStatus.FORBIDDEN).body("State parameter does not match the value originally sent.");
        }
        if (error != null && !error.isEmpty()) {
            LOG.error("Error during agrirouter authorization: {}", error);
            return HttpResponse.badRequest("Error during agrirouter authorization: " + error);
        }
        if (tenantId != null && !tenantId.isEmpty()) {
            tokenService.setTenantId(tenantId);
            LOG.info("Successfully stored tenant ID: {}", tenantId);
            endpointService.createEndpoint();
            LOG.info("Successfully triggered endpoint creation for tenant ID: {}", tenantId);
        }
        return HttpResponse.ok("Authorization successful. You can close this window now.");
    }

    private String generateRandomState() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
