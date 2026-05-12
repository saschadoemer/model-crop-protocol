package de.saschadoemer.agrirouter.mcp.service;

import de.saschadoemer.agrirouter.mcp.dto.AgrirouterState;
import de.saschadoemer.agrirouter.mcp.dto.requests.Capability;
import de.saschadoemer.agrirouter.mcp.dto.requests.EndpointRequest;
import de.saschadoemer.agrirouter.mcp.dto.requests.Subscription;
import de.saschadoemer.agrirouter.mcp.persistence.PersistenceService;
import io.micronaut.core.type.Argument;
import io.micronaut.context.annotation.Value;
import io.micronaut.serde.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service to handle agrirouter endpoints.
 */
@Singleton
public class EndpointService {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointService.class);

    @Value("${agrirouter.external-id-prefix:urn:github:saschadoemer:de:}")
    private String EXTERNAL_ID_PREFIX;

    private final PersistenceService persistenceService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${agrirouter.env.api-url}")
    private String apiUrl;

    @Value("${agrirouter.application-id}")
    private String applicationId;

    @Value("${agrirouter.software-version-id}")
    private String softwareVersionId;

    @Value("${agrirouter.endpoint-type:cloud_software}")
    private String endpointType;

    @Value("${agrirouter.application-name:agrirouter-mcp}")
    private String generatedApplicationName;

    @Value("${agrirouter.software-version.capabilities}")
    private String capabilities;

    @Value("${agrirouter.software-version.subscriptions}")
    private String subscriptions;

    private List<Capability> capabilityList;
    private List<Subscription> subscriptionList;

    public EndpointService(PersistenceService persistenceService, TokenService tokenService, ObjectMapper objectMapper) {
        this.persistenceService = persistenceService;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Setup the capabilities and subscriptions from the configuration.
     */
    @PostConstruct
    public void setup() {
        try {
            this.capabilityList = objectMapper.readValue(capabilities, Argument.listOf(Capability.class));
            this.subscriptionList = objectMapper.readValue(subscriptions, Argument.listOf(Subscription.class));
        } catch (IOException e) {
            LOG.error("Error while parsing capabilities or subscriptions from configuration.", e);
            throw new RuntimeException("Error while parsing capabilities or subscriptions from configuration", e);
        }
    }

    /**
     * Create an endpoint within the agrirouter.
     */
    public void createEndpoint() {
        AgrirouterState state = persistenceService.load().orElse(new AgrirouterState());
        if (state.getTenantId() == null || state.getTenantId().isBlank()) {
            LOG.warn("No tenant ID found, cannot create endpoint.");
            return;
        }

        if (state.getExternalId() == null || state.getExternalId().isBlank()) {
            state.setExternalId(EXTERNAL_ID_PREFIX + UUID.randomUUID());
            persistenceService.save(state);
            LOG.info("Generated and saved new external ID: {}", state.getExternalId());
        }

        LOG.info("Creating endpoint with external ID: {} for tenant: {}", state.getExternalId(), state.getTenantId());

        try {
            final EndpointRequest endpointRequest = new EndpointRequest();
            endpointRequest.setName(generatedApplicationName);
            endpointRequest.setApplicationId(applicationId);
            endpointRequest.setSoftwareVersionId(softwareVersionId);
            endpointRequest.setEndpointType(endpointType);
            endpointRequest.setCapabilities(capabilityList);
            endpointRequest.setSubscriptions(subscriptionList);

            String jsonBody = objectMapper.writeValueAsString(endpointRequest);
            LOG.debug("Sending request to create endpoint: {}", jsonBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/endpoints/" + state.getExternalId()))
                    .header("Authorization", "Bearer " + tokenService.getAccessToken())
                    .header("Content-Type", "application/json")
                    .header("x-agrirouter-tenant-id", state.getTenantId())
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                LOG.info("Successfully created/updated endpoint. Status code: {}", response.statusCode());
            } else {
                LOG.error("Failed to create endpoint. Status code: {}, Body: {}", response.statusCode(), response.body());
                throw new RuntimeException("Failed to create endpoint: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("Error while creating endpoint.", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Error while creating endpoint", e);
        }
    }
}
